package at.kocmana.filerename.service;

import at.kocmana.filerename.model.JobArguments;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  @Override
  public JobStatus call() {
    outputFileName = jobArguments.outputTemplate();
    for (var transformationRule : jobArguments.transformationRules()) {
      outputFileName = transformationRule.apply(jobArguments.inputFile(), outputFileName);
    }

    jobStatus = JobStatus.READY;
    log.info(this.toString());

    jobStatus = JobStatus.RUNNING;
    if (!jobArguments.dryRun()) {
      performFileOperation();
    }
    return jobStatus;
  }

  private void performFileOperation() {
    try {
      if (jobArguments.createCopy()) {
        Files.copy(jobArguments.inputFile(), jobArguments.inputFile().resolveSibling(outputFileName));
      } else {
        Files.move(jobArguments.inputFile(), jobArguments.inputFile().resolveSibling(outputFileName));
      }
      jobStatus = JobStatus.SUCCESS;
    } catch (Exception exception) {
      var operation = jobArguments.createCopy() ? "copy" : "rename";
      log.error("Could not {} filename from {} to {}: {}",
          operation, jobArguments.inputFile().getFileName(), outputFileName, exception.getMessage());
      jobStatus = JobStatus.FAILED;
    }
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
