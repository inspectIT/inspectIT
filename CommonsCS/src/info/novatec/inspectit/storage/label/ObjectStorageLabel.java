package info.novatec.inspectit.storage.label;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

/**
 * Type of the label that can hold arbitrary object as the value.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 */
public class ObjectStorageLabel<T extends Comparable<T>> extends AbstractStorageLabel<T> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -2763874070566920037L;

	/**
	 * Value of the label.
	 */
	private T value;

	/**
	 * Storage label type.
	 */
	private AbstractStorageLabelType<T> storageLabelType;

	/**
	 * No-arg constructor.
	 */
	public ObjectStorageLabel() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param value
	 *            Value of the label.
	 * @param storageLabelType
	 *            Type of the label.
	 */
	public ObjectStorageLabel(T value, AbstractStorageLabelType<T> storageLabelType) {
		this.value = value;
		this.storageLabelType = storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormatedValue() {
		return value.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractStorageLabelType<T> getStorageLabelType() {
		return storageLabelType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStorageLabelType(AbstractStorageLabelType<T> storageLabelType) {
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
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ObjectStorageLabel<?> other = (ObjectStorageLabel<?>) obj;
		if (storageLabelType == null) {
			if (other.storageLabelType != null) {
				return false;
			}
		} else if (!storageLabelType.equals(other.storageLabelType)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(AbstractStorageLabel<?> other) {
		int typeCompare = storageLabelType.compareTo(other.getStorageLabelType());
		if (0 != typeCompare) {
			return typeCompare;
		} else {
			if (!ObjectStorageLabel.class.isAssignableFrom(other.getClass())) {
				return super.compareTo(other);
			} else {
				ObjectStorageLabel<T> otherLabel = (ObjectStorageLabel<T>) other;
				return value.compareTo(otherLabel.getValue());
			}
		}
	}
}
