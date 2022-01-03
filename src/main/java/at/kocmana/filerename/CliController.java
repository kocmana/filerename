package at.kocmana.filerename;

import at.kocmana.filerename.model.Arguments;
import java.nio.file.Path;
import java.nio.file.Paths;
import picocli.CommandLine;
import picocli.CommandLine.Option;


public class CliController implements Runnable {

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

  @Option(names = {"-d", "--dry"},
      description = "Setting this parameter will only display how the file names will be changed", defaultValue = "false")
  private boolean dryRun = false;

  @Override
  public void run() {
    var arguments = mapArguments();
    try {
      var taskSuccessfull = new FileRenameTask(arguments).call();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  Arguments mapArguments() {
    return new Arguments(path, recursive, inputTemplate, outputTemplate, dryRun);
  }

  public void run(String[] args) {
    var exitCode = new CommandLine(new CliController()).execute(args);
    System.exit(exitCode);
  }

}
