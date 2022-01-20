package at.kocmana.filerename.service;

import at.kocmana.filerename.model.CommandLineArguments;
import at.kocmana.filerename.model.JobArguments;
import at.kocmana.filerename.service.transformation.TransformationRuleFactory;
import at.kocmana.filerename.service.transformation.rules.TransformationRule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRenameTask implements Callable<FileRenameTask.TaskStatus> {

  private static final Logger log = LoggerFactory.getLogger(FileRenameTask.class);
  private static final String LIST_LINE_BREAK = "\r\n\t";

  private final CommandLineArguments arguments;

  private TaskStatus taskStatus = TaskStatus.CREATED;
  private List<FileRenameJob> fileRenameJobs = Collections.emptyList();
  private List<TransformationRule> transformationRules = Collections.emptyList();

  public FileRenameTask(CommandLineArguments arguments) {
    this.arguments = arguments;
  }

  public TaskStatus getTaskStatus() {
    return taskStatus;
  }

  @Override
  public TaskStatus call() {
    transformationRules = TransformationRuleFactory.generateApplicableTransformationRules(arguments.inputTemplate(),
            arguments.outputTemplate());
    transformationRules.forEach(transformationRule -> log.info(transformationRule.toString()));
    var searchString = generateFileSearchPattern();
    generateRenameJobs(searchString);

    this.taskStatus = TaskStatus.RUNNING;

    try {
      fileRenameJobs.parallelStream()
              .forEach(FileRenameJob::prepare);
      log.info("The following operations will be performed: " + LIST_LINE_BREAK + "{}", fileRenameJobsToString());
      fileRenameJobs.parallelStream()
              .forEach(FileRenameJob::call);
      log.info("Completed. Result:" + LIST_LINE_BREAK + "{}", generateResultStatistics());
      this.taskStatus = TaskStatus.SUCCESS;
    } catch (Exception exception) {
      log.error("Could not finish task with arguments: {}: {}", arguments, exception.getMessage(), exception);
    }

    return taskStatus;
  }

  public String getStatus() {
    return fileRenameJobs.stream()
            .map(Objects::toString)
            .collect(Collectors.joining("\r\n"));
  }

  private String generateFileSearchPattern() {
    var searchString = arguments.inputTemplate();
    for (var transformationRule : transformationRules) {
      searchString = transformationRule.replaceTemplateWithSearchString(searchString);
    }
    return searchString;
  }

  private void generateRenameJobs(String searchString) {
    var filePattern = Pattern.compile(searchString);
    var searchPattern = filePattern.asPredicate();

    BiPredicate<Path, BasicFileAttributes> searchCriteria = (path, attributes) -> {
      var filename = path.getFileName().toString();
      return attributes.isRegularFile() && searchPattern.test(filename);
    };

    var searchDepth = arguments.recursive() ? Integer.MAX_VALUE : 1;
    try (var relevantFiles = Files.find(arguments.path(), searchDepth, searchCriteria)) {
      var files = relevantFiles.toList();
      log.info("Files matching provided input pattern:" + LIST_LINE_BREAK + "{}", matchingFilesToString(files));

      fileRenameJobs = files.stream()
              .map(file -> new JobArguments(file, transformationRules, arguments.outputTemplate(), arguments.dryRun(),
                      arguments.createCopy(), arguments.collisionResolutionStrategy()))
              .map(FileRenameJob::new)
              .toList();
    } catch (IOException exception) {
      failTask("Could not lookup files in directory {}: {}",
              arguments.path().toAbsolutePath().toString(),
              exception.getMessage());
    }
  }

  private String matchingFilesToString(List<Path> files) {
    return files.stream()
            .map(Path::toString)
            .collect(Collectors.joining(LIST_LINE_BREAK));
  }

  private String fileRenameJobsToString() {
    var action = arguments.createCopy() ? "COPY " : "MOVE ";

    return fileRenameJobs.stream()
        .map(FileRenameJob::toString)
        .map(fileRenameDescription -> action + fileRenameDescription)
        .collect(Collectors.joining(LIST_LINE_BREAK));
  }

  private String generateResultStatistics() {
    var status = fileRenameJobs.stream()
            .collect(Collectors.groupingBy(FileRenameJob::getJobStatus, Collectors.counting()));
    return status.keySet().stream().map(key -> String.format("%s: %d", key.toString(), status.get(key)))
            .collect(Collectors.joining(LIST_LINE_BREAK));
  }

  private void failTask(String errorMessage, Object... errorMessageArguments) {
    log.error(errorMessage, errorMessageArguments);
    taskStatus = TaskStatus.FAILURE;
  }

  public enum TaskStatus {
    CREATED, RUNNING, SUCCESS, FAILURE
  }

}
