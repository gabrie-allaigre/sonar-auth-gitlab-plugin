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

import com.github.scribejava.core.extractors.JsonTokenExtractor;
import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.Verb;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

public class GitLabApiTest {

    @Test
    public void testFields() {
        GitLabOAuthApi gitLabOAuthApi = new GitLabOAuthApi("http://server");

        Assertions.assertThat(gitLabOAuthApi.getAccessTokenEndpoint()).isEqualTo("http://server/oauth/token");
        Assertions.assertThat(gitLabOAuthApi.getAccessTokenVerb()).isEqualTo(Verb.POST);
        Assertions.assertThat(gitLabOAuthApi.getAccessTokenExtractor()).isInstanceOf(JsonTokenExtractor.class);
    }

    @Test
    public void testUrl() {
        GitLabOAuthApi gitLabOAuthApi = new GitLabOAuthApi("http://server");

        OAuthConfig oAuthConfig = Mockito.mock(OAuthConfig.class);
        Mockito.when(oAuthConfig.getCallback()).thenReturn("http://server");
        Mockito.when(oAuthConfig.hasScope()).thenReturn(true);
        Mockito.when(oAuthConfig.getScope()).thenReturn("read_user");
        Mockito.when(oAuthConfig.getApiKey()).thenReturn("123");
        Assertions.assertThat(gitLabOAuthApi.getAuthorizationUrl(oAuthConfig)).isEqualTo("http://server/oauth/authorize?client_id=123&redirect_uri=http%3A%2F%2Fserver&response_type=code&scope=read_user");
    }
}
