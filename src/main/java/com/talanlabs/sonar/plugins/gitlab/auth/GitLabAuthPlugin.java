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

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.valueOf;
import static org.sonar.api.PropertyType.BOOLEAN;

public class GitLabAuthPlugin implements Plugin {

    public static final String GITLAB_AUTH_ENABLED = "sonar.auth.gitlab.enabled";
    public static final String GITLAB_AUTH_URL = "sonar.auth.gitlab.url";
    public static final String GITLAB_AUTH_APPLICATIONID = "sonar.auth.gitlab.applicationId";
    public static final String GITLAB_AUTH_SECRET = "sonar.auth.gitlab.secret";
    public static final String GITLAB_AUTH_ALLOWUSERSTOSIGNUP = "sonar.auth.gitlab.allowUsersToSignUp";

    public static final String CATEGORY = "gitlab";
    public static final String SUBCATEGORY = "authentication";

    static List<PropertyDefinition> definitions() {
        return Arrays.asList(PropertyDefinition.builder(GITLAB_AUTH_ENABLED).name("Enabled").description("Enable Gitlab users to login. Value is ignored if client ID and secret are not defined.")
                        .category(CATEGORY).subCategory(SUBCATEGORY).type(BOOLEAN).defaultValue(valueOf(false)).index(1).build(),
                PropertyDefinition.builder(GITLAB_AUTH_URL).name("GitLab url").description("URL to access GitLab.").category(CATEGORY).subCategory(SUBCATEGORY).defaultValue("https://gitlab.com")
                        .index(2).build(),
                PropertyDefinition.builder(GITLAB_AUTH_APPLICATIONID).name("Application ID").description("Application ID provided by GitLab when registering the application.").category(CATEGORY)
                        .subCategory(SUBCATEGORY).index(3).build(),
                PropertyDefinition.builder(GITLAB_AUTH_SECRET).name("Secret").description("Secret provided by GitLab when registering the application.").category(CATEGORY).subCategory(SUBCATEGORY)
                        .type(PropertyType.PASSWORD).index(4).build(), PropertyDefinition.builder(GITLAB_AUTH_ALLOWUSERSTOSIGNUP).name("Allow users to sign-up")
                        .description("Allow new users to authenticate. When set to 'false', only existing users will be able to authenticate to the server.").category(CATEGORY)
                        .subCategory(SUBCATEGORY).type(BOOLEAN).defaultValue(valueOf(true)).index(5).build());
    }

    @Override
    public void define(Context context) {
        context.addExtensions(GitLabConfiguration.class, GitLabIdentityProvider.class).addExtensions(definitions());
    }
}
