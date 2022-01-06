package at.kocmana.filerename.service;

import at.kocmana.filerename.model.JobArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
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
    return jobStatus;
  }

  @Override
  public JobStatus call() {
    var filename = jobArguments.inputFile().getFileName().toString();
    outputFileName = jobArguments.outputTemplate();
    for (var transformationRule : jobArguments.transformationRules()) {
      outputFileName = transformationRule.apply(filename, outputFileName);
    }

    log.info("{} -> {}", filename, outputFileName);

    if (!jobArguments.dryRun()) {
      try {
        Files.move(jobArguments.inputFile(), jobArguments.inputFile().resolveSibling(outputFileName));
      } catch (IOException exception) {
        log.error("Could not rename filename from {} to {}: {}",
                filename, outputFileName, exception.getMessage());
      }
    }
    return jobStatus;
  }

  @Override
  public String toString() {
    return "jobArguments: " + jobArguments.toString() + "\r\n"
            + "job Status" + jobStatus;
  }

  private static String transformFilename(String filename, String dateTime, String outputPattern,
                                          DateTimeFormatter dtfIn, DateTimeFormatter dtfOut) {
    var dateTimeParsed = dtfIn.parse(dateTime);

    return outputPattern.replaceAll("<<.*?>>", dtfOut.format(dateTimeParsed));
  }

  public enum JobStatus {
    CREATED, READY, RUNNING, SUCCESS, FAILED
  }

}
