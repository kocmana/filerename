package at.kocmana.filerename.testutil;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFileUtils {

  public static Path openTestFile(String filename) {
    return Paths.get("src", "test", "resources", "testfiles", filename);
  }

}
