package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.cmr.configuration.business.IExpression;
import info.novatec.inspectit.cmr.configuration.business.IMatchingRule;
import info.novatec.inspectit.cmr.configuration.business.MatchingRule;
import info.novatec.inspectit.cmr.configuration.business.expression.Expression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.AndExpression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.NotExpression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.OrExpression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import org.springframework.stereotype.Component;

/**
 * This component is responsible for evaluating {@link MatchingRule} instances.
 * 
 * @author Alexander Wert
 *
 */
@Component
public class ExpressionEvaluation {

	/**
	 * Evaluation strategy for {@link NotExpression} instances.
	 */
	final NotEvalStrategy notStrategy;

	/**
	 * Evaluation strategy for {@link AndExpression} instances.
	 */
	final AndEvalStrategy andStrategy;

	/**
	 * Evaluation strategy for {@link OrExpression} instances.
	 */
	final OrEvalStrategy orStrategy;

	/**
	 * Evaluation strategy for {@link StringMatchingExpression} instances.
	 */
	final StringMatchingEvalStrategy stringMatchingStrategy;

	/**
	 * Constructor. Sets up evaluation strategies.
	 */
	public ExpressionEvaluation() {
		notStrategy = new NotEvalStrategy(this);
		andStrategy = new AndEvalStrategy(this);
		orStrategy = new OrEvalStrategy(this);
		stringMatchingStrategy = new StringMatchingEvalStrategy(this);
	}

	/**
	 * Constructor.
	 * 
	 * @param notStrategy
	 *            {@link #notStrategy}
	 * @param andStrategy
	 *            {@link #andStrategy}
	 * @param orStrategy
	 *            {@link #orStrategy}
	 * @param stringStrategy
	 *            {@link #stringMatchingStrategy}
	 */
	protected ExpressionEvaluation(NotEvalStrategy notStrategy, AndEvalStrategy andStrategy, OrEvalStrategy orStrategy, StringMatchingEvalStrategy stringStrategy) {
		this.notStrategy = notStrategy;
		this.andStrategy = andStrategy;
		this.orStrategy = orStrategy;
		this.stringMatchingStrategy = stringStrategy;
	}

	/**
	 * Evaluates the given {@link MatchingRule} against the evaluation context defined by the
	 * {@link InvocationSequenceData} instance.
	 * 
	 * @param rule
	 *            {@link MatchingRule} instance
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance defining the evaluation context.
	 * @return Boolean result of evaluating the {@link MatchingRule}
	 */
	public boolean evaluate(IMatchingRule rule, InvocationSequenceData invocSequence) {
		return evaluate(rule.getExpression(), invocSequence);
	}

	/**
	 * Evaluates the given {@link Expression} against the evaluation context defined by the
	 * {@link InvocationSequenceData} instance.
	 * 
	 * @param expression
	 *            {@link Expression} instance
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance defining the evaluation context.
	 * @return Boolean result of evaluating the {@link Expression}
	 */
	protected boolean evaluate(IExpression expression, InvocationSequenceData invocSequence) {
		if (expression instanceof AndExpression) {
			return andStrategy.evaluate((AndExpression) expression, invocSequence);
		} else if (expression instanceof OrExpression) {
			return orStrategy.evaluate((OrExpression) expression, invocSequence);
		} else if (expression instanceof NotExpression) {
			return notStrategy.evaluate((NotExpression) expression, invocSequence);
		} else if (expression instanceof StringMatchingExpression) {
			return stringMatchingStrategy.evaluate((StringMatchingExpression) expression, invocSequence);
		} else if (expression instanceof BooleanExpression) {
			return ((BooleanExpression) expression).isValue();
		}
		throw new RuntimeException("Expression of type " + expression.getClass().getName() + " not supported!");
	}

}
