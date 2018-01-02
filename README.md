Sonar Auth GitLab Plugin
==============================

[![https://travis-ci.org/gabrie-allaigre/sonar-auth-gitlab-plugin](https://api.travis-ci.org/gabrie-allaigre/sonar-auth-gitlab-plugin.png?branch=master)](https://travis-ci.org/gabrie-allaigre/sonar-auth-gitlab-plugin)

Inspired by https://github.com/SonarSource/sonar-auth-github

**The version 1.2.2 is directly in the SonarQube update center**

**Version 1.3.0-rc1**

- Add default v4 api
- Add sync groups option with GitLab
- Add custom groups for user
- Add exception for few users

Download 1.3.0-rc1 version https://github.com/gabrie-allaigre/sonar-auth-gitlab-plugin/releases/download/1.3.0-rc1/sonar-auth-gitlab-plugin-1.3.0-rc1.jar

**Version 1.2.2**

- Add option to change scope for GitLab (since 9.2, scope is api)

Download 1.2.2 version https://github.com/gabrie-allaigre/sonar-auth-gitlab-plugin/releases/download/1.2.2/sonar-auth-gitlab-plugin-1.2.2.jar

# Goal

Enables user authentication and Single Sign-On via GitLab.

Uses GitLab OAuth login in SonarQube login page.

![Signin](doc/sonar_signin.jpg)

# Usage

For SonarQube >=5.6:

- Download last version https://github.com/gabrie-allaigre/sonar-auth-gitlab-plugin/releases/download/1.2.2/sonar-auth-gitlab-plugin-1.2.2.jar
- Copy file in extensions directory `SONARQUBE_HOME/extensions/plugins`
- Restart SonarQube 

For SonarQube >=5.4:

- Download last version https://github.com/gabrie-allaigre/sonar-auth-gitlab-plugin/releases/download/1.0.0/sonar-auth-gitlab-plugin-1.0.0.jar
- Copy file in extensions directory `SONARQUBE_HOME/extensions/plugins`
- Restart SonarQube 

**Other Plugin: [Add Reporting in GitLab commit](https://github.com/gabrie-allaigre/sonar-gitlab-plugin)**

# Configuration

**Warning : In SonarQube, must have `Server base URL` with HTTPS**

- In GitLab, create Application OAuth : Admin Settings -> **Application**

Fill name SonarQube and fill redirect URI with 'https://mygitlab.com/oauth2/callback/gitlab' (replace url).

![Gitlab Add](doc/gitlab_add.jpg)

Copy Application Id and Secret in Settings of Sonarqube.

![Gitlab App](doc/gitlab_app.jpg)

- In SonarQube: Administration -> General Settings -> GitLab -> **Authentication**

![Sonar Settings](doc/sonar_admin.jpg)

| Variable | Comment | Type |
| -------- | ----------- | ---- |
| sonar.auth.gitlab.enabled | Enable GitLab users to login. Value is ignored if client ID and secret are not defined |
| sonar.auth.gitlab.url | URL to access GitLab | 
| sonar.auth.gitlab.applicationId | Application ID provided by GitLab when registering the application |
| sonar.auth.gitlab.secret | Token of the user who can make reports on the project, either global or per project |
| sonar.auth.gitlab.allowUsersToSignUp | Allow new users to authenticate. When set to 'false', only existing users will be able to authenticate to the server |
| sonar.auth.gitlab.scope | Scope provided by GitLab when access user info, either global or per project, default read_user |
| sonar.auth.gitlab.groups | Set groups, use , for multi |
| sonar.auth.gitlab.sync_user_groups | Enable synchronization between GitLab and SonarQube groups. SonarQube users groups will be the same as GitLab's |
| sonar.auth.gitlab.groups | Defaults groups, which will be added to SonarQube default group and syncs groups if enabled |
| sonar.auth.gitlab.api_version | GitLab API version |
| sonar.auth.gitlab.user_exceptions | List of usernames to keep intact (e.g. admin list), use , for multi |

# Sonarqube

https://sonarcloud.io/dashboard?id=com.talanlabs%3Asonar-auth-gitlab-plugin
