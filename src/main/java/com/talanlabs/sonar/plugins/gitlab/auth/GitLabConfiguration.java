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

import org.sonar.api.config.Configuration;
import org.sonar.api.server.ServerSide;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.api.utils.System2;

@ServerSide
public class GitLabConfiguration {

    private final Configuration configuration;
    private final System2 system2;

    public GitLabConfiguration(Configuration configuration, System2 system2) {
        super();

        this.configuration = configuration;
        this.system2 = system2;
    }

    @CheckForNull
    public String url() {
        return configuration.get(GitLabAuthPlugin.GITLAB_AUTH_URL).orElse(null);
    }

    @CheckForNull
    public String applicationId() {
        return configuration.get(GitLabAuthPlugin.GITLAB_AUTH_APPLICATIONID).orElse(null);
    }

    @CheckForNull
    public String secret() {
        return configuration.get(GitLabAuthPlugin.GITLAB_AUTH_SECRET).orElse(null);
    }

    public String scope() {
        return configuration.get(GitLabAuthPlugin.GITLAB_AUTH_SCOPE).orElse(null);
    }

    public boolean isEnabled() {
        if (applicationId() != null && secret() != null) {
            return configuration.getBoolean(GitLabAuthPlugin.GITLAB_AUTH_ENABLED)
                .orElse(false);
        }
        return false;
    }

    public boolean allowUsersToSignUp() {
        return configuration.getBoolean(GitLabAuthPlugin.GITLAB_AUTH_ALLOWUSERSTOSIGNUP).orElse(false);
    }

    public Set<String> groups() {
        String groups = configuration.get(GitLabAuthPlugin.GITLAB_AUTH_GROUPS).orElse(null);
        return groups != null ? Stream.of(groups.split(",")).map(String::trim).collect(Collectors.toSet()) : Collections.emptySet();
    }

    public boolean syncUserGroups() {
        return configuration.getBoolean(GitLabAuthPlugin.GITLAB_AUTH_SYNC_USER_GROUPS).orElse(false);
    }

    public String apiVersion() {
        return configuration.get(GitLabAuthPlugin.GITLAB_AUTH_API_VERSION).orElse(null);
    }

    public Set<String> userExceptions() {
        String exceptions = configuration.get(GitLabAuthPlugin.GITLAB_AUTH_USER_EXCEPTIONS).orElse(null);
        return exceptions != null ? Stream.of(exceptions.split(",")).map(String::trim).collect(Collectors.toSet()) : Collections.emptySet();
    }

    public boolean ignoreCertificate() {
        return configuration.getBoolean(GitLabAuthPlugin.GITLAB_AUTH_IGNORE_CERT).orElse(false);
    }
}
