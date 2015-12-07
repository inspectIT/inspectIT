package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.cmr.configuration.business.expression.Expression;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class for expression evluation strategies.
 * 
 * @author Alexander Wert
 *
 * @param <T>
 *            type of the expression the concrete class is going to evaluate
 */
public abstract class AbstractExpressionEvaluationStrategy<T extends Expression> {

	/**
	 * {@link ExpressionEvaluation} instance.
	 */
	protected ExpressionEvaluation expressionEvaluation;

	/**
	 * {@link CachedDataService} instance.
	 */
	@Autowired
	protected ICachedDataService cachedDataService;

	/**
	 * Constructor.
	 * 
	 * @param expressionEvaluation
	 *            expression evaluation component used for recursive evaluation
	 */
	public AbstractExpressionEvaluationStrategy(ExpressionEvaluation expressionEvaluation) {
		super();
		this.expressionEvaluation = expressionEvaluation;
	}

	/**
	 * Evaluates the given expression.
	 * 
	 * @param expression
	 *            expression to be evaluated
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance that constitutes the evaluation context
	 * @return Returns the boolean result of the expression evaluation.
	 */
	public abstract boolean evaluate(T expression, InvocationSequenceData invocSequence);
}
