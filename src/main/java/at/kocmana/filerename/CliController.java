package at.kocmana.filerename;

import at.kocmana.filerename.model.CommandLineArguments;
import at.kocmana.filerename.service.FileRenameTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.nio.file.Paths;


public class CliController implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(CliController.class);

  @Option(names = {"-p", "--path"},
          description = "The directory for the operation.")
  private Path path = Paths.get(".");

  @Option(names = {"-r", "--recursive"},
          description = "Include sub directories.", defaultValue = "false")
  private boolean recursive = false;

  @Option(names = {"-i", "--input"}, required = true,
          description = "The pattern of the input file names.")
  private String inputTemplate;

  @Option(names = {"-o", "--output"}, required = true,
          description = "The pattern of the output file names")
  private String outputTemplate;

  @Option(names = {"-d", "--dryRun"},
          description = "Setting this parameter will only display how the file names will be changed", defaultValue = "false")
  private boolean dryRun = false;

  @Override
  public void run() {
    var arguments = mapArguments();
    try {
      var taskSuccessful = new FileRenameTask(arguments).call();
    } catch (Exception e) {
      log.error("Encountered issue while running the application: {}", e.getMessage());
    }
  }

  CommandLineArguments mapArguments() {
    return new CommandLineArguments(path, recursive, inputTemplate, outputTemplate, dryRun);
  }

  public void run(String[] args) {
    var exitCode = new CommandLine(new CliController()).execute(args);
    System.exit(exitCode);
  }

}
