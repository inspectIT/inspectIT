package info.novatec.inspectit.cmr.property.configuration;

import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;

/**
 * Root element of the XML configuration. Holding list of sections that define the configuration.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cmr-configuration")
public class Configuration {

	/**
	 * Sections that are pare of configuration.
	 */
	@XmlElementRef(type = PropertySection.class)
	private Set<PropertySection> sections = new HashSet<PropertySection>();

	/**
	 * Validates all properties in this configuration returning the map of containing the properties
	 * that have validation errors.
	 * 
	 * @return Map of properties with {@link PropertyValidation} containing errors.
	 */
	public Map<AbstractProperty, PropertyValidation> validate() {
		Map<AbstractProperty, PropertyValidation> validationMap = new HashMap<AbstractProperty, PropertyValidation>();
		for (AbstractProperty property : getAllProperties()) {
			PropertyValidation propertyValidation = property.validate();
			if (propertyValidation.hasErrors()) {
				validationMap.put(property, propertyValidation);
			}
		}
		return validationMap;
	}

	/**
	 * Returns the {@link SingleProperty} with the given logical name in the configuration or
	 * <code>null</code> if such does not exist.
	 * 
	 * @param <T>
	 *            Type of property value.
	 * 
	 * @param propertyLogicalName
	 *            Property logical name to search for.
	 * @return {@link SingleProperty} or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public <T> SingleProperty<T> forLogicalName(String propertyLogicalName) {
		for (AbstractProperty property : getAllProperties()) {
			SingleProperty<?> returnProperty = property.forLogicalname(propertyLogicalName);
			if (null != returnProperty) {
				return (SingleProperty<T>) returnProperty;
			}
		}
		return null;
	}

	/**
	 * Returns all properties in all sections.
	 * 
	 * @return Returns all properties in all sections.
	 */
	public Collection<AbstractProperty> getAllProperties() {
		Set<AbstractProperty> properties = new HashSet<AbstractProperty>();
		if (CollectionUtils.isNotEmpty(sections)) {
			for (PropertySection section : sections) {
				properties.addAll(section.getProperties());
			}
		}
		return properties;
	}

	/**
	 * Adds a sections to the section list.
	 * 
	 * @param section
	 *            {@link PropertySection} to add.
	 */
	public void addSection(PropertySection section) {
		sections.add(section);
	}

	/**
	 * Gets {@link #sections}.
	 * 
	 * @return {@link #sections}
	 */
	public Set<PropertySection> getSections() {
		return sections;
	}

	/**
	 * Sets {@link #sections}.
	 * 
	 * @param sections
	 *            New value for {@link #sections}
	 */
	public void setSections(Set<PropertySection> sections) {
		this.sections = sections;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sections == null) ? 0 : sections.hashCode());
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
		Configuration other = (Configuration) obj;
		if (sections == null) {
			if (other.sections != null) {
				return false;
			}
		} else if (!sections.equals(other.sections)) {
			return false;
		}
		return true;
	}

}
