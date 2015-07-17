package info.novatec.inspectit.cmr.property.configuration;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.collections.CollectionUtils;

/**
 * Class denoting one section of properties.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "section")
@XmlSeeAlso(AbstractProperty.class)
public class PropertySection {

	/**
	 * Name of the section.
	 */
	@XmlAttribute(name = "name", required = true)
	private String name;

	/**
	 * Properties belonging to the section.
	 */
	@XmlElementWrapper(name = "properties")
	@XmlElementRef
	private Set<AbstractProperty> properties = new LinkedHashSet<AbstractProperty>();

	/**
	 * No-arg constructor.
	 */
	public PropertySection() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            Name of the section.
	 */
	public PropertySection(String name) {
		this.name = name;
	}

	/**
	 * Returns <code>true</code> if at least one property in this section is marked as advanced,
	 * <code>false</code> otherwise.
	 * 
	 * @return Returns <code>true</code> if at least one property in this section is marked as
	 *         advanced, <code>false</code> otherwise.
	 */
	public boolean hasAdvancedProperties() {
		if (CollectionUtils.isNotEmpty(properties)) {
			for (AbstractProperty property : properties) {
				if (property.isAdvanced()) {
					return true;
				}
			}
		}
		return false;
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
	 * Gets {@link #properties}.
	 * 
	 * @return {@link #properties}
	 */
	public Set<AbstractProperty> getProperties() {
		return properties;
	}

	/**
	 * Add {@link AbstractProperty} to section.
	 * 
	 * @param property
	 *            {@link AbstractProperty}.
	 */
	public void addProperty(AbstractProperty property) {
		properties.add(property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
		PropertySection other = (PropertySection) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else if (!properties.equals(other.properties)) {
			return false;
		}
		return true;
	}

}
