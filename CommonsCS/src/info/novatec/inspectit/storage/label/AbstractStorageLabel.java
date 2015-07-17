package info.novatec.inspectit.storage.label;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.io.Serializable;

/**
 * The abstract class for all labels.
 * 
 * @author Ivan Senic
 * 
 * @param <V>
 *            Type of value hold by label.
 */
public abstract class AbstractStorageLabel<V> implements Serializable, Comparable<AbstractStorageLabel<?>> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6285532039131921007L;

	/**
	 * Id of label for persistence purposes.
	 */
	private int id;

	/**
	 * Returns object that represent the value of label.
	 * 
	 * @return Returns object that represent the value of label.
	 */
	public abstract V getValue();

	/**
	 * Sets the value. implementing classes need to perform all checks for the value to be accepted.
	 * 
	 * @param value
	 *            New value.
	 */
	public abstract void setValue(V value);

	/**
	 * Returns the formated value of the label.
	 * 
	 * @return Returns the formated value of the label.
	 */
	public abstract String getFormatedValue();

	/**
	 * Returns the {@link AbstractStorageLabelType}.
	 * 
	 * @return Returns the {@link AbstractStorageLabelType}.
	 */
	public abstract AbstractStorageLabelType<V> getStorageLabelType();

	/**
	 * Sets {@link #AbstractStorageLabelType}.
	 * 
	 * @param storageLabelType
	 *            New value for {@link #AbstractStorageLabelType}
	 */
	public abstract void setStorageLabelType(AbstractStorageLabelType<V> storageLabelType);

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(AbstractStorageLabel<?> other) {
		return this.getClass().getName().compareTo(other.getClass().getName());
	}

}
