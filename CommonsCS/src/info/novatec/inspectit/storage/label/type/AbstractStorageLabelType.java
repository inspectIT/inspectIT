package info.novatec.inspectit.storage.label.type;

import info.novatec.inspectit.storage.StorageData;

import java.io.Serializable;

/**
 * Abstract class for all storage label types.
 * 
 * @author Ivan Senic
 * 
 * @param <V>
 *            Type of value label is holding.
 */
public abstract class AbstractStorageLabelType<V> implements Serializable, Comparable<AbstractStorageLabelType<?>> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8790699289978448114L;

	/**
	 * Id of label type for persistence purposes.
	 */
	private int id;

	/**
	 * Define if type of the label can only exists one in a {@link StorageData}.
	 * 
	 * @return True if only one type of the label is allowed per {@link StorageData}.
	 */
	public abstract boolean isOnePerStorage();

	/**
	 * Defines if the values that can be assigned with this label type are reusable. In other words
	 * the labels values that are not reusable will not be saved in the CMR label manager.
	 * 
	 * @return If the values of the label are reusable.
	 */
	public abstract boolean isValueReusable();

	/**
	 * Defines if the many instances of the implementing classes can created and saved to the DB. In
	 * other words if class in not "MultiType" than only one object of that class will be saved to
	 * the CMR database.
	 * 
	 * @return Returns if the many instances of the implementing classes can created and saved to
	 *         the DB.
	 */
	public abstract boolean isMultiType();

	/**
	 * Returns the value class.
	 * 
	 * @return Returns the value class.
	 */
	public abstract Class<V> getValueClass();

	/**
	 * If the value of the label type can be edited by the user. Returns <code>true</code> by
	 * default, sub-classes may override.
	 * 
	 * @return If the value of the label type can be edited by the user.
	 */
	public boolean isEditable() {
		return true;
	}

	/**
	 * If the labels of this type can be grouped. Returns <code>true</code> by default, sub-classes
	 * may override.
	 * 
	 * @return If the labels of this type can be grouped.
	 */
	public boolean isGroupingEnabled() {
		return true;
	}

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
	public int compareTo(AbstractStorageLabelType<?> other) {
		return this.getClass().getName().compareTo(other.getClass().getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.getClass().hashCode();
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
		return true;
	}

}
