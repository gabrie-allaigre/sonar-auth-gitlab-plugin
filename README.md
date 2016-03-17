Sonar GitLab Plugin
===================

Fork to https://github.com/SonarSource/sonar-auth-github

# Goal

Add GitLab OAuth login in login page.

# Usage

For add plugin in SonarQube :

- Download last version http://nexus.talanlabs.com/service/local/repo_groups/public_release/content/com/synaptix/sonar-auth-gitlab-plugin/1.0.0/sonar-gitlab-plugin-1.0.0.jar
- Copy file in extensions directory `SONARQUBE_HOME/extensions/plugins`
- Restart SonarQube 

# Configuration

- Administration : **Settings** globals in SonarQube

| Variable | Comment | Type |
| -------- | ----------- | ---- |
| sonar.auth.gitlab.enabled | Enable Gitlab users to login. Value is ignored if client ID and secret are not defined |
| sonar.auth.gitlab.url | URL to access GitLab | 
| sonar.auth.gitlab.applicationId | Application ID provided by GitLab when registering the application |
| sonar.auth.gitlab.secret | Token of the user who can make reports on the project, either global or per project |
| sonar.auth.gitlab.allowUsersToSignUp | Allow new users to authenticate. When set to 'false', only existing users will be able to authenticate to the server |

