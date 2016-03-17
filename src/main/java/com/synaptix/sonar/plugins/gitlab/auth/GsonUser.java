/*
 * SonarQube :: GitLab Auth Plugin
 * Copyright (C) 2016-2016 Talanlabs
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
package com.synaptix.sonar.plugins.gitlab.auth;

import com.google.gson.Gson;

/**
 * Lite representation of JSON response of GET https://gitlab.com/user
 */
public class GsonUser {
  private String username;
  private String name;
  private String email;

  public String getUsername() {
    return username;
  }

  public GsonUser setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getName() {
    return name;
  }

  public GsonUser setName(String name) {
    this.name = name;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public GsonUser setEmail(String email) {
    this.email = email;
    return this;
  }

  public static GsonUser parse(String json) {
    Gson gson = new Gson();
    return gson.fromJson(json, GsonUser.class);
  }
}
