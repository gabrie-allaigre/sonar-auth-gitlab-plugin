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

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;

public class GitLabConfigurationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Settings settings;
    private GitLabConfiguration config;

    @Before
    public void prepare() {
        settings = new Settings(new PropertyDefinitions(GitLabAuthPlugin.definitions()));
        config = new GitLabConfiguration(settings);
    }

    @Test
    public void global() {
        Assertions.assertThat(config.url()).isEqualTo("https://gitlab.com");
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_URL, "https://gitlab.talanlabs.com/api");
        Assertions.assertThat(config.url()).isEqualTo("https://gitlab.talanlabs.com/api");

        Assertions.assertThat(config.isEnabled()).isFalse();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_ENABLED, "true");
        Assertions.assertThat(config.isEnabled()).isFalse();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_APPLICATIONID, "1234");
        Assertions.assertThat(config.isEnabled()).isFalse();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_SECRET, "5678");
        Assertions.assertThat(config.isEnabled()).isTrue();

        Assertions.assertThat(config.applicationId()).isEqualTo("1234");
        Assertions.assertThat(config.secret()).isEqualTo("5678");

        Assertions.assertThat(config.allowUsersToSignUp()).isTrue();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_ALLOWUSERSTOSIGNUP, "false");
        Assertions.assertThat(config.allowUsersToSignUp()).isFalse();

        Assertions.assertThat(config.scope()).isEqualTo(GitLabAuthPlugin.READ_USER_SCOPE);
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_SCOPE, GitLabAuthPlugin.API_SCOPE);
        Assertions.assertThat(config.scope()).isEqualTo(GitLabAuthPlugin.API_SCOPE);

        Assertions.assertThat(config.groups()).isEmpty();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_GROUPS, "group1,group2");
        Assertions.assertThat(config.groups()).contains("group1", "group2");

        Assertions.assertThat(config.syncUserGroups()).isFalse();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_SYNC_USER_GROUPS, true);
        Assertions.assertThat(config.syncUserGroups()).isTrue();

        Assertions.assertThat(config.apiVersion()).isEqualTo(GitLabAuthPlugin.V4_API_VERSION);
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_API_VERSION, GitLabAuthPlugin.V3_API_VERSION);
        Assertions.assertThat(config.apiVersion()).isEqualTo(GitLabAuthPlugin.V3_API_VERSION);

        Assertions.assertThat(config.userExceptions()).isEmpty();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_USER_EXCEPTIONS, "admin,guest");
        Assertions.assertThat(config.userExceptions()).containsExactly("admin", "guest");

        Assertions.assertThat(config.ignoreCertificate()).isFalse();
        settings.setProperty(GitLabAuthPlugin.GITLAB_AUTH_IGNORE_CERT, "true");
        Assertions.assertThat(config.ignoreCertificate()).isTrue();
    }
}
