package at.kocmana.filerename.model;

import at.kocmana.filerename.service.transformation.TransformationRule;

import java.nio.file.Path;
import java.util.List;

public record JobArguments(
        Path inputFile,
        List<TransformationRule> transformationRules,
        String outputTemplate,
        boolean dryRun
) {
}
