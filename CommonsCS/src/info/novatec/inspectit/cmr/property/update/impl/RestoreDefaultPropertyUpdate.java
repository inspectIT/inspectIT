package info.novatec.inspectit.cmr.property.update.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Special type of property update that basically says restore defaults.
 * 
 * @author Ivan Senic
 * 
 * @param <V>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "restore-default-property-update")
public class RestoreDefaultPropertyUpdate<V> implements IPropertyUpdate<V> {

	/**
	 * Logical name of the property being updated.
	 */
	@XmlAttribute(name = "property-logical-name", required = true)
	private String propertyLogicalName;

	/**
	 * Default value of the property being update. Note that this field is transient, thus this
	 * value will never be flushed to the configuration updates XML.
	 */
	private transient V defaultValue;

	/**
	 * No-arg constructor.
	 */
	public RestoreDefaultPropertyUpdate() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            {@link SingleProperty} to create update for.
	 */
	public RestoreDefaultPropertyUpdate(SingleProperty<V> property) {
		if (null == property) {
			throw new IllegalArgumentException("Property can not be null.");
		}
		this.propertyLogicalName = property.getLogicalName();
		this.defaultValue = property.getDefaultValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRestoreDefault() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public V getUpdateValue() {
		return defaultValue;
	}

	/**
	 * {@inheritDoc}
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
		RestoreDefaultPropertyUpdate<?> other = (RestoreDefaultPropertyUpdate<?>) obj;
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
