/*
 * GitHub Authentication for SonarQube
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonarsource.auth.github;

import org.junit.Test;
import org.sonar.api.config.Settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonarsource.auth.github.GitHubSettings.LOGIN_STRATEGY_DEFAULT_VALUE;
import static org.sonarsource.auth.github.GitHubSettings.LOGIN_STRATEGY_PROVIDER_ID;

public class GitHubSettingsTest {

  Settings settings = new Settings();

  GitHubSettings underTest = new GitHubSettings(settings);

  @Test
  public void is_enabled() throws Exception {
    settings.setProperty("sonar.auth.github.clientId", "id");
    settings.setProperty("sonar.auth.github.clientSecret", "secret");
    settings.setProperty("sonar.auth.github.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    settings.setProperty("sonar.auth.github.enabled", true);
    assertThat(underTest.isEnabled()).isTrue();

    settings.setProperty("sonar.auth.github.enabled", false);
    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void is_enabled_always_return_false_when_client_id_is_null() throws Exception {
    settings.setProperty("sonar.auth.github.enabled", true);
    settings.setProperty("sonar.auth.github.clientId", (String) null);
    settings.setProperty("sonar.auth.github.clientSecret", "secret");
    settings.setProperty("sonar.auth.github.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void is_enabled_always_return_false_when_client_secret_is_null() throws Exception {
    settings.setProperty("sonar.auth.github.enabled", true);
    settings.setProperty("sonar.auth.github.clientId", "id");
    settings.setProperty("sonar.auth.github.clientSecret", (String) null);
    settings.setProperty("sonar.auth.github.loginStrategy", LOGIN_STRATEGY_DEFAULT_VALUE);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void is_enabled_always_return_false_when_login_strategy_is_null() throws Exception {
    settings.setProperty("sonar.auth.github.enabled", true);
    settings.setProperty("sonar.auth.github.clientId", "id");
    settings.setProperty("sonar.auth.github.clientSecret", "secret");
    settings.setProperty("sonar.auth.github.loginStrategy", (String) null);

    assertThat(underTest.isEnabled()).isFalse();
  }

  @Test
  public void return_client_id() throws Exception {
    settings.setProperty("sonar.auth.github.clientId", "id");
    assertThat(underTest.clientId()).isEqualTo("id");
  }

  @Test
  public void return_client_secret() throws Exception {
    settings.setProperty("sonar.auth.github.clientSecret", "secret");
    assertThat(underTest.clientSecret()).isEqualTo("secret");
  }

  @Test
  public void return_login_strategy() throws Exception {
    settings.setProperty("sonar.auth.github.loginStrategy", LOGIN_STRATEGY_PROVIDER_ID);
    assertThat(underTest.loginStrategy()).isEqualTo(LOGIN_STRATEGY_PROVIDER_ID);
  }

  @Test
  public void allow_users_to_sign_up() throws Exception {
    settings.setProperty("sonar.auth.github.allowUsersToSignUp", "true");
    assertThat(underTest.allowUsersToSignUp()).isTrue();

    settings.setProperty("sonar.auth.github.allowUsersToSignUp", "false");
    assertThat(underTest.allowUsersToSignUp()).isFalse();
  }

  @Test
  public void definitions() throws Exception {
    assertThat(GitHubSettings.definitions()).hasSize(5);
  }
}
