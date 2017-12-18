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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import org.sonar.api.config.Settings;
import org.sonar.api.server.ServerSide;

@ServerSide
public class GitLabConfiguration {

    private final Settings settings;

    public GitLabConfiguration(Settings settings) {
        this.settings = settings;
    }

    @CheckForNull
    public String url() {
        return settings.getString(GitLabAuthPlugin.GITLAB_AUTH_URL);
    }

    @CheckForNull
    public String applicationId() {
        return settings.getString(GitLabAuthPlugin.GITLAB_AUTH_APPLICATIONID);
    }

    @CheckForNull
    public String secret() {
        return settings.getString(GitLabAuthPlugin.GITLAB_AUTH_SECRET);
    }

    public String scope() {
        return settings.getString(GitLabAuthPlugin.GITLAB_AUTH_SCOPE);
    }

    public boolean isEnabled() {
        return settings.getBoolean(GitLabAuthPlugin.GITLAB_AUTH_ENABLED) && applicationId() != null && secret() != null;
    }

    public boolean allowUsersToSignUp() {
        return settings.getBoolean(GitLabAuthPlugin.GITLAB_AUTH_ALLOWUSERSTOSIGNUP);
    }

    public String groups() {
        return settings.getString(GitLabAuthPlugin.GITLAB_AUTH_GROUPS);
    }

    public boolean syncUserGroups() {
        return settings.getBoolean(GitLabAuthPlugin.GITLAB_AUTH_SYNC_USER_GROUPS);
    }

    public String apiVersion() {
        return settings.getString(GitLabAuthPlugin.GITLAB_AUTH_API_VERSION);
    }

    public List<String> userExceptions() {
        String exceptions = settings.getString(GitLabAuthPlugin.GITLAB_AUTH_USER_EXCEPTIONS);
        return exceptions != null ? Stream.of(exceptions.split(",")).map(String::trim).collect(Collectors.toList()) : Collections.emptyList();
    }
}
