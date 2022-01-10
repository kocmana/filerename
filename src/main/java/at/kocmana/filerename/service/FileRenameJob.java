package at.kocmana.filerename.service;

import at.kocmana.filerename.model.JobArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.concurrent.Callable;

public class FileRenameJob implements Callable<FileRenameJob.JobStatus> {

  private static final Logger log = LoggerFactory.getLogger(FileRenameJob.class);
  private final JobArguments jobArguments;
  private volatile JobStatus jobStatus = JobStatus.CREATED;

  private String outputFileName;

  public FileRenameJob(JobArguments arguments) {
    this.jobArguments = arguments;
  }

  public JobStatus getJobStatus() {
    return  jobStatus;
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
      try {
        Files.move(jobArguments.inputFile(), jobArguments.inputFile().resolveSibling(outputFileName));
        jobStatus = JobStatus.SUCCESS;
      } catch (Exception exception) {
        log.error("Could not rename filename from {} to {}: {}",
                jobArguments.inputFile().getFileName(), outputFileName, exception.getMessage());
        jobStatus = JobStatus.FAILED;
      }
    } else {
      jobStatus = JobStatus.SUCCESS;
    }
    return jobStatus;
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
