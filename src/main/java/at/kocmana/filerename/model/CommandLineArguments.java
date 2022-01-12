package at.kocmana.filerename.model;

import java.nio.file.Path;

public record CommandLineArguments(
    Path path,
    boolean recursive,
    String inputTemplate,
    String outputTemplate,
    boolean dryRun,
    boolean createCopy
) {
}
