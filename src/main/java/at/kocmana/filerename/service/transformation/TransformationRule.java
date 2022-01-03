package at.kocmana.filerename.service.transformation;

public interface TransformationRule {

  TransformationRuleIdentity getIdentity();

  default boolean equalTo(TransformationRule other) {
    return this.getIdentity().equals(other.getIdentity());
  }

  void prepare();

  String apply(String filename);

}
