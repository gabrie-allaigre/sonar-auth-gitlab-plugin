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
