package at.kocmana.filerename.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

class CliControllerTest {

  @ParameterizedTest
  @ValueSource(strings = {"-i", "--input"})
  void testCorrectInputPattern(String parameterName) {
    //given
    var underTest = new CliController();
    String[] args = {parameterName, "foo", "-o", "bar"};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(1);
    assertThat(actualResult.get(0).inputTemplate()).isEqualTo("foo");
  }

  @Test
  void testExceptionOnMissingInputPattern() {
    //given
    var underTest = new CliController();
    String[] args = {"-o", "bar"};

    //when + then
    assertThatExceptionOfType(CommandLine.MissingParameterException.class)
        .isThrownBy(() -> new CommandLine(underTest).parseArgs(args))
        .withMessage("Error: Missing required argument(s): --input=<inputTemplate>");
  }

  @ParameterizedTest
  @ValueSource(strings = {"-o", "--output"})
  void testCorrectOutputPattern(String parameterName) {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo", parameterName, "bar"};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(1);
    assertThat(actualResult.get(0).outputTemplate()).isEqualTo("bar");
  }

  @Test
  void testExceptionOnMissingOutputPattern() {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo"};

    //when + then
    assertThatExceptionOfType(CommandLine.MissingParameterException.class)
        .isThrownBy(() -> new CommandLine(underTest).parseArgs(args))
        .withMessage("Error: Missing required argument(s): --output=<outputTemplate>");
  }

  @ParameterizedTest
  @ValueSource(strings = {"-r", "--recursive"})
  void testCorrectRecursiveArgumentValue(String parameterName) {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo", "-o", "bar", parameterName};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(1);
    assertThat(actualResult.get(0).recursive()).isTrue();
  }

  @Test
  void testMissingRecursiveArgumentValue() {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo", "-o", "bar"};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(1);
    assertThat(actualResult.get(0).recursive()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"-d", "--dryRun"})
  void testCorrectDryRunArgumentValue(String parameterName) {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo", "-o", "bar", parameterName};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(1);
    assertThat(actualResult.get(0).dryRun()).isTrue();
  }

  @Test
  void testMissingDryRunArgumentValue() {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo", "-o", "bar"};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(1);
    assertThat(actualResult.get(0).dryRun()).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"-p", "--path"})
  void testCorrectPathArgumentValue(String parameterName) {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo", "-o", "bar", parameterName, "./baz"};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(1);
    assertThat(actualResult.get(0).path()).endsWithRaw(Path.of("baz"));
  }

  @Test
  void testMultipleArgumentGroups() {
    //given
    var underTest = new CliController();
    String[] args = {"-i", "foo", "-o", "bar", "-i", "any", "-o", "some"};

    //when
    new CommandLine(underTest).parseArgs(args);
    var actualResult = underTest.mapArguments();

    //then
    assertThat(actualResult).hasSize(2)
        .anyMatch(argument -> argument.inputTemplate().equals("foo"))
        .anyMatch(argument -> argument.inputTemplate().equals("any"));
  }

}
