package info.novatec.inspectit.storage;

import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.DateStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.label.type.impl.CreationDateLabelType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class that defines the storage information.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageData extends AbstractStorageData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -7907538407211424496L;

	/**
	 * List of labels.
	 */
	private List<AbstractStorageLabel<?>> labelList = new ArrayList<AbstractStorageLabel<?>>();

	/**
	 * {@link StorageState} that defines what operations can be done on storage.
	 */
	private StorageState state;

	/**
	 * Default constructor. Sets creation date to now.
	 */
	public StorageData() {
		state = StorageState.CREATED_NOT_OPENED;
		DateStorageLabel dateLabel = new DateStorageLabel();
		dateLabel.setStorageLabelType(new CreationDateLabelType());
		dateLabel.setDateValue(new Date());
		labelList.add(dateLabel);
	}

	/**
	 * Secondary constructor. Copies data from the given {@link StorageData}.
	 * 
	 * @param storageData
	 *            {@link StorageData} to copy information.
	 */
	public StorageData(IStorageData storageData) {
		setId(storageData.getId());
		setName(storageData.getName());
		setDescription(storageData.getDescription());
		setDiskSize(storageData.getDiskSize());
		setCmrVersion(storageData.getCmrVersion());
		labelList = new ArrayList<AbstractStorageLabel<?>>(storageData.getLabelList());
		if (storageData instanceof StorageData) {
			state = ((StorageData) storageData).getState(); // NOPMD
		} else {
			state = StorageState.CLOSED;
		}
	}

	/**
	 * Returns all labels of this {@link StorageData}.
	 * 
	 * @return Returns all labels of this {@link StorageData}.
	 */
	public List<AbstractStorageLabel<?>> getLabelList() {
		return labelList;
	}

	/**
	 * @return the state
	 */
	public StorageState getState() {
		return state;
	}

	/**
	 * 
	 * @return If the storage is opened, and thus available for writing.
	 */
	public boolean isStorageOpened() {
		return state.equals(StorageState.OPENED) || state.equals(StorageState.RECORDING);
	}

	/**
	 * Marks storage as open.
	 */
	public void markOpened() {
		state = StorageState.OPENED;
	}

	/**
	 * 
	 * @return If the storage is closed.
	 */
	public boolean isStorageClosed() {
		return state.equals(StorageState.CLOSED);
	}

	/**
	 * Marks storage as closed.
	 */
	public void markClosed() {
		state = StorageState.CLOSED;
	}

	/**
	 * 
	 * @return If the storage is closed.
	 */
	public boolean isStorageRecording() {
		return state.equals(StorageState.RECORDING);
	}

	/**
	 * Marks storage as closed.
	 */
	public void markRecording() {
		state = StorageState.RECORDING;
	}

	/**
	 * Adds a label to the label list of the storage data. The labels that are one per storage will
	 * be inserted only if the label of that type does not exists, or the overwrite flag is set to
	 * true.
	 * 
	 * @param label
	 *            Label to insert.
	 * @param doOverwrite
	 *            If this is true than the label of the same type will be overwritten in the list if
	 *            it already exists. Otherwise the label will not be inserted.
	 */
	public void addLabel(AbstractStorageLabel<?> label, boolean doOverwrite) {
		if (label.getStorageLabelType().isOnePerStorage()) {
			boolean exists = false;
			for (AbstractStorageLabel<?> existingLabel : labelList) {
				if (label.getStorageLabelType().equals(existingLabel.getStorageLabelType())) {
					exists = true;
					if (doOverwrite) {
						labelList.remove(existingLabel);
						labelList.add(label);
					}
					break;
				}
			}
			if (!exists) {
				labelList.add(label);
			}
		} else if (!labelList.contains(label)) {
			labelList.add(label);
		}
	}

	/**
	 * Removes label from the label list.
	 * 
	 * @param label
	 *            Label to remove.
	 * @return True if the label was removed.
	 * @see List#remove(Object)
	 */
	public boolean removeLabel(AbstractStorageLabel<?> label) {
		return labelList.remove(label);
	}

	/**
	 * Return if the label of provided type is present in the label list.
	 * 
	 * @param labelType
	 *            Type to search for.
	 * @return True if one of more labels of desired type is present, false otherwise.
	 */
	public boolean isLabelPresent(AbstractStorageLabelType<?> labelType) {
		for (AbstractStorageLabel<?> label : labelList) {
			if (label.getStorageLabelType().equals(labelType)) {
				return true;
			}
		}
		return false;
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

	/**
	 * Enum that defines the storage state.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum StorageState {

		/**
		 * Just created, not opened yet, thus writing not possible.
		 */
		CREATED_NOT_OPENED,

		/**
		 * Opened, ready for writing.
		 */
		OPENED,

		/**
		 * Opened, and currently recording.
		 */
		RECORDING,

		/**
		 * Closed, not allowed for writing, reading possible.
		 */
		CLOSED;
	}

}
