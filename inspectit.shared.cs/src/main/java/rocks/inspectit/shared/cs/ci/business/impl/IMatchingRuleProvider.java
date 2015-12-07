package rocks.inspectit.shared.cs.ci.business.impl;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;

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
	 * Indicates whether the provided rule is allowed to be changed.
	 *
	 * @return Returns true, if the rule is allowed to be changed. Otherwise false.
	 */
	boolean isChangeable();
}
