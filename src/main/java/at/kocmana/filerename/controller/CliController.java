package at.kocmana.filerename.controller;

import at.kocmana.filerename.model.CommandLineArguments;
import at.kocmana.filerename.service.FileRenameTask;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true, versionProvider = VersionInformationController.class)
public class CliController implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(CliController.class);

  @ArgGroup(exclusive = false, multiplicity = "1..*")
  List<CliArgument> cliArguments;

  static class CliArgument {
    @Option(names = {"-p", "--path"},
        description = "The directory for the operation")
    private Path path = Paths.get(".");

    @Option(names = {"-r", "--recursive"},
        description = "Include sub directories.", defaultValue = "false")
    private boolean recursive = false;

    @Option(names = {"-i", "--input"}, required = true,
        description = "The pattern of the input file names")
    private String inputTemplate;

    @Option(names = {"-o", "--output"}, required = true,
        description = "The pattern of the output file names")
    private String outputTemplate;

    @Option(names = {"-d", "--dryRun"},
        description = "Setting this parameter will only display how the file names will be changed without " +
            "performing any changes", defaultValue = "false")
    private boolean dryRun = false;

    @Option(names = {"-cp", "--copy"},
        description = "Define the operation to be performed. If set, files will be copied instead of renamed.",
        defaultValue = "false")
    private boolean createCopy = false;
  }

  @Override
  public void run() {
    var arguments = mapArguments();
    try {
      var completableFutures = arguments.stream()
          .map(FileRenameTask::new)
          .map(task -> CompletableFuture.supplyAsync(task::call))
          .toList();

      var allTasksFutures = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));

      allTasksFutures.get();

      log.info("Process finished.");
    } catch (Exception exception) {
      log.error("Encountered issue while running the application: {}.", exception.getMessage(), exception);
    }
  }

  List<CommandLineArguments> mapArguments() {
    return cliArguments.stream()
        .map(args -> new CommandLineArguments(args.path, args.recursive, args.inputTemplate, args.outputTemplate,
            args.dryRun, args.createCopy))
        .toList();
  }

  public void run(String[] args) {
    var exitCode = new CommandLine(new CliController()).execute(args);
    System.exit(exitCode);
  }

}
