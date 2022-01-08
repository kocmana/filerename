package at.kocmana.filerename;

import at.kocmana.filerename.controller.CliController;

/**
 * <p>FileRename Application main class.</p>
 * This class passes all arguments to the {@link CliController}. This is done to encapsulate all logic depending on the
 * CLI dependency used into one separate class.
 */
public class FileRenameApplication {

  public static void main(String[] args) {
    new CliController().run(args);
  }

}
