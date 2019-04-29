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

import static java.util.Objects.requireNonNull;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.Verb;
import org.sonar.api.server.ServerSide;
import java.util.Map;

@ServerSide
public class GitLabOAuthApi extends DefaultApi20 {

    private final GitLabConfiguration configuration;

    public GitLabOAuthApi(GitLabConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return configuration.url() + "/oauth/token";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return configuration.url() + "/oauth/authorize";
    }

    @Override
    public String getAuthorizationUrl(String responseType, String apiKey, String callback, String scope, String state, Map<String, String> additionalParams) {
        requireNonNull(callback, "URL for callback should not be null.");
        requireNonNull(apiKey, "ApiKey should not be null.");
        requireNonNull(state, "State should not be null.");

        return super.getAuthorizationUrl(responseType, apiKey, callback, scope, state, additionalParams);
    }
}
