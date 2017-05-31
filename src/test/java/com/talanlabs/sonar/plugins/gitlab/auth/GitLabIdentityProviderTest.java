/*
 * SonarQube :: GitLab Auth Plugin
 * Copyright (C) 2016-2017 Talanlabs
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
    public void testInitSuccess1() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn("http://server");
        Mockito.when(configuration.scope()).thenReturn("");
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.InitContext initContext = Mockito.mock(OAuth2IdentityProvider.InitContext.class);
        Mockito.when(initContext.getCallbackUrl()).thenReturn("http://server/callback");

        gitLabIdentityProvider.init(initContext);

        Mockito.verify(initContext).redirectTo("http://server/oauth/authorize?client_id=123&redirect_uri=http%3A%2F%2Fserver%2Fcallback&response_type=code");
    }

    @Test
    public void testInitSuccess2() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn("http://server");
        Mockito.when(configuration.scope()).thenReturn(GitLabAuthPlugin.API_SCOPE);
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.InitContext initContext = Mockito.mock(OAuth2IdentityProvider.InitContext.class);
        Mockito.when(initContext.getCallbackUrl()).thenReturn("http://server/callback");

        gitLabIdentityProvider.init(initContext);

        Mockito.verify(initContext).redirectTo("http://server/oauth/authorize?client_id=123&redirect_uri=http%3A%2F%2Fserver%2Fcallback&response_type=code&scope=api");
    }

    @Test
    public void testInitSuccess3() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn("http://server");
        Mockito.when(configuration.scope()).thenReturn(GitLabAuthPlugin.READ_USER_SCOPE);
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.InitContext initContext = Mockito.mock(OAuth2IdentityProvider.InitContext.class);
        Mockito.when(initContext.getCallbackUrl()).thenReturn("http://server/callback");

        gitLabIdentityProvider.init(initContext);

        Mockito.verify(initContext).redirectTo("http://server/oauth/authorize?client_id=123&redirect_uri=http%3A%2F%2Fserver%2Fcallback&response_type=code&scope=read_user");
    }

    @Test
    public void testInitFail() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(false);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn("http://server");
        GitLabIdentityProvider gitLabIdentityProvider = new GitLabIdentityProvider(configuration);

        OAuth2IdentityProvider.InitContext initContext = Mockito.mock(OAuth2IdentityProvider.InitContext.class);
        Mockito.when(initContext.getCallbackUrl()).thenReturn("http://server/callback");

        Assertions.assertThatThrownBy(() -> gitLabIdentityProvider.init(initContext)).isInstanceOf(IllegalStateException.class).hasMessageContaining("GitLab Authentication is disabled");
    }
}
