package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.ci.BusinessContextDefinition;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;
import info.novatec.inspectit.exception.BusinessException;

import java.util.Collection;
import java.util.List;

/**
 * Service interface which defines the methods to manage the business context (i.e. applications,
 * business transactions, SLAs, etc.) of invocation sequences.
 *
 * @author Alexander Wert
 *
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IBusinessContextManagementService {

	/**
	 * Returns the currently existing {@link IBusinessContextDefinition} from the CMR configuration.
	 *
	 * @return Returns the currently existing {@link IBusinessContextDefinition} from the CMR
	 *         configuration.
	 */
	BusinessContextDefinition getBusinessContextDefinition();

	/**
	 * Returns an unmodifiable list of all {@link ApplicationDefinition} instances.
	 *
	 * @return unmodifiable list of all {@link ApplicationDefinition} instances.
	 */
	List<ApplicationDefinition> getApplicationDefinitions();

	/**
	 * Retrieves the {@link ApplicationDefinition} for the given id.
	 *
	 * @param id
	 *            identifier of the application definition.
	 * @return Returns the {@link ApplicationDefinition} for the given id.
	 *
	 * @throws BusinessException
	 *             if adding {@link ApplicationDefinition} fails.
	 */
	ApplicationDefinition getApplicationDefinition(int id) throws BusinessException;

	/**
	 * Adds application definition to the business context.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} instance to add
	 * @throws BusinessException
	 *             if adding {@link ApplicationDefinition} fails.
	 */
	void addApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException;

	/**
	 * Adds application definition to the business context. Inserts it to the list before the
	 * element with the passed index.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} instance to add
	 * @param insertBeforeIndex
	 *            insert before this index
	 * @throws BusinessException
	 *             if adding {@link ApplicationDefinition} fails.
	 */
	void addApplicationDefinition(ApplicationDefinition appDefinition, int insertBeforeIndex) throws BusinessException;

	/**
	 * Deletes the {@link ApplicationDefinition} from the business context.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to delete
	 * @throws BusinessException
	 *             if deleting {@link ApplicationDefinition} fails.
	 */
	void deleteApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException;

	/**
	 * Moves the {@link ApplicationDefinition} to a different position specified by the index
	 * parameter.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to move
	 * @param index
	 *            position to move the {@link ApplicationDefinition} to
	 * @throws BusinessException
	 *             if moving {@link ApplicationDefinition} fails.
	 */
	void moveApplicationDefinition(ApplicationDefinition appDefinition, int index) throws BusinessException;

	/**
	 * Updates the given {@link ApplicationDefinition}.
	 *
	 * @param appDefinition
	 *            {@link ApplicationDefinition} to update
	 * @throws BusinessException
	 *             if update of {@link ApplicationDefinition} fails
	 * @throws BusinessException
	 *             if updating {@link ApplicationDefinition} fails.
	 * @return the updated {@link ApplicationDefinition} instance
	 */
	ApplicationDefinition updateApplicationDefinition(ApplicationDefinition appDefinition) throws BusinessException;

	/**
	 * Returns a collection of all recognized applications.
	 *
	 * @return a collection of all recognized applications.
	 */
	Collection<ApplicationData> getApplications();

	/**
	 * Returns a collection of all recognized business transactions.
	 *
	 * @return a collection of all recognized business transactions.
	 */
	Collection<BusinessTransactionData> getBusinessTransactions();

	/**
	 * Returns a collection of all recognized business transactions for the given application
	 * identifier.
	 *
	 * @param applicationId
	 *            application identifier to retrieve the business transactions for.
	 * @return a collection of all recognized business transactions.
	 */
	Collection<BusinessTransactionData> getBusinessTransactions(int applicationId);

	/**
	 * Registers a business transaction.
	 *
	 * @param businessTransaction
	 *            {@link BusinessTransactionData} instance to register.
	 */
	void registerBusinessTransaction(BusinessTransactionData businessTransaction);
}
