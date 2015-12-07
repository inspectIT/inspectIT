package info.novatec.inspectit.cmr.configuration.business;

import java.io.Serializable;

/**
 * Matching rule used for mapping of applications and business transactions to measurement data.
 *
 * @author Alexander Wert
 *
 */
public interface IMatchingRule extends Serializable {
	/**
	 *
	 * @return Returns the {@link IExpression} instance.
	 */
	IExpression getExpression();

	/**
	 * Sets the {@link IExpression}.
	 *
	 * @param expression
	 *            {@link IExpression} to set
	 */
	void setExpression(IExpression expression);
}
