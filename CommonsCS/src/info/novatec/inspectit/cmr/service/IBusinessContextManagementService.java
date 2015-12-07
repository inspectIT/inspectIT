package info.novatec.inspectit.cmr.service;

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
}
