package at.kocmana.filerename.model.exception;

import at.kocmana.filerename.service.transformation.rules.TransformationRule;

/**
 * TransformationRuleException is a wrapper for all exceptions happening during creation and
 * application of {@link TransformationRule}s. The initial {@link Throwable} has to be passed down.
 */
public class TransformationRuleException extends RuntimeException {

  public TransformationRuleException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransformationRuleException(Throwable cause) {
    super(cause);
  }
}
