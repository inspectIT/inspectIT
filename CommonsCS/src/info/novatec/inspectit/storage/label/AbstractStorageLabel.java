package info.novatec.inspectit.storage.label;

import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

/**
 * The abstract class for all labels.
 * 
 * @author Ivan Senic
 * 
 * @param <V>
 *            Type of value hold by label.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries({ @NamedQuery(name = AbstractStorageLabel.FIND_ALL, query = "SELECT l FROM AbstractStorageLabel l"),
		@NamedQuery(name = AbstractStorageLabel.FIND_BY_LABEL_TYPE, query = "SELECT l FROM AbstractStorageLabel l WHERE l.storageLabelType=:storageLabelType") })
public abstract class AbstractStorageLabel<V> implements Serializable, Comparable<AbstractStorageLabel<?>> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 6285532039131921007L;

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_ALL = "AbstractStorageLabel.findAll";

	/**
	 * Constant for findAll query.
	 */
	public static final String FIND_BY_LABEL_TYPE = "AbstractStorageLabel.findByLabelType";

	/**
	 * Id of label for persistence purposes.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;

	/**
	 * Storage label type.
	 */
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = AbstractStorageLabelType.class)
	protected AbstractStorageLabelType<V> storageLabelType;

	/**
	 * No-arg constructor.
	 */
	public AbstractStorageLabel() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param storageLabelType
	 *            {@link AbstractStorageLabelType}
	 */
	public AbstractStorageLabel(AbstractStorageLabelType<V> storageLabelType) {
		this.storageLabelType = storageLabelType;
	}

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
	 * Gets {@link #storageLabelType}.
	 * 
	 * @return {@link #storageLabelType}
	 */
	public AbstractStorageLabelType<V> getStorageLabelType() {
		return storageLabelType;
	}

	/**
	 * Sets {@link #storageLabelType}.
	 * 
	 * @param storageLabelType
	 *            New value for {@link #storageLabelType}
	 */
	public void setStorageLabelType(AbstractStorageLabelType<V> storageLabelType) {
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
		AbstractStorageLabel<?> other = (AbstractStorageLabel<?>) obj;
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
		return this.getClass().getName().compareTo(other.getClass().getName());
	}

}
