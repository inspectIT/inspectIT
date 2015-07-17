package info.novatec.inspectit.storage.label;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

/**
 * Label that has a {@link Boolean} as a value.
 * 
 * @author Ivan Senic
 * 
 */
public class BooleanStorageLabel extends AbstractStorageLabel<Boolean> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 7318450928862869442L;

	/**
	 * Label value.
	 */
	private Boolean booleanValue;

	/**
	 * Storage label type.
	 */
	private AbstractStorageLabelType<Boolean> storageLabelType;

	/**
	 * Default constructor.
	 */
	public BooleanStorageLabel() {
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param booleanValue
	 *            Label value.
	 * @param storageLabelType
	 *            Label type.
	 */
	public BooleanStorageLabel(Boolean booleanValue, AbstractStorageLabelType<Boolean> storageLabelType) {
		this.booleanValue = booleanValue;
		this.storageLabelType = storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean getValue() {
		return booleanValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(Boolean value) {
		booleanValue = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormatedValue() {
		return booleanValue.toString();
	}

	/**
	 * Gets {@link #booleanValue}.
	 * 
	 * @return {@link #booleanValue}
	 */
	public Boolean getBooleanValue() {
		return booleanValue;
	}

	/**
	 * Sets {@link #booleanValue}.
	 * 
	 * @param booleanValue
	 *            New value for {@link #booleanValue}
	 */
	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	/**
	 * Gets {@link #storageLabelType}.
	 * 
	 * @return {@link #storageLabelType}
	 */
	public AbstractStorageLabelType<Boolean> getStorageLabelType() {
		return storageLabelType;
	}

	/**
	 * Sets {@link #storageLabelType}.
	 * 
	 * @param storageLabelType
	 *            New value for {@link #storageLabelType}
	 */
	public void setStorageLabelType(AbstractStorageLabelType<Boolean> storageLabelType) {
		this.storageLabelType = storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((booleanValue == null) ? 0 : booleanValue.hashCode());
		result = prime * result + ((storageLabelType == null) ? 0 : storageLabelType.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BooleanStorageLabel other = (BooleanStorageLabel) obj;
		if (booleanValue == null) {
			if (other.booleanValue != null) {
				return false;
			}
		} else if (!booleanValue.equals(other.booleanValue)) {
			return false;
		}
		if (storageLabelType == null) {
			if (other.storageLabelType != null) {
				return false;
			}
		} else if (!storageLabelType.equals(other.storageLabelType)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AbstractStorageLabel<?> other) {
		int typeCompare = storageLabelType.compareTo(other.getStorageLabelType());
		if (0 != typeCompare) {
			return typeCompare;
		} else {
			if (!BooleanStorageLabel.class.isAssignableFrom(other.getClass())) {
				return super.compareTo(other);
			} else {
				BooleanStorageLabel otherLabel = (BooleanStorageLabel) other;
				return booleanValue.compareTo(otherLabel.booleanValue);
			}
		}
	}

}
