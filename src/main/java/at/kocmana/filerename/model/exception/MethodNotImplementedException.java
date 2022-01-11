package at.kocmana.filerename.model.exception;

import at.kocmana.filerename.service.transformation.rules.AbstractTransformationRule;

/**
 * Used to warn users when extending {@link AbstractTransformationRule} that required methods still lack implementation.
 */
public class MethodNotImplementedException extends RuntimeException {

  public MethodNotImplementedException(String message) {
    super(message);
  }

}
