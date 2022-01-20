package at.kocmana.filerename.service;

import static at.kocmana.filerename.controller.CliController.CollisionResolutionStrategy.ENUMERATE;

import at.kocmana.filerename.model.JobArguments;
import at.kocmana.filerename.service.transformation.helper.FileEnumerater;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRenameJob implements Callable<FileRenameJob.JobStatus> {

  private static final Logger log = LoggerFactory.getLogger(FileRenameJob.class);

  private final JobArguments jobArguments;
  private volatile JobStatus jobStatus = JobStatus.CREATED;

  private String outputFileName;
  private FileEnumerater fileEnumerater;

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
    fileEnumerater = FileEnumerater.forFilename(outputFileName);
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
    var filename = outputFileName;
    do {
      try {
        performFileOperation(filename);
        jobStatus = JobStatus.SUCCESS;
      } catch (FileAlreadyExistsException exception) {
        if (jobArguments.collisionResolutionStrategy() == ENUMERATE) {
          filename = fileEnumerater.enumerateFilename();
        } else {
          failWithException(exception);
        }
      } catch (Exception exception) {
        failWithException(exception);
      }
    } while (jobArguments.collisionResolutionStrategy() == ENUMERATE && !jobStatus.isSuccessful());
  }

  private void performFileOperation(String filename) throws IOException {
    var outputPath = jobArguments.inputFile().resolveSibling(filename);
    if (jobArguments.createCopy()) {
      Files.copy(jobArguments.inputFile(), outputPath);
    } else {
      Files.move(jobArguments.inputFile(), outputPath);
    }
  }

  private void failWithException(Exception exception) {
    var operation = jobArguments.createCopy() ? "copy" : "rename";
    log.warn("Could not {} file {} to {}: {}.",
        operation, jobArguments.inputFile().getFileName(), outputFileName, exception.getMessage());
    jobStatus = JobStatus.FAILED;
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
    CREATED, READY, RUNNING, SUCCESS, FAILED;

    public boolean isSuccessful() {
      return this == JobStatus.SUCCESS;
    }
  }

}
