package info.novatec.inspectit.storage.label.type;

import java.io.Serializable;

/**
 * Abstract class for all custom storage label types.
 * 
 * @author Ivan Senic
 * 
 * @param <V>
 *            Type of value label is holding.
 */
public abstract class AbstractCustomStorageLabelType<V> extends AbstractStorageLabelType<V> implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 4468771008746395551L;

	/**
	 * If is one per storage.
	 */
	private boolean onePerStorage;

	/**
	 * Name of this custom label type.
	 */
	private String name;

	/**
	 * Key of the image that will be used for the label.
	 */
	private String imageKey;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValueReusable() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMultiType() {
		return true;
	}

	/**
	 * Gets {@link #onePerStorage}.
	 * 
	 * @return {@link #onePerStorage}
	 */
	@Override
	public boolean isOnePerStorage() {
		return onePerStorage;
	}

	/**
	 * Sets {@link #onePerStorage}.
	 * 
	 * @param onePerStorage
	 *            New value for {@link #onePerStorage}
	 */
	public void setOnePerStorage(boolean onePerStorage) {
		this.onePerStorage = onePerStorage;
	}

	/**
	 * Gets {@link #name}.
	 * 
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name}.
	 * 
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #imageKey}.
	 * 
	 * @return {@link #imageKey}
	 */
	public String getImageKey() {
		return imageKey;
	}

	/**
	 * Sets {@link #imageKey}.
	 * 
	 * @param imageKey
	 *            New value for {@link #imageKey}
	 */
	public void setImageKey(String imageKey) {
		this.imageKey = imageKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((imageKey == null) ? 0 : imageKey.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (onePerStorage ? 1231 : 1237);
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractCustomStorageLabelType<?> other = (AbstractCustomStorageLabelType<?>) obj;
		if (imageKey == null) {
			if (other.imageKey != null) {
				return false;
			}
		} else if (!imageKey.equals(other.imageKey)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (onePerStorage != other.onePerStorage) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(AbstractStorageLabelType<?> other) {
		if (!AbstractCustomStorageLabelType.class.isAssignableFrom(other.getClass())) {
			return super.compareTo(other);
		} else {
			AbstractCustomStorageLabelType<?> abstractCustomStorageLabelType = (AbstractCustomStorageLabelType<?>) other;
			return name.compareTo(abstractCustomStorageLabelType.getName());
		}
	}

}
