package rocks.inspectit.ui.rcp.model.storage;

import org.eclipse.core.runtime.Assert;

import com.google.common.base.Objects;

import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.model.Leaf;
import rocks.inspectit.ui.rcp.provider.IStorageDataProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Leaf used for displaying the storages in the storage tree.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageLeaf extends Leaf implements IStorageDataProvider {

	/**
	 * {@link StorageData}.
	 */
	private StorageData storageData;

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 * 
	 * @param storageData
	 *            {@link StorageData}
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 */
	public StorageLeaf(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		super();
		Assert.isNotNull(storageData);
		Assert.isNotNull(cmrRepositoryDefinition);
		this.storageData = storageData;
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.setName(storageData.getName());
		this.setImage(ImageFormatter.getImageForStorageLeaf(storageData));
		this.setTooltip(storageData.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	public StorageData getStorageData() {
		return storageData;
	}

	/**
	 * {@inheritDoc}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(storageData, cmrRepositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		if (!super.equals(object)) {
			return false;
		}
		StorageLeaf that = (StorageLeaf) object;
		return Objects.equal(this.storageData, that.storageData) && Objects.equal(this.cmrRepositoryDefinition, that.cmrRepositoryDefinition);
	}

}
