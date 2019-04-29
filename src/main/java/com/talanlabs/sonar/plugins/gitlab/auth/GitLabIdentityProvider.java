/*
 * SonarQube :: GitLab Auth Plugin
 * Copyright (C) 2016-2018 TalanLabs
 * gabriel.allaigre@talanlabs.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.talanlabs.sonar.plugins.gitlab.auth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.talanlabs.gitlab.api.Paged;
import com.talanlabs.gitlab.api.v3.GitLabAPI;
import java.util.concurrent.ExecutionException;
import org.sonar.api.server.ServerSide;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.OAuth2IdentityProvider;
import org.sonar.api.server.authentication.UserIdentity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

@ServerSide
public class GitLabIdentityProvider implements OAuth2IdentityProvider {

    private static final Logger LOGGER = Loggers.get(GitLabIdentityProvider.class);

    private final GitLabConfiguration gitLabConfiguration;
    private final GitLabOAuthApi gitLabOAuthApi;

    public GitLabIdentityProvider(GitLabConfiguration gitLabConfiguration, GitLabOAuthApi gitLabOAuthApi) {
        this.gitLabConfiguration = gitLabConfiguration;
        this.gitLabOAuthApi = gitLabOAuthApi;
    }

    @Override
    public String getKey() {
        return "gitlab";
    }

    @Override
    public String getName() {
        return "GitLab";
    }

    @Override
    public Display getDisplay() {
        return Display.builder()
                // URL of src/main/resources/static/gitlab.svg at runtime
                .setIconPath("/static/authgitlab/gitlab.svg").setBackgroundColor("#333c47").build();
    }

    @Override
    public boolean isEnabled() {
        return gitLabConfiguration.isEnabled();
    }

    @Override
    public boolean allowsUsersToSignUp() {
        return gitLabConfiguration.allowUsersToSignUp();
    }

    @Override
    public void init(InitContext context) {
        String state = context.generateCsrfState();
        OAuth20Service scribe = prepareScribe(context).build(gitLabOAuthApi);
        String url = scribe.getAuthorizationUrl(state);
        context.redirectTo(url);
    }

    private static IllegalStateException unexpectedResponseCode(String requestUrl, Response response) throws IOException {
        return new IllegalStateException(format("Fail to execute request '%s'. HTTP code: %s, response: %s", requestUrl, response.getCode(), response.getBody()));
    }

    private static Response executeRequest(String requestUrl, OAuth20Service scribe, OAuth2AccessToken accessToken) throws IOException, ExecutionException, InterruptedException {
        OAuthRequest request = new OAuthRequest(Verb.GET, requestUrl);
        scribe.signRequest(accessToken, request);
        Response response = scribe.execute(request);
        if (!response.isSuccessful()) {
            throw unexpectedResponseCode(requestUrl, response);
        }
        return response;
    }

    @Override
    public void callback(CallbackContext context) {
        try {
            onCallback(context);
        } catch (IOException | ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public void onCallback(CallbackContext context) throws InterruptedException, ExecutionException, IOException {
        context.verifyCsrfState();

        HttpServletRequest request = context.getRequest();
        OAuth20Service scribe = prepareScribe(context).build(gitLabOAuthApi);
        String oAuthVerifier = request.getParameter("code");

        OAuth2AccessToken accessToken = scribe.getAccessToken(oAuthVerifier);

        String userResponseBody = executeRequest(gitLabConfiguration.url() + "/api/" + gitLabConfiguration.apiVersion() + "/user", scribe, accessToken).getBody();

        LOGGER.trace("User response received : %s", userResponseBody);
        GsonUser gsonUser = GsonUser.parse(userResponseBody);

        UserIdentity.Builder builder = UserIdentity.builder().setProviderLogin(gsonUser.getUsername()).setLogin(gsonUser.getUsername()).setName(gsonUser.getName()).setEmail(gsonUser.getEmail());
        if (!gitLabConfiguration.userExceptions().contains(gsonUser.getUsername())) {
            Set<String> groups = getUserGroups(accessToken);
            if (!groups.isEmpty()) {
                builder.setGroups(groups);
            }
        }

        context.authenticate(builder.build());
        context.redirectToRequestedPage();
    }

    private Set<String> getUserGroups(OAuth2AccessToken accessToken) {
        Set<String> groups = new HashSet<>();
        if (!gitLabConfiguration.groups().isEmpty()) {
            groups.addAll(gitLabConfiguration.groups());
        }
        if (gitLabConfiguration.syncUserGroups()) {
            groups.addAll(getUserGitLabGroups(accessToken));
        }
        return groups;
    }

    private Set<String> getUserGitLabGroups(OAuth2AccessToken accessToken) {
        Set<String> groups = Collections.emptySet();
        try {
            if (GitLabAuthPlugin.V3_API_VERSION.equals(gitLabConfiguration.apiVersion())) {
                com.talanlabs.gitlab.api.v3.GitLabAPI api = createV3Api(accessToken.getAccessToken());
                groups = Stream.of(api.getGitLabAPIGroups().getMyGroups()).map(Paged::getResults)
                    .flatMap(Collection::stream)
                    .map(com.talanlabs.gitlab.api.v3.models.GitlabGroup::getName)
                    .collect(Collectors.toSet());
            } else if (GitLabAuthPlugin.V4_API_VERSION.equals(gitLabConfiguration.apiVersion())) {
                com.talanlabs.gitlab.api.v4.GitLabAPI api = createV4Api(accessToken.getAccessToken());
                groups = Stream.of(api.getGitLabAPIGroups().getMyGroups()).map(Paged::getResults)
                    .flatMap(Collection::stream)
                    .map(com.talanlabs.gitlab.api.v4.models.GitlabGroup::getName)
                    .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            LOGGER.error("An error occured when trying to fetch user's groups", e);
        }
        return groups;
    }

    private GitLabAPI createV3Api(String accessToken) {
        return com.talanlabs.gitlab.api.v3.GitLabAPI.connect(gitLabConfiguration.url(), accessToken, com.talanlabs.gitlab.api.v3.TokenType.ACCESS_TOKEN).setIgnoreCertificateErrors(gitLabConfiguration.ignoreCertificate());
    }

    private com.talanlabs.gitlab.api.v4.GitLabAPI createV4Api(String accessToken) {
        return com.talanlabs.gitlab.api.v4.GitLabAPI.connect(gitLabConfiguration.url(), accessToken, com.talanlabs.gitlab.api.v4.TokenType.ACCESS_TOKEN).setIgnoreCertificateErrors(gitLabConfiguration.ignoreCertificate());
    }

    private ServiceBuilder prepareScribe(OAuth2IdentityProvider.OAuth2Context context) {
        if (!isEnabled()) {
            throw new IllegalStateException("GitLab Authentication is disabled");
        }
        ServiceBuilder serviceBuilder = new ServiceBuilder(gitLabConfiguration.applicationId())
            .apiKey(gitLabConfiguration.applicationId())
            .apiSecret(gitLabConfiguration.secret())
            .callback(context.getCallbackUrl());

        if (gitLabConfiguration.scope() != null && !GitLabAuthPlugin.NONE_SCOPE.equals(gitLabConfiguration.scope())) {
            serviceBuilder.defaultScope(gitLabConfiguration.scope());
        }
        return serviceBuilder;
    }
}
