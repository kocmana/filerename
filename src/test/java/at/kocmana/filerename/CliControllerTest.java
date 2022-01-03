package at.kocmana.filerename;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

class CliControllerTest {

  private static final CliController UNDER_TEST = new CliController();

  @ParameterizedTest
  @ValueSource(strings = {"-i", "--input"})
  void testCorrectInputPattern(String parameterName) {
    //given
    String[] args = {parameterName, "foo", "-o", "bar"};

    //when
    new CommandLine(UNDER_TEST).parseArgs(args);
    var actualResult = UNDER_TEST.mapArguments();

    //then
    assertThat(actualResult.inputTemplate()).isEqualTo("foo");
  }

  @Test
  void testExceptionOnMissingInputPattern() {
    //given
    String[] args = {"-o", "bar"};

    //when + then
    assertThatExceptionOfType(CommandLine.MissingParameterException.class)
        .isThrownBy(() -> new CommandLine(UNDER_TEST).parseArgs(args))
        .withMessage("Missing required option: '--input=<inputTemplate>'");
  }

  @ParameterizedTest
  @ValueSource(strings = {"-o", "--output"})
  void testCorrectOutputPattern(String parameterName) {
    //given
    String[] args = {"-i", "foo", parameterName, "bar"};

    //when
    new CommandLine(UNDER_TEST).parseArgs(args);
    var actualResult = UNDER_TEST.mapArguments();

    //then
    assertThat(actualResult.outputTemplate()).isEqualTo("bar");
  }

  @Test
  void testExceptionOnMissingOutputPattern() {
    //given
    String[] args = {"-i", "foo"};

    //when + then
    assertThatExceptionOfType(CommandLine.MissingParameterException.class)
        .isThrownBy(() -> new CommandLine(UNDER_TEST).parseArgs(args))
        .withMessage("Missing required option: '--output=<outputTemplate>'");
  }

  @ParameterizedTest
  @ValueSource(strings = {"-r", "--recursive"})
  void testCorrectRecursiveArgumentValue(String parameterName) {
    //given
    String[] args = {"-i", "foo", "-o", "bar", parameterName};

    //when
    new CommandLine(UNDER_TEST).parseArgs(args);
    var actualResult = UNDER_TEST.mapArguments();

    //then
    assertThat(actualResult.recursive()).isTrue();
  }

  @Test
  void testMissingRecursiveArgumentValue() {
    //given
    String[] args = {"-i", "foo", "-o", "bar"};

    //when
    new CommandLine(UNDER_TEST).parseArgs(args);
    var actualResult = UNDER_TEST.mapArguments();

    //then
    assertThat(actualResult.recursive()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"-d", "--dry"})
  void testCorrectDryRunArgumentValue(String parameterName) {
    //given
    String[] args = {"-i", "foo", "-o", "bar", parameterName};

    //when
    new CommandLine(UNDER_TEST).parseArgs(args);
    var actualResult = UNDER_TEST.mapArguments();

    //then
    assertThat(actualResult.dryRun()).isTrue();
  }

  @Test
  void testMissingDryRunArgumentValue() {
    //given
    String[] args = {"-i", "foo", "-o", "bar"};

    //when
    new CommandLine(UNDER_TEST).parseArgs(args);
    var actualResult = UNDER_TEST.mapArguments();

    //then
    assertThat(actualResult.dryRun()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"-p", "--path"})
  void testCorrectPathArgumentValue(String parameterName) {
    //given
    String[] args = {"-i", "foo", "-o", "bar", parameterName, "./baz"};

    //when
    new CommandLine(UNDER_TEST).parseArgs(args);
    var actualResult = UNDER_TEST.mapArguments();

    //then
    assertThat(actualResult.path().endsWith("baz")).isTrue();
  }

}
