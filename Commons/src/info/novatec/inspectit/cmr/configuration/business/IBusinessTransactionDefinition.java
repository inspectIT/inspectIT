package info.novatec.inspectit.cmr.configuration.business;

import java.io.Serializable;

/**
 * Configuration element defining a business transaction context.
 *
 * @author Alexander Wert
 *
 */
public interface IBusinessTransactionDefinition extends Serializable {
	/**
	 * The default identifier.
	 */
	long DEFAULT_ID = 0L;

	/**
	 * Returns the name of the business transaction.
	 *
	 * @return the name of the business transaction
	 */
	String getBusinessTransactionName();

	/**
	 * Sets the name of the business transaction.
	 *
	 * @param businessTransactionName
	 *            New value for the name of the business transaction
	 */
	void setBusinessTransactionName(String businessTransactionName);

	/**
	 *
	 * @return description
	 */
	String getDescription();

	/**
	 * Sets the description text.
	 *
	 * @param description
	 *            New value for the description text.
	 */
	void setDescription(String description);

	/**
	 * Returns the unique identifier of this business transaction definition.
	 *
	 * @return Returns the unique identifier of this business transaction definition.
	 */

	/**
	 * Returns the identifier of this business transaction.
	 *
	 * @return Returns the identifier of this business transaction
	 */
	long getId();

	/**
	 * Gets {@link #matchingRule}.
	 *
	 * @return Returns the {@link IMatchingRule} for this business transaction.
	 */
	IMatchingRule getMatchingRule();

	/**
	 * Sets the {@link IMatchingRule} for this business transaction.
	 *
	 * @param matchingRule
	 *            New value for {@link IMatchingRule}.
	 */
	void setMatchingRule(IMatchingRule matchingRule);
}
