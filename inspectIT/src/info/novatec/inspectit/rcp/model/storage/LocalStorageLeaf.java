package info.novatec.inspectit.rcp.model.storage;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.model.Leaf;
import info.novatec.inspectit.rcp.provider.ILocalStorageDataProvider;
import info.novatec.inspectit.storage.LocalStorageData;

import org.eclipse.core.runtime.Assert;

import com.google.common.base.Objects;

/**
 * Leaf used for displaying the local storages in the storage tree.
 * 
 * @author Ivan Senic
 * 
 */
public class LocalStorageLeaf extends Leaf implements ILocalStorageDataProvider {

	/**
	 * {@link LocalStorageData}.
	 */
	private LocalStorageData localStorageData;

	/**
	 * Default constructor.
	 * 
	 * @param localStorageData
	 *            {@link LocalStorageData} leaf to hold.
	 */
	public LocalStorageLeaf(LocalStorageData localStorageData) {
		super();
		Assert.isNotNull(localStorageData);
		this.localStorageData = localStorageData;
		this.setName(localStorageData.getName());
		this.setTooltip(localStorageData.getName());
		this.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_STORAGE_DOWNLOADED));
	}

	/**
	 * Gets {@link #localStorageData}.
	 * 
	 * @return {@link #localStorageData}
	 */
	public LocalStorageData getLocalStorageData() {
		return localStorageData;
	}

	/**
	 * Sets {@link #localStorageData}.
	 * 
	 * @param localStorageData
	 *            New value for {@link #localStorageData}
	 */
	public void setLocalStorageData(LocalStorageData localStorageData) {
		this.localStorageData = localStorageData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(localStorageData);
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
		LocalStorageLeaf that = (LocalStorageLeaf) object;
		return Objects.equal(this.localStorageData, that.localStorageData);
	}

}
