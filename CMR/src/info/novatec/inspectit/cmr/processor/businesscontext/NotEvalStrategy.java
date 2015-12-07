package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.cmr.configuration.business.expression.impl.NotExpression;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

/**
 * Evaluation strategy for the {@link NotExpression}.
 * 
 * @author Alexander Wert
 *
 */
public class NotEvalStrategy extends AbstractExpressionEvaluationStrategy<NotExpression> {
	/**
	 * Constructor.
	 * 
	 * @param expressionEvaluation
	 *            expression evaluation component used for recursive evaluation
	 */
	public NotEvalStrategy(ExpressionEvaluation expressionEvaluation) {
		super(expressionEvaluation);
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(NotExpression expression, InvocationSequenceData invocSequence) {
		return !expressionEvaluation.evaluate(expression.getOperand(), invocSequence);
	}

}
