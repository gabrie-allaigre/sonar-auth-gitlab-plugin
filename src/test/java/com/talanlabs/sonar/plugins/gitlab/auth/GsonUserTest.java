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
import org.junit.Test;

public class GsonUserTest {

    @Test
    public void testParse() {
        GsonUser gsonUser = GsonUser.parse("{ \"username\":\"toto\",\"name\":\"Toto Toto\",\"email\":\"toto@toto.com\"}");

        Assertions.assertThat(gsonUser).isNotNull();
        Assertions.assertThat(gsonUser.getUsername()).isEqualTo("toto");
        Assertions.assertThat(gsonUser.getName()).isEqualTo("Toto Toto");
        Assertions.assertThat(gsonUser.getEmail()).isEqualTo("toto@toto.com");
    }
}
