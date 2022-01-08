package at.kocmana.filerename.controller;

import picocli.CommandLine.IVersionProvider;

import java.util.Properties;

final class VersionInformationController implements IVersionProvider {

  public VersionInformationController() {
    // public no args constructor is required by the picocli framework
  }

  @Override
  public String[] getVersion() throws Exception {
    final Properties properties = new Properties();
    properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));

    var applicationName = properties.getProperty("applicationName");
    var version = properties.getProperty("version");

    var versionInformation = String.format("%s Version %s", applicationName, version);
    return new String[]{versionInformation};
  }
}
