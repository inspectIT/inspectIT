package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.ci.business.AndExpression;
import info.novatec.inspectit.ci.business.Expression;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

/**
 * Evaluation strategy for the {@link AndExpression}.
 *
 * @author Alexander Wert
 *
 */
public class AndEvalStrategy extends AbstractExpressionEvaluationStrategy<AndExpression> {

	/**
	 * Constructor.
	 *
	 * @param expressionEvaluation
	 *            expression evaluation component used for recursive evaluation
	 */
	public AndEvalStrategy(ExpressionEvaluation expressionEvaluation) {
		super(expressionEvaluation);
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(AndExpression expression, InvocationSequenceData invocSequence) {
		for (Expression expr : expression.getOperands()) {
			if (!expressionEvaluation.evaluate(expr, invocSequence)) {
				return false;
			}
		}
		return true;
	}

}
