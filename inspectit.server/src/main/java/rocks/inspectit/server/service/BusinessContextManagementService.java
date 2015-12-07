package rocks.inspectit.server.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;

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
	public ApplicationData registerApplication(ApplicationDefinition applicationDefinition) {
		int applicationId = applicationDefinition.createApplicationId();
		ApplicationData application = getApplicationForId(applicationId);
		if (null == application) {
			application = new ApplicationData(applicationId, applicationDefinition.getId(), applicationDefinition.getApplicationName());
			applications.put(applicationId, application);
		}

		return application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BusinessTransactionData registerBusinessTransaction(ApplicationData application, BusinessTransactionDefinition businessTransactionDefinition, String businessTransactionName) {
		int businessTxId = businessTransactionDefinition.createBusinessTransactionId(businessTransactionName);
		BusinessTransactionData businessTransaction = getBusinessTransactionForId(application.getId(), businessTxId);
		if (null == businessTransaction) {
			businessTransaction = new BusinessTransactionData(businessTxId, businessTransactionDefinition.getId(), application, businessTransactionName);
			Pair<Integer, Integer> key = new Pair<Integer, Integer>(application.getId(), businessTransaction.getId());
			businessTransactions.put(key, businessTransaction);
		}

		return businessTransaction;
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
