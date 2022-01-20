package at.kocmana.filerename.service;

import at.kocmana.filerename.model.JobArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static at.kocmana.filerename.controller.CliController.CollisionResolutionStrategy.ENUMERATE;

public class FileRenameJob implements Callable<FileRenameJob.JobStatus> {

  private static final Logger log = LoggerFactory.getLogger(FileRenameJob.class);
  private final JobArguments jobArguments;
  private volatile JobStatus jobStatus = JobStatus.CREATED;

  private String outputFileName;

  public FileRenameJob(JobArguments arguments) {
    this.jobArguments = arguments;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  public JobArguments getJobArguments() {
    return jobArguments;
  }

  public JobStatus prepare() {
    jobStatus = JobStatus.RUNNING;
    outputFileName = jobArguments.outputTemplate();
    for (var transformationRule : jobArguments.transformationRules()) {
      outputFileName = transformationRule.apply(jobArguments.inputFile(), outputFileName);
    }
    jobStatus = JobStatus.READY;
    return jobStatus;
  }

  @Override
  public JobStatus call() {
    jobStatus = JobStatus.RUNNING;
    if (!jobArguments.dryRun()) {
      executeFileTask();
    } else {
      jobStatus = JobStatus.SUCCESS;
    }
    return jobStatus;
  }

  private void executeFileTask() {
    var isSuccess = false;
    int currentEnumerator = 1;
    String filename = outputFileName;
    do {
      try {
        performFileOperation(filename);
        isSuccess = true;
        jobStatus = JobStatus.SUCCESS;
      } catch (FileAlreadyExistsException exception) {
        var operation = jobArguments.createCopy() ? "copy" : "rename";
        log.warn("Could not {} file {} to {} - This file already exists.",
                operation, jobArguments.inputFile().getFileName(), outputFileName, exception);
        outputFileName = enumerateFilename(outputFileName, currentEnumerator++);
        isSuccess = false;
        jobStatus = JobStatus.FAILED;
      } catch (IOException exception) {
        var operation = jobArguments.createCopy() ? "copy" : "rename";
        log.warn("Could not {} file {} to {}: {}.",
                operation, jobArguments.inputFile().getFileName(), outputFileName, exception.getMessage(), exception);      }
    } while (jobArguments.collisionResolutionStrategy() == ENUMERATE && !isSuccess);
  }

  private void performFileOperation(String filename) throws IOException {
    if (jobArguments.createCopy()) {
      Files.copy(jobArguments.inputFile(), jobArguments.inputFile().resolveSibling(filename));
    } else {
      Files.move(jobArguments.inputFile(), jobArguments.inputFile().resolveSibling(filename));
    }
  }

  private String enumerateFilename(String filename, int number) {
    StringBuffer buf = new StringBuffer(filename);
    var pattern = Pattern.compile("(?<filename>.+)(?<dot>\\.)(?<fileSuffix>.+)");
    var matcher = pattern.matcher(filename);
    log.info(matcher.find() ? "Found" : "not found");
    buf.replace(matcher.start("dot"), matcher.end("dot"), "-" + number + ".");
    log.info("New Filename: {}", buf);
    return buf.toString();
  }

  @Override
  public String toString() {
    var inputFilenameRepresentation = jobArguments.inputFile().getFileName().toString();
    var outputFilenameRepresentation = outputFileName != null ? outputFileName : "currently undefined";

    return String.format("%s -> %s: %s",
            inputFilenameRepresentation, outputFilenameRepresentation,
            jobStatus);
  }

  public enum JobStatus {
    CREATED, READY, RUNNING, SUCCESS, FAILED
  }

}
