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
        GitLabApi gitLabApi = new GitLabApi("http://server");

        Assertions.assertThat(gitLabApi.getAccessTokenEndpoint()).isEqualTo("http://server/oauth/token");
        Assertions.assertThat(gitLabApi.getAccessTokenVerb()).isEqualTo(Verb.POST);
        Assertions.assertThat(gitLabApi.getAccessTokenExtractor()).isInstanceOf(JsonTokenExtractor.class);
    }

    @Test
    public void testUrl() {
        GitLabApi gitLabApi = new GitLabApi("http://server");

        OAuthConfig oAuthConfig = Mockito.mock(OAuthConfig.class);
        Mockito.when(oAuthConfig.getCallback()).thenReturn("http://server");
        Mockito.when(oAuthConfig.hasScope()).thenReturn(true);
        Mockito.when(oAuthConfig.getScope()).thenReturn("read_user");
        Mockito.when(oAuthConfig.getApiKey()).thenReturn("123");
        Assertions.assertThat(gitLabApi.getAuthorizationUrl(oAuthConfig)).isEqualTo("http://server/oauth/authorize?client_id=123&redirect_uri=http%3A%2F%2Fserver&response_type=code&scope=read_user");
    }
}
