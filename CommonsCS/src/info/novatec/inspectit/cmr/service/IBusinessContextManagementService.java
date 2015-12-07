package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;

import java.util.Collection;

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
	 * Registers an application.
	 *
	 * @param application
	 *            {@link ApplicationData} instance to register.
	 */
	void registerApplication(ApplicationData application);

	/**
	 * Registers a business transaction.
	 *
	 * @param businessTransaction
	 *            {@link BusinessTransactionData} instance to register.
	 */
	void registerBusinessTransaction(BusinessTransactionData businessTransaction);

	/**
	 * Retrieves the {@link IApplicationDefinition} for the given identifier.
	 *
	 * @param id
	 *            unique identifier of the application definition
	 * @return Returns the application definition for the given id or null if no applicaiton
	 *         definition for the id exists.
	 */
	ApplicationData getApplicationForId(int id);

	/**
	 * Retrieves the {@link BusinessTransactionDefinition} for the given application and business
	 * transaction identifiers.
	 *
	 * @param appId
	 *            unique identifier of the application definition
	 * @param businessTxId
	 *            unique identifier of the business transaction definition
	 * @return Returns the business transaction definition or null if no business transaction
	 *         definition for the given pair of identifiers exists.
	 */
	BusinessTransactionData getBusinessTransactionForId(int appId, int businessTxId);
}
