/*
 * SonarQube :: GitLab Auth Plugin
 * Copyright (C) 2016-2017 TalanLabs
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
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.model.Verifier;
import com.github.scribejava.core.oauth.OAuthService;
import com.talanlabs.gitlab.api.Paged;
import com.talanlabs.gitlab.api.v3.GitLabAPI;
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

    private static final Token EMPTY_TOKEN = null;

    private final GitLabConfiguration gitLabConfiguration;

    public GitLabIdentityProvider(GitLabConfiguration gitLabConfiguration) {
        this.gitLabConfiguration = gitLabConfiguration;
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
        OAuthService scribe = prepareScribe(context).build();
        String url = scribe.getAuthorizationUrl(EMPTY_TOKEN);
        context.redirectTo(url);
    }

    @Override
    public void callback(CallbackContext context) {
        HttpServletRequest request = context.getRequest();
        OAuthService scribe = prepareScribe(context).build();
        String oAuthVerifier = request.getParameter("code");

        Token accessToken = scribe.getAccessToken(EMPTY_TOKEN, new Verifier(oAuthVerifier));

        OAuthRequest userRequest = new OAuthRequest(Verb.GET, gitLabConfiguration.url() + "/api/" + gitLabConfiguration.apiVersion() + "/user", scribe);
        scribe.signRequest(accessToken, userRequest);

        com.github.scribejava.core.model.Response userResponse = userRequest.send();
        if (!userResponse.isSuccessful()) {
            throw new IllegalStateException(format("Fail to authenticate the user. Error code is %s, Body of the response is %s", userResponse.getCode(), userResponse.getBody()));
        }
        String userResponseBody = userResponse.getBody();
        LOGGER.trace("User response received : %s", userResponseBody);
        GsonUser gsonUser = GsonUser.parse(userResponseBody);

        UserIdentity.Builder builder = UserIdentity.builder().setProviderLogin(gsonUser.getUsername()).setLogin(gsonUser.getUsername()).setName(gsonUser.getName()).setEmail(gsonUser.getEmail());
        if (!gitLabConfiguration.userExceptions().contains(gsonUser.getUsername())) {
            Set<String> groups = getUserGroups(accessToken);
            if (groups != null && !groups.isEmpty()) {
                builder.setGroups(groups);
            }
        }

        context.authenticate(builder.build());
        context.redirectToRequestedPage();
    }

    private Set<String> getUserGroups(Token accessToken) {
        Set<String> groups = new HashSet<>();
        if (!gitLabConfiguration.groups().isEmpty()) {
            groups.addAll(gitLabConfiguration.groups());
        }
        if (gitLabConfiguration.syncUserGroups()) {
            groups.addAll(getUserGitLabGroups(accessToken));
        }
        return groups;
    }

    private Set<String> getUserGitLabGroups(Token accessToken) {
        Set<String> groups = Collections.emptySet();
        try {
            if (GitLabAuthPlugin.V3_API_VERSION.equals(gitLabConfiguration.apiVersion())) {
                com.talanlabs.gitlab.api.v3.GitLabAPI api = createV3Api(accessToken.getToken());
                groups = Stream.of(api.getGitLabAPIGroups().getMyGroups()).map(Paged::getResults).flatMap(Collection::stream).map(com.talanlabs.gitlab.api.v3.models.GitlabGroup::getName)
                        .collect(Collectors.toSet());
            } else if (GitLabAuthPlugin.V4_API_VERSION.equals(gitLabConfiguration.apiVersion())) {
                com.talanlabs.gitlab.api.v4.GitLabAPI api = createV4Api(accessToken.getToken());
                groups = Stream.of(api.getGitLabAPIGroups().getMyGroups()).map(Paged::getResults).flatMap(Collection::stream).map(com.talanlabs.gitlab.api.v4.models.GitlabGroup::getName)
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            LOGGER.error("An error occured when trying to fetch user's groups", e);
        }
        return groups;
    }

    private GitLabAPI createV3Api(String accessToken) {
        return com.talanlabs.gitlab.api.v3.GitLabAPI.connect(gitLabConfiguration.url(), accessToken, com.talanlabs.gitlab.api.v3.TokenType.ACCESS_TOKEN);
    }

    private com.talanlabs.gitlab.api.v4.GitLabAPI createV4Api(String accessToken) {
        return com.talanlabs.gitlab.api.v4.GitLabAPI.connect(gitLabConfiguration.url(), accessToken, com.talanlabs.gitlab.api.v4.TokenType.ACCESS_TOKEN);
    }

    private ServiceBuilder prepareScribe(OAuth2IdentityProvider.OAuth2Context context) {
        if (!isEnabled()) {
            throw new IllegalStateException("GitLab Authentication is disabled");
        }
        ServiceBuilder serviceBuilder = new ServiceBuilder().provider(new GitLabOAuthApi(gitLabConfiguration.url())).apiKey(gitLabConfiguration.applicationId()).apiSecret(gitLabConfiguration.secret())
                .grantType(OAuthConstants.AUTHORIZATION_CODE).callback(context.getCallbackUrl());
        if (gitLabConfiguration.scope() != null && !GitLabAuthPlugin.NONE_SCOPE.equals(gitLabConfiguration.scope())) {
            serviceBuilder.scope(gitLabConfiguration.scope());
        }
        return serviceBuilder;
    }
}
