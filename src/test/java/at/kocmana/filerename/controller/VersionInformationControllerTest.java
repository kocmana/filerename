package at.kocmana.filerename.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VersionInformationControllerTest {

  @Test
  void testGetVersion() throws Exception {
    var underTest = new VersionInformationController();

    var versionInformation = underTest.getVersion();

    assertThat(versionInformation).hasSize(1);
    assertThat(versionInformation[0])
            .contains("version 1.1")
            .contains("foo bar");
  }
}