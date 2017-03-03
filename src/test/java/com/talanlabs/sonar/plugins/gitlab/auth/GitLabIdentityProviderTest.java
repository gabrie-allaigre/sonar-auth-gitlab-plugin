package com.talanlabs.sonar.plugins.gitlab.auth;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonar.api.server.authentication.Display;
import org.sonar.api.server.authentication.OAuth2IdentityProvider;

public class GitLabIdentityProviderTest {

    @Test
    public void testFields() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        Assertions.assertThat(gitLabIdentityProvider.getKey()).isEqualTo("gitlab");
        Assertions.assertThat(gitLabIdentityProvider.getName()).isEqualTo("GitLab");
        Display display = gitLabIdentityProvider.getDisplay();
        Assertions.assertThat(display.getIconPath()).isEqualTo("/static/authgitlab/gitlab.svg");
        Assertions.assertThat(display.getBackgroundColor()).isEqualTo("#333c47");
        Assertions.assertThat(gitLabIdentityProvider.isEnabled()).isTrue();
        Assertions.assertThat(gitLabIdentityProvider.allowsUsersToSignUp()).isTrue();
    }

    @Test
    public void testInit() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn("http://server");
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.InitContext initContext = Mockito.mock(OAuth2IdentityProvider.InitContext.class);
        Mockito.when(initContext.getCallbackUrl()).thenReturn("http://server/callback");

        gitLabIdentityProvider.init(initContext);

        Mockito.verify(initContext).redirectTo("http://server/oauth/authorize?client_id=123&redirect_uri=http%3A%2F%2Fserver%2Fcallback&response_type=code&scope=read_user");
    }
}
