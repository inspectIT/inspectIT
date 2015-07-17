package info.novatec.inspectit.cmr.property.update;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.impl.BooleanPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.BytePropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.LongPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.PercentagePropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.StringPropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract class representing the property update.
 * 
 * @author Ivan Senic
 * 
 * @param <V>
 *            Type of property and update value.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ StringPropertyUpdate.class, LongPropertyUpdate.class, BooleanPropertyUpdate.class, PercentagePropertyUpdate.class, BytePropertyUpdate.class })
public abstract class AbstractPropertyUpdate<V> implements IPropertyUpdate<V> {

	/**
	 * Logical name of the property being updated.
	 */
	@XmlAttribute(name = "property-logical-name", required = true)
	private String propertyLogicalName;

	/**
	 * No-arg constructor.
	 */
	public AbstractPropertyUpdate() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property update is related to.
	 * @param updateValue
	 *            Updated value.
	 */
	public AbstractPropertyUpdate(SingleProperty<V> property, V updateValue) {
		if (null == property) {
			throw new IllegalArgumentException("Property can not be null.");
		}
		if (null == updateValue) {
			throw new IllegalArgumentException("Update of the property can not be null.");
		}
		this.propertyLogicalName = property.getLogicalName();
		setUpdateValue(updateValue);
	}

	/**
	 * Gets the update value.
	 * 
	 * @return Gets the update value.
	 */
	public abstract V getUpdateValue();

	/**
	 * Sets the currently used value.
	 * 
	 * @param updateValue
	 *            Update value for the property.
	 */
	protected abstract void setUpdateValue(V updateValue);

	/**
	 * {@inheritDoc}
	 */
	public boolean isRestoreDefault() {
		return false;
	}

	/**
	 * Gets {@link #propertyLogicalName}.
	 * 
	 * @return {@link #propertyLogicalName}
	 */
	public String getPropertyLogicalName() {
		return propertyLogicalName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyLogicalName == null) ? 0 : propertyLogicalName.hashCode());
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
		AbstractPropertyUpdate<?> other = (AbstractPropertyUpdate<?>) obj;
		if (propertyLogicalName == null) {
			if (other.propertyLogicalName != null) {
				return false;
			}
		} else if (!propertyLogicalName.equals(other.propertyLogicalName)) {
			return false;
		}
		return true;
	}

}
