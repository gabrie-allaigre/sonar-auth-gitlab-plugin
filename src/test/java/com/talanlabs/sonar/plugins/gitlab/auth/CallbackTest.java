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

import javax.servlet.http.HttpServletRequest;

public class CallbackTest {

    @Rule
    public MockWebServer gitlab = new MockWebServer();

    @Test
    public void testCallback() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn(String.format("http://%s:%d", gitlab.getHostName(), gitlab.getPort()));
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.CallbackContext callbackContext = Mockito.mock(OAuth2IdentityProvider.CallbackContext.class);
        Mockito.when(callbackContext.getCallbackUrl()).thenReturn("http://server/callback");

        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(httpServletRequest.getParameter("code")).thenReturn("789");

        Mockito.when(callbackContext.getRequest()).thenReturn(httpServletRequest);

        gitlab.enqueue(new MockResponse().setBody(
                "{\n" + " \"access_token\": \"de6780bc506a0446309bd9362820ba8aed28aa506c71eedbe1c5c4f9dd350e54\",\n" + " \"token_type\": \"bearer\",\n" + " \"expires_in\": 7200,\n"
                        + " \"refresh_token\": \"8257e65c97202ed1726cf9571600918f3bffb2544b26e00a61df9897668c33a1\"\n" + "}"));
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
}
