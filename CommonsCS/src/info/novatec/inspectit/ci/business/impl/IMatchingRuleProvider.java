package info.novatec.inspectit.ci.business.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;

/**
 * Common interface for entities that provide a {@link AbstractExpression} instance as a matching
 * rule.
 *
 * @author Alexander Wert
 *
 */
public interface IMatchingRuleProvider {

	/**
	 * Returns an {@link AbstractExpression} instance.
	 *
	 * @return Returns an {@link AbstractExpression} instance.s
	 */
	AbstractExpression getMatchingRuleExpression();

	/**
	 * Sets the {@link AbstractExpression} for this {@link IMatchingRuleProvider} instance.
	 *
	 * @param matchingRuleExpression
	 *            New value for {@link AbstractExpression}.
	 */
	void setMatchingRuleExpression(AbstractExpression matchingRuleExpression);

	/**
	 * Returns the unique identifier of this {@link IMatchingRuleProvider} instance.
	 *
	 * @return Returns the unique identifier of this {@link IMatchingRuleProvider} instance.
	 */
	int getId();
}
