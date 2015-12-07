package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * Cached access and management service to the business context definition.
 *
 * @author Alexander Wert
 *
 */
@Service
public class BusinessContextManagementService implements IBusinessContextManagementService, InitializingBean {
	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Set of {@link ApplicationData} instances representing identified applications.
	 */
	private final Map<Integer, ApplicationData> applications = new HashMap<Integer, ApplicationData>();

	/**
	 * Set of {@link BusinessTransactionData} instances representing identified business
	 * transactions.
	 */
	private final Map<Pair<Integer, Integer>, BusinessTransactionData> businessTransactions = new HashMap<Pair<Integer, Integer>, BusinessTransactionData>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ApplicationData> getApplications() {
		// creation of new Set required as java.util.HashMap$Values cannot be serialized
		return new HashSet<>(applications.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<BusinessTransactionData> getBusinessTransactions() {
		// creation of new Set required as java.util.HashMap$Values cannot be serialized
		return new HashSet<>(businessTransactions.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<BusinessTransactionData> getBusinessTransactions(int applicationId) {
		Set<BusinessTransactionData> resultSet = new HashSet<>();
		for (BusinessTransactionData businessTx : businessTransactions.values()) {
			if (businessTx.getApplication().getId() == applicationId) {
				resultSet.add(businessTx);
			}
		}
		return resultSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerApplication(ApplicationData application) {
		if (applications.containsKey(application.getId())) {
			throw new IllegalStateException("Cannot register application with ID " + application.getId() + ". An application with this ID already exists.");
		}
		applications.put(application.getId(), application);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerBusinessTransaction(BusinessTransactionData businessTransaction) {
		Pair<Integer, Integer> key = new Pair<Integer, Integer>(businessTransaction.getApplication().getId(), businessTransaction.getId());
		if (businessTransactions.containsKey(key)) {
			throw new IllegalStateException("Cannot register business transaction with ID " + businessTransaction.getId() + " for application having ID " + businessTransaction.getApplication().getId()
					+ ". A business transaction with this combination of IDs already exists.");
		}
		businessTransactions.put(key, businessTransaction);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationData getApplicationForId(int id) {
		return applications.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BusinessTransactionData getBusinessTransactionForId(int appId, int businessTxId) {
		return businessTransactions.get(new Pair<Integer, Integer>(appId, businessTxId));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (log.isInfoEnabled()) {
			log.info("|-Business Context Management Service active...");
		}
	}
}
