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

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sonar.api.server.authentication.OAuth2IdentityProvider;
import org.sonar.api.server.authentication.UserIdentity;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CallbackTest {

    @Rule
    public MockWebServer gitlab = new MockWebServer();

    @Test
    public void testCallbackSuccess() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn(String.format("http://%s:%d", gitlab.getHostName(), gitlab.getPort()));
        Mockito.when(configuration.scope()).thenReturn("read_user");
        Mockito.when(configuration.groupAllowed()).thenReturn(0);

        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.CallbackContext callbackContext = Mockito.mock(OAuth2IdentityProvider.CallbackContext.class);
        Mockito.when(callbackContext.getCallbackUrl()).thenReturn("http://server/callback");

        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getParameter("code")).thenReturn("789");

        Mockito.when(callbackContext.getRequest()).thenReturn(httpServletRequest);

        gitlab.enqueue(new MockResponse().setBody(
                "{\n" + " \"access_token\": \"de6780bc506a0446309bd9362820ba8aed28aa506c71eedbe1c5c4f9dd350e54\",\n" + " \"token_type\": \"bearer\",\n" + " \"expires_in\": 7200,\n" + " \"refresh_token\": \"8257e65c97202ed1726cf9571600918f3bffb2544b26e00a61df9897668c33a1\"\n" + "}"));
        gitlab.enqueue(new MockResponse().setBody("{\"username\":\"toto\", \"name\":\"Toto Toto\",\"email\":\"toto@toto.com\"}"));

        gitLabIdentityProvider.callback(callbackContext);

        ArgumentCaptor<UserIdentity> argument = ArgumentCaptor.forClass(UserIdentity.class);
        Mockito.verify(callbackContext).authenticate(argument.capture());
        Assertions.assertThat(argument.getValue()).isNotNull();
        Assertions.assertThat(argument.getValue().getProviderLogin()).isEqualTo("toto");
        Assertions.assertThat(argument.getValue().getLogin()).isEqualTo("toto");
        Assertions.assertThat(argument.getValue().getName()).isEqualTo("Toto Toto");
        Assertions.assertThat(argument.getValue().getEmail()).isEqualTo("toto@toto.com");
        Mockito.verify(callbackContext).redirectToRequestedPage();
    }

    @Test
    public void testCallbackFail() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn(String.format("http://%s:%d", gitlab.getHostName(), gitlab.getPort()));
        Mockito.when(configuration.scope()).thenReturn(GitLabAuthPlugin.NONE_SCOPE);
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.CallbackContext callbackContext = Mockito.mock(OAuth2IdentityProvider.CallbackContext.class);
        Mockito.when(callbackContext.getCallbackUrl()).thenReturn("http://server/callback");

        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getParameter("code")).thenReturn("789");

        Mockito.when(callbackContext.getRequest()).thenReturn(httpServletRequest);

        gitlab.enqueue(new MockResponse().setBody(
                "{\n" + " \"access_token\": \"de6780bc506a0446309bd9362820ba8aed28aa506c71eedbe1c5c4f9dd350e54\",\n" + " \"token_type\": \"bearer\",\n" + " \"expires_in\": 7200,\n"
                        + " \"refresh_token\": \"8257e65c97202ed1726cf9571600918f3bffb2544b26e00a61df9897668c33a1\"\n" + "}"));
        gitlab.enqueue(new MockResponse().setResponseCode(404).setBody("empty"));

        Assertions.assertThatThrownBy(() -> gitLabIdentityProvider.callback(callbackContext)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fail to authenticate the user. Error code is 404, Body of the response is empty");
    }

    // test with group synchronization using api V3
    @Test
    public void testCallbackSuccessWithGroupV3() {
        testSuccessWithGroups(GitLabAuthPlugin.V3_API_VERSION, "testV3", Collections.singleton("group"));
    }

    // test with group synchronization using api V4
    @Test
    public void testCallbackSuccessWithGroupV4() {
        testSuccessWithGroups(GitLabAuthPlugin.V4_API_VERSION, "testV4", Collections.singleton("group"));
    }

    @Test
    public void testCallbackAPIExceptionWithGroupV4() {
        testSuccessWithGroups(GitLabAuthPlugin.V4_API_VERSION, null, Collections.singleton("group"));
    }

    @Test
    public void testCallbackUnknownVersion() {
        testSuccessWithGroups("unknownVersion", null, Collections.emptySet());
    }

    @Test
    public void testCallbackUnknownVersionAndEmptyGroup() {
        testSuccessWithGroups("unknownVersion", null, Collections.emptySet());
    }

    @Test
    public void testCallbackUnknownVersionAndUseException() {
        testSuccessWithGroupsAndException(GitLabAuthPlugin.V4_API_VERSION, "testV4", Collections.singleton("group"), true);
    }

    private void testSuccessWithGroups(String apiVersion, @Nullable String groupTestName, Set<String> defaultGroup) {
        testSuccessWithGroupsAndException(apiVersion, groupTestName, defaultGroup, false);
    }

    private void testSuccessWithGroupsAndException(String apiVersion, @Nullable String groupTestName, Set<String> defaultGroup, boolean useException) {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn(String.format("http://%s:%d", gitlab.getHostName(), gitlab.getPort()));
        Mockito.when(configuration.syncUserGroups()).thenReturn(true);
        Mockito.when(configuration.groups()).thenReturn(defaultGroup);
        Mockito.when(configuration.apiVersion()).thenReturn(apiVersion);
        Mockito.when(configuration.groupAllowed()).thenReturn(0);
        if (useException) {
            Mockito.when(configuration.userExceptions()).thenReturn(Collections.singleton("username"));
        }

        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.CallbackContext callbackContext = Mockito.mock(OAuth2IdentityProvider.CallbackContext.class);
        Mockito.when(callbackContext.getCallbackUrl()).thenReturn("http://server/callback");

        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getParameter("code")).thenReturn("789");

        Mockito.when(callbackContext.getRequest()).thenReturn(httpServletRequest);

        gitlab.enqueue(new MockResponse().setBody(
                "{\n" + " \"access_token\": \"de6780bc506a0446309bd9362820ba8aed28aa506c71eedbe1c5c4f9dd350e54\",\n" + " \"token_type\": \"bearer\",\n" + " \"expires_in\": 7200,\n" + " \"refresh_token\": \"8257e65c97202ed1726cf9571600918f3bffb2544b26e00a61df9897668c33a1\"\n" + "}"));
        gitlab.enqueue(new MockResponse().setBody("{ \"username\": \"username\", \"name\": \"name\", \"email\": \"email\"}"));
        if (groupTestName == null) {
            gitlab.enqueue(new MockResponse().setBody(""));
        } else {
            gitlab.enqueue(new MockResponse().setBody(String.format("[{\"name\": \"%s\"}]", groupTestName)));
        }

        gitLabIdentityProvider.callback(callbackContext);

        ArgumentCaptor<UserIdentity> captor = ArgumentCaptor.forClass(UserIdentity.class);
        Mockito.verify(callbackContext).authenticate(captor.capture());
        UserIdentity value = captor.getValue();
        Assertions.assertThat(value.getName()).isEqualTo("name");
        Assertions.assertThat(value.getLogin()).isEqualTo("username");
        if (defaultGroup == null || useException) {
            Assertions.assertThat(value.getGroups()).isEmpty();
        } else if (groupTestName != null) {
            Assertions.assertThat(value.getGroups()).isEqualTo(new HashSet<>(Arrays.asList(groupTestName, "group")));
        }
        Assertions.assertThat(value.getEmail()).isEqualTo("email");
    }
}
