package at.kocmana.filerename.model;

import at.kocmana.filerename.controller.CliController.CollisionResolutionStrategy;

import java.nio.file.Path;

public record CommandLineArguments(
        Path path,
        boolean recursive,
        String inputTemplate,
        String outputTemplate,
        boolean dryRun,
        boolean createCopy,
        CollisionResolutionStrategy collisionResolutionStrategy
) {
}
