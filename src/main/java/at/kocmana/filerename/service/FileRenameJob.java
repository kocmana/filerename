package at.kocmana.filerename.service;

import at.kocmana.filerename.model.JobArguments;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class FileRenameJob implements Callable<FileRenameJob.JobStatus> {

  private JobArguments jobArguments;
  private volatile JobStatus jobStatus = JobStatus.CREATED;

  private String outputFileName;

  public FileRenameJob(JobArguments arguments) {
    this.jobArguments = arguments;
  }

  public JobStatus getJobStatus() {
    return jobStatus;
  }

  @Override
  public JobStatus call() throws Exception {
    var date = extractDate(jobArguments.inputFile().toString(), jobArguments.outputTemplate()), arguments.outputFileName, dtfIn, dtfOut)
    outputFileName = transformFilename(arguments.inputFile().getFileName().toString(), date);
    //              .map(path -> path.getFileName() + "->" +
//                      transformFilename(path.getFileName().toString(), extractDate(path.getFileName().toString(), filePattern),
//                              arguments.outputTemplate(), dtfIn, dtfOut))
//              //.map(path -> extractDate(path.getFileName().toString(), filePattern))
    return jobStatus;
  }

  private static String extractDate(String fileName, Pattern filePattern) {
    System.out.println("Assessing " + fileName);

    var match = filePattern.matcher(fileName);
    if (!match.find()) {
      System.out.println("Not found");
      return "NOT Treated";
    }
    return match.group("date");
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
