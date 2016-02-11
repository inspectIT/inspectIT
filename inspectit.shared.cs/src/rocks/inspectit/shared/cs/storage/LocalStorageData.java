package info.novatec.inspectit.storage;

import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.ArrayList;
import java.util.List;

/**
 * Local storage data holds all information about a storage that will be saved on the client
 * machine.
 * 
 * @author Ivan Senic
 * 
 */
public class LocalStorageData extends AbstractStorageData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -6129025210768974889L;

	/**
	 * Is the storage completely locally available (no connection to CMR needed).
	 */
	private boolean fullyDownloaded;

	/**
	 * List of labels. Read only.
	 */
	private List<AbstractStorageLabel<?>> labelList = new ArrayList<AbstractStorageLabel<?>>();

	/**
	 * No-argument constructor.
	 */
	public LocalStorageData() {
	}

	/**
	 * Creates a {@link LocalStorageData} from a corresponding {@link StorageData}.
	 * 
	 * @param storageData
	 *            {@link StorageData}.
	 */
	public LocalStorageData(StorageData storageData) {
		copyStorageDataInformation(storageData);
	}

	/**
	 * Copies the local storage data information in this local storage instance.
	 * 
	 * @param storageData
	 *            {@link StorageData} to copy information from.
	 */
	public final void copyStorageDataInformation(StorageData storageData) {
		this.setId(storageData.getId());
		this.setName(storageData.getName());
		this.setDescription(storageData.getDescription());
		this.setDiskSize(storageData.getDiskSize());
		this.setCmrVersion(storageData.getCmrVersion());
		this.labelList = storageData.getLabelList();

	}

	/**
	 * @return the fullyDownloaded
	 */
	public boolean isFullyDownloaded() {
		return fullyDownloaded;
	}

	/**
	 * @param fullyDownloaded
	 *            the fullyDownloaded to set
	 */
	public void setFullyDownloaded(boolean fullyDownloaded) {
		this.fullyDownloaded = fullyDownloaded;
	}

	/**
	 * Gets {@link #labelList}.
	 * 
	 * @return {@link #labelList}
	 */
	public List<AbstractStorageLabel<?>> getLabelList() {
		return labelList;
	}

	/**
	 * Return all labels of these storage that are of a given type.
	 * 
	 * @param <T>
	 *            Type
	 * @param labelType
	 *            Searched label type.
	 * @return List of labels.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<AbstractStorageLabel<T>> getLabels(AbstractStorageLabelType<T> labelType) {
		List<AbstractStorageLabel<T>> labels = new ArrayList<AbstractStorageLabel<T>>();
		for (AbstractStorageLabel<?> label : labelList) {
			if (label.getStorageLabelType().equals(labelType)) {
				labels.add((AbstractStorageLabel<T>) label);
			}
		}
		return labels;
	}

}
