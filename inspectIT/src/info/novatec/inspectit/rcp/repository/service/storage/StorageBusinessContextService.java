package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IBusinessContextManagementService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link IBusinessContextManagementService} for storage purposes.
 *
 * @author Alexander Wert
 *
 */
public class StorageBusinessContextService extends AbstractStorageService<DefaultData> implements IBusinessContextManagementService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<DefaultData> indexingTree;

	/**
	 * Business transactions.
	 */
	private final Map<Pair<Integer, Integer>, BusinessTransactionData> businessTransactions = new HashMap<>();

	/**
	 * Applications.
	 */
	private final Map<Integer, ApplicationData> applications = new HashMap<>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStorageTreeComponent<DefaultData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<DefaultData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * Sets {@link #businessTransactions}.
	 *
	 * @param businessTransactions
	 *            A collection of {@link BusinessTransactionData} instances.
	 */
	public void setBusinessTransactions(Collection<BusinessTransactionData> businessTransactions) {
		for (BusinessTransactionData businessTx : businessTransactions) {
			this.businessTransactions.put(new Pair<Integer, Integer>(businessTx.getApplication().getId(), businessTx.getId()), businessTx);
			this.applications.put(businessTx.getApplication().getId(), businessTx.getApplication());
		}
	}

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
		Set<BusinessTransactionData> result = new HashSet<>();
		for (BusinessTransactionData businessTx : businessTransactions.values()) {
			if (businessTx.getApplication().getId() == applicationId) {
				result.add(businessTx);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerApplication(ApplicationData application) {
		throw new UnsupportedOperationException("Applications cannot be registered on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerBusinessTransaction(BusinessTransactionData businessTransaction) {
		throw new UnsupportedOperationException("Business transactions cannot be registered on a storage!");
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
}
