package at.kocmana.filerename.service.transformation.rules;

import java.nio.file.Path;

public interface TransformationRule {

  TransformationRuleIdentity getIdentity();

  default boolean equalTo(TransformationRule other) {
    return this.getIdentity().equals(other.getIdentity());
  }

  default boolean overlapsWith(TransformationRule other) {
    return this.getIdentity().overlapsWith(other.getIdentity());
  }

  String replaceTemplateWithSearchString(String inputPattern);

  String apply(Path file, String outputPattern);

}
