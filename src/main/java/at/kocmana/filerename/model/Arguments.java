package at.kocmana.filerename.model;

import java.nio.file.Path;

public record Arguments(
    Path path,
    boolean recursive,
    String inputTemplate,
    String outputTemplate,
    boolean dryRun
) {
}
