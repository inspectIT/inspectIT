package info.novatec.inspectit.cmr.configuration.business;

import info.novatec.inspectit.exception.BusinessException;

import java.io.Serializable;
import java.util.List;

/**
 * Configuration element defining an application context.
 *
 * @author Alexander Wert
 *
 */
public interface IApplicationDefinition extends Serializable {
	/**
	 * The default identifier.
	 */
	long DEFAULT_ID = 0L;

	/**
	 * Returns the name of the application.
	 *
	 * @return Returns the name of the application
	 */
	String getApplicationName();

	/**
	 * Sets the name of the application.
	 *
	 * @param applicationName
	 *            New value for the name of the application.
	 */
	void setApplicationName(String applicationName);

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
	 * Returns an unmodifiable list of all {@link IBusinessTransactionDefinition} instances known in
	 * this application definition.
	 *
	 * @return unmodifiable list of all {@link IBusinessTransactionDefinition} instances known in
	 *         this application definition
	 */
	List<IBusinessTransactionDefinition> getBusinessTransactionDefinitions();

	/**
	 * Retrieves the {@link IBusinessTransactionDefinition} with the given identifier.
	 *
	 * @param id
	 *            unique id identifying the business transaction to retrieve
	 * @return Return the {@link IBusinessTransactionDefinition} with the given id, or null if no
	 *         {@link IBusinessTransactionDefinition} with the passed id could be found.
	 *
	 * @throws BusinessException
	 *             if no {@link IBusinessTransactionDefinition} with the given identifier exists.
	 */
	IBusinessTransactionDefinition getBusinessTransactionDefinition(long id) throws BusinessException;

	/**
	 * Returns the unique identifier of this application definition.
	 *
	 * @return Returns the unique identifier of this application definition.
	 */
	long getId();

	/**
	 * Returns the default {@link IBusinessTransactionDefinition}.
	 *
	 * @return Returns the default {@link IBusinessTransactionDefinition}
	 */
	IBusinessTransactionDefinition getDefaultBusinessTransactionDefinition();

	/**
	 * Adds business transaction definition to the application definition.
	 *
	 * @param businessTransactionDefinition
	 *            {@link IBusinessTransactionDefinition} instance to add
	 * @throws BusinessException
	 *             If the application definition already contains a business transaction with same
	 *             identifier.
	 */
	void addBusinessTransactionDefinition(IBusinessTransactionDefinition businessTransactionDefinition) throws BusinessException;

	/**
	 * Adds business transaction definition to the application definition. Inserts it to the list
	 * before the element with the passed index.
	 *
	 * @param businessTransactionDefinition
	 *            {@link IBusinessTransactionDefinition} instance to add
	 * @param insertBeforeIndex
	 *            insert before this index
	 * @throws BusinessException
	 *             If the application definition already contains a business transaction with same
	 *             identifier or the insertBeforeIndex is not valid.
	 */
	void addBusinessTransactionDefinition(IBusinessTransactionDefinition businessTransactionDefinition, int insertBeforeIndex) throws BusinessException;

	/**
	 * Deletes the {@link IBusinessTransactionDefinition} from the application definition.
	 *
	 * @param businessTransactionDefinition
	 *            {@link IBusinessTransactionDefinition} to delete
	 *
	 * @return Returns true if the application definition contained the business transaction
	 */
	boolean deleteBusinessTransactionDefinition(IBusinessTransactionDefinition businessTransactionDefinition);

	/**
	 * Moves the {@link IBusinessTransactionDefinition} to a different position specified by the
	 * index parameter.
	 *
	 * @param businessTransactionDefinition
	 *            {@link IBusinessTransactionDefinition} to move
	 * @param index
	 *            position to move the {@link IBusinessTransactionDefinition} to
	 * @throws BusinessException
	 *             If the moving the {@link IBusinessTransactionDefinition} fails.
	 */
	void moveBusinessTransactionDefinition(IBusinessTransactionDefinition businessTransactionDefinition, int index) throws BusinessException;

	/**
	 * Gets {@link #matchingRule}.
	 *
	 * @return Returns the {@link IMatchingRule} for this application.
	 */
	IMatchingRule getMatchingRule();

	/**
	 * Sets the {@link IMatchingRule} for this application.
	 *
	 * @param matchingRule
	 *            New value for {@link IMatchingRule}.
	 */
	void setMatchingRule(IMatchingRule matchingRule);

}
