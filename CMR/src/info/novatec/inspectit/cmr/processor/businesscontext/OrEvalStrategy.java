package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.ci.business.Expression;
import info.novatec.inspectit.ci.business.OrExpression;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

/**
 * Evaluation strategy for the {@link OrExpression}.
 * 
 * @author Alexander Wert
 *
 */
public class OrEvalStrategy extends AbstractExpressionEvaluationStrategy<OrExpression> {
	/**
	 * Constructor.
	 * 
	 * @param expressionEvaluation
	 *            expression evaluation component used for recursive evaluation
	 */
	public OrEvalStrategy(ExpressionEvaluation expressionEvaluation) {
		super(expressionEvaluation);
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(OrExpression expression, InvocationSequenceData invocSequence) {
		for (Expression expr : expression.getOperands()) {
			if (expressionEvaluation.evaluate(expr, invocSequence)) {
				return true;
			}
		}
		return false;
	}
}
