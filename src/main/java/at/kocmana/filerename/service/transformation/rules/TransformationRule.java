package at.kocmana.filerename.service.transformation.rules;

import at.kocmana.filerename.service.FileRenameJob;
import at.kocmana.filerename.service.FileRenameTask;
import java.nio.file.Path;

public interface TransformationRule {

  /**
   * <p>The identity of a rule is defined as its start and endpoint in both input (<i>-i</i>) and output (<i>-o</i>) argument.
   * This function is used to check for correct parsing of the rule templates. Correct rule templates may not overlap.</p>
   *
   * <p>If a rule applies just to one of the arguments (e.g. just the input) return a {@link TransformationRuleIdentity.Range}
   * of {@code (0,0)} for the argument where the pattern is not expected.</p>
   *
   * <h3>Example:</h3>
   * The following arguments will yield a {@link TransformationRuleIdentity} for the {@link TimestampTransformationRule} of {@code (4,26,0,22)}
   * <pre>
   *   -i "IMG_&lt;&lt;TS|yyyyMMdd_HHmmss&gt;&gt;.jpg" -o "&lt;&lt;TS|yyyyMMdd_HHmmss&gt;&gt;"
   * </pre>
   *
   * <h3>Default implementation</h3>
   * <p>{@link AbstractTransformationRule} provides a default implementation for generating rule identities that can be
   * used as a reference.</p>
   *
   * @return a {@link TransformationRuleIdentity} consisting of two {@link TransformationRuleIdentity.Range}s,
   * one for the input and one for the output argument.
   */
  TransformationRuleIdentity getIdentity();

  /**
   * <p>Function used to check if the arguments provided can be parsed without of having any overlaps between the applied
   * rule templates.</p>
   * @param other another instance of a {@link TransformationRule}
   * @return a boolean, where {@code true} indicates that the rule templates either overlap in the input argument,
   * the output argument or both. {@code false} indicates no overlap between to {@link TransformationRule} instances.
   */
  default boolean overlapsWith(TransformationRule other) {
    return this.getIdentity().overlapsWith(other.getIdentity());
  }

  /**
   * <p>This function is called by each {@link FileRenameTask} in order to generate a pattern
   * that can be used to search for filenames to which the rules apply. Depending on the number of other applicable
   * rules and their order, the {@code inputPattern} provided may already be deviating from the original
   * input (<i>-i</i>) argument.</p>
   *
   * <p>Commonly, the rule template for the respective rule is replaced with a regex string that will match the expected
   * parts of the respective filenames.</p>
   *
   * <p>If any rule does not require any information from the input argument, the input pattern should be returned:</p>
   * <pre>
   *   public String replaceTemplateWithSearchString(String inputPattern) {
   *     return inputPattern;
   *   }
   * </pre>
   *
   * <p><b>CAUTION:</b> This function may be called multiple times in parallel without any external synchronization.
   * If a common state is modified in the course of this call, sufficient precautions have to be taken.</p>
   *
   * @param inputPattern the input pattern derived from the original input argument.
   * @return a modified {@code inputPattern} where the rule template for the rule to be implemented are replaced with a
   * regex pattern that matches the expected parts of the filename.
   */
  String replaceTemplateWithSearchString(String inputPattern);

  /**
   * <p>This function is called by each {@link FileRenameJob} in order to prepare the
   * copy/move process for the file handled by the respective {@link FileRenameJob}.
   * In this step, the file name of a file should be modified in a way that represents the rule to be implemented.</p>
   *
   * <p>Similar to {@link #replaceTemplateWithSearchString(String)} method, the outputPattern may deviate from the
   * original output (<i>-o</i>) command line argument provided, depending on the applicability of other rules.</p>
   *
   * <p><b>CAUTION:</b> This function may be called multiple times in parallel without any external synchronization.
   * If a common state is modified in the course of this call, sufficient precautions have to be taken.</p>
   *
   * @param file The file that should be renamed or copied.
   * @param outputPattern The output pattern in its current step of modification. Should be used as a basis for
   *                      modification, instead of the original output command line argument.
   * @return The output pattern provided modified according to the rule to be implemented.
   */
  String apply(Path file, String outputPattern);

}
