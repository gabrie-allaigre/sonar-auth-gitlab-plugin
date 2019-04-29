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

import com.github.scribejava.core.model.Verb;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class GitLabApiTest {
    @Rule
    public MockWebServer gitlab = new MockWebServer();

    @Test
    public void testFields() {

        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.url()).thenReturn("http://server");
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.scope()).thenReturn("read_user");

        GitLabOAuthApi gitLabOAuthApi = new GitLabOAuthApi(configuration);

        Assertions.assertThat(gitLabOAuthApi.getAccessTokenEndpoint()).isEqualTo("http://server/oauth/token");
        Assertions.assertThat(gitLabOAuthApi.getAccessTokenVerb()).isEqualTo(Verb.POST);
    }

    @Test
    public void testUrl() {
        GitLabConfiguration configuration = Mockito.mock(GitLabConfiguration.class);
        Mockito.when(configuration.isEnabled()).thenReturn(true);
        Mockito.when(configuration.allowUsersToSignUp()).thenReturn(true);
        Mockito.when(configuration.applicationId()).thenReturn("123");
        Mockito.when(configuration.secret()).thenReturn("456");
        Mockito.when(configuration.url()).thenReturn("http://server");
        Mockito.when(configuration.scope()).thenReturn("read_user");

        GitLabOAuthApi gitLabOAuthApi = new GitLabOAuthApi(configuration);

        Assertions.assertThat(gitLabOAuthApi.getAuthorizationUrl("code", configuration.applicationId(), configuration.url(), configuration.scope(), "state", null))
            .isEqualTo("http://server/oauth/authorize?response_type=code&client_id=123&redirect_uri=http%3A%2F%2Fserver&scope=read_user&state=state");
    }
}
