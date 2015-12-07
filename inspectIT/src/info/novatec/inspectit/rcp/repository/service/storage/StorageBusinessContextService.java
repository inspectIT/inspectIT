package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.ci.BusinessContextDefinition;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.cmr.service.IBusinessContextManagementService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
	private final Set<BusinessTransactionData> businessTransactions = new HashSet<>();

	/**
	 * Applications.
	 */
	private final Set<ApplicationData> applications = new HashSet<>();

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
	 * {@inheritDoc}
	 */
	@Override
	public BusinessContextDefinition getBusinessContextDefinition() {
		throw new UnsupportedOperationException("Retrieving Business Context Definitions is not supported for storages!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ApplicationDefinition> getApplicationDefinitions() {
		throw new UnsupportedOperationException("Retrieving Business Context Definitions is not supported for storages!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationDefinition getApplicationDefinition(int id) throws BusinessException {
		throw new UnsupportedOperationException("Retrieving Business Context Definitions is not supported for storages!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(ApplicationDefinition appDefinition) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(ApplicationDefinition appDefinition, int insertBeforeIndex) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteApplicationDefinition(ApplicationDefinition appDefinition) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveApplicationDefinition(ApplicationDefinition appDefinition, int index) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationDefinition updateApplicationDefinition(ApplicationDefinition appDefinition) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * Sets {@link #businessTransactions}.
	 *
	 * @param businessTransactions
	 *            A collection of {@link BusinessTransactionData} instances.
	 */
	public void setBusinessTransactions(Collection<BusinessTransactionData> businessTransactions) {
		for (BusinessTransactionData businessTx : businessTransactions) {
			this.businessTransactions.add(businessTx);
			this.applications.add(businessTx.getApplication());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<ApplicationData> getApplications() {
		return applications;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<BusinessTransactionData> getBusinessTransactions() {
		return businessTransactions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<BusinessTransactionData> getBusinessTransactions(int applicationId) {
		Set<BusinessTransactionData> result = new HashSet<>();
		for (BusinessTransactionData businessTx : businessTransactions) {
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
	public void registerBusinessTransaction(BusinessTransactionData businessTransaction) {
		throw new UnsupportedOperationException("Business transactions cannot be registered on a storage!");
	}

}
