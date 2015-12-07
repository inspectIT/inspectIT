package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.configuration.business.BusinessContextDefinition;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessContextDefinition;
import info.novatec.inspectit.cmr.service.IBusinessContextManagementService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import java.util.List;

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
	 * {@link BusinessContextDefinition} to get the information from.
	 */
	private IBusinessContextDefinition businessContextDefinition;

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
	public IBusinessContextDefinition getBusinessContextDefinition() {
		return businessContextDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IApplicationDefinition> getApplicationDefinitions() {
		return businessContextDefinition.getApplicationDefinitions();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addApplicationDefinition(IApplicationDefinition appDefinition, int insertBeforeIndex) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteApplicationDefinition(IApplicationDefinition appDefinition) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void moveApplicationDefinition(IApplicationDefinition appDefinition, int index) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateApplicationDefinition(IApplicationDefinition appDefinition) {
		throw new UnsupportedOperationException("Business context should not be changed on a storage!");
	}

	/**
	 * Sets {@link #businessContextDefinition}.
	 *
	 * @param businessContextDefinition
	 *            New value for {@link #businessContextDefinition}
	 */
	public void setBusinessContextDefinition(IBusinessContextDefinition businessContextDefinition) {
		this.businessContextDefinition = businessContextDefinition;
	}

}
