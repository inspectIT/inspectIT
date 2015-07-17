package info.novatec.inspectit.cmr.property.update.configuration;

import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.RestoreDefaultPropertyUpdate;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.collections.CollectionUtils;

/**
 * Class holding all the {@link AbstractPropertyUpdate}s for one configuration.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cmr-configuration-update")
@XmlSeeAlso({ AbstractPropertyUpdate.class, RestoreDefaultPropertyUpdate.class })
public class ConfigurationUpdate {

	/**
	 * All updates.
	 */
	@XmlElementWrapper(name = "updates")
	@XmlElementRefs({ @XmlElementRef(type = AbstractPropertyUpdate.class), @XmlElementRef(type = RestoreDefaultPropertyUpdate.class) })
	private Set<IPropertyUpdate<?>> propertyUpdates = new HashSet<IPropertyUpdate<?>>();

	/**
	 * Merges two configuration updates. If overwrite is set to <code>true</code> and there is a
	 * same property in this and other {@link ConfigurationUpdate}, then value from the other will
	 * be used. If <code>false</code> is passed, this {@link ConfigurationUpdate} object will only
	 * merge non-existing properties from the other configuration.
	 * 
	 * @param other
	 *            Other {@link ConfigurationUpdate}
	 * @param overwrite
	 *            Should properties from this update be overwritten with values in the other update.
	 */
	public void merge(ConfigurationUpdate other, boolean overwrite) {
		if (CollectionUtils.isNotEmpty(other.getPropertyUpdates())) {
			for (IPropertyUpdate<?> propertyUpdate : other.getPropertyUpdates()) {
				IPropertyUpdate<?> thisPropertyUpdate = this.forLogicalName(propertyUpdate.getPropertyLogicalName());
				if (null != thisPropertyUpdate) {
					if (overwrite) {
						this.removePropertyUpdate(thisPropertyUpdate);
						this.addPropertyUpdate(propertyUpdate);
					}
				} else {
					this.addPropertyUpdate(propertyUpdate);
				}
			}
		}
	}

	/**
	 * Returns the {@link IPropertyUpdate} for the given property logical name if such update is
	 * existing in the configuration.
	 * 
	 * @param propertyLogicalName
	 *            Logical name of property.
	 * @return {@link AbstractPropertyUpdate} or <code>null</code>.
	 */
	public IPropertyUpdate<?> forLogicalName(String propertyLogicalName) {
		if (CollectionUtils.isNotEmpty(propertyUpdates)) {
			for (IPropertyUpdate<?> propertyUpdate : propertyUpdates) {
				if (ObjectUtils.equals(propertyLogicalName, propertyUpdate.getPropertyLogicalName())) {
					return propertyUpdate;
				}
			}
		}
		return null;
	}

	/**
	 * Adds {@link IPropertyUpdate} to this configuration update.
	 * 
	 * @param propertyUpdate
	 *            {@link IPropertyUpdate} to add.
	 */
	public void addPropertyUpdate(IPropertyUpdate<?> propertyUpdate) {
		propertyUpdates.add(propertyUpdate);
	}

	/**
	 * Removes {@link IPropertyUpdate} to this configuration update.
	 * 
	 * @param propertyUpdate
	 *            {@link IPropertyUpdate} to remove.
	 */
	public void removePropertyUpdate(IPropertyUpdate<?> propertyUpdate) {
		propertyUpdates.remove(propertyUpdate);
	}

	/**
	 * Gets {@link #propertyUpdates}.
	 * 
	 * @return {@link #propertyUpdates}
	 */
	public Set<IPropertyUpdate<?>> getPropertyUpdates() {
		return propertyUpdates;
	}

	/**
	 * Sets {@link #propertyUpdates}.
	 * 
	 * @param propertyUpdates
	 *            New value for {@link #propertyUpdates}
	 */
	public void setPropertyUpdates(Set<IPropertyUpdate<?>> propertyUpdates) {
		this.propertyUpdates = propertyUpdates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((propertyUpdates == null) ? 0 : propertyUpdates.hashCode());
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
		ConfigurationUpdate other = (ConfigurationUpdate) obj;
		if (propertyUpdates == null) {
			if (other.propertyUpdates != null) {
				return false;
			}
		} else if (!propertyUpdates.equals(other.propertyUpdates)) {
			return false;
		}
		return true;
	}

}
