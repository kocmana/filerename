package at.kocmana.filerename.model;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public record JobArguments(
        Path inputFile,
        DateTimeFormatter dtfIn,
        DateTimeFormatter dtfOut,
        String outputTemplate
) {
}
