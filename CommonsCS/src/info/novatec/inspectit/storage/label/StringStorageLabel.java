package info.novatec.inspectit.storage.label;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

/**
 * Label that has a {@link String} as a value.
 * 
 * @author Ivan Senic
 * 
 */
public class StringStorageLabel extends AbstractStorageLabel<String> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -879178622863569380L;

	/**
	 * Label value.
	 */
	private String stringValue;

	/**
	 * Storage label type.
	 */
	private AbstractStorageLabelType<String> storageLabelType;

	/**
	 * Default constructor.
	 */
	public StringStorageLabel() {
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param stringValue
	 *            Label value.
	 * @param storageLabelType
	 *            Label Type.
	 */
	public StringStorageLabel(String stringValue, AbstractStorageLabelType<String> storageLabelType) {
		super();
		this.stringValue = stringValue;
		this.storageLabelType = storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValue() {
		return stringValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(String value) {
		this.stringValue = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormatedValue() {
		return stringValue;
	}

	/**
	 * @return the stringValue
	 */
	public String getStringValue() {
		return stringValue;
	}

	/**
	 * @param stringValue
	 *            the stringValue to set
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	/**
	 * Gets {@link #storageLabelType}.
	 * 
	 * @return {@link #storageLabelType}
	 */
	public AbstractStorageLabelType<String> getStorageLabelType() {
		return storageLabelType;
	}

	/**
	 * Sets {@link #storageLabelType}.
	 * 
	 * @param storageLabelType
	 *            New value for {@link #storageLabelType}
	 */
	public void setStorageLabelType(AbstractStorageLabelType<String> storageLabelType) {
		this.storageLabelType = storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((storageLabelType == null) ? 0 : storageLabelType.hashCode());
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
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
		StringStorageLabel other = (StringStorageLabel) obj;
		if (storageLabelType == null) {
			if (other.storageLabelType != null) {
				return false;
			}
		} else if (!storageLabelType.equals(other.storageLabelType)) {
			return false;
		}
		if (stringValue == null) {
			if (other.stringValue != null) {
				return false;
			}
		} else if (!stringValue.equals(other.stringValue)) {
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
			if (!StringStorageLabel.class.isAssignableFrom(other.getClass())) {
				return super.compareTo(other);
			} else {
				StringStorageLabel otherLabel = (StringStorageLabel) other;
				return stringValue.compareToIgnoreCase(otherLabel.stringValue);
			}
		}
	}

}
