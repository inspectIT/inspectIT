package rocks.inspectit.shared.cs.cmr.service;

import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;

/**
 * Service interface which defines the methods to register the business context (i.e. applications,
 * business transactions, SLAs, etc.) of invocation sequences.
 *
 * @author Alexander Wert
 *
 */
public interface IBusinessContextRegistryService {
	/**
	 * Registers an application.
	 *
	 * @param applicationDefinition
	 *            {@link ApplicationDefinition} describing the {@link ApplicationData} instance to
	 *            register.
	 * @return returns the registered {@link ApplicationData} instance.
	 */
	ApplicationData registerApplication(ApplicationDefinition applicationDefinition);

	/**
	 * Registers a business transaction.
	 *
	 * @param application
	 *            The {@link ApplicationData} instance to register the business transaction to
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} describing the
	 *            {@link BusinessTransactionData} instance to register.
	 * @param businessTransactionName
	 *            the name of the business transaction to register
	 *
	 * @return returns the registered {@link BusinessTransactionData} instance.
	 */
	BusinessTransactionData registerBusinessTransaction(ApplicationData application, BusinessTransactionDefinition businessTransactionDefinition, String businessTransactionName);
}
