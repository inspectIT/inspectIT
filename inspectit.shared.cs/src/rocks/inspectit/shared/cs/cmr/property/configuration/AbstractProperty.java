package info.novatec.inspectit.cmr.property.configuration;

import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;

import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract property class.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ SingleProperty.class, GroupedProperty.class })
public abstract class AbstractProperty {

	/**
	 * Display name of the property.
	 */
	@XmlAttribute(name = "name", required = true)
	private String name;

	/**
	 * Description providing more information on property.
	 */
	@XmlAttribute(name = "description")
	private String description;

	/**
	 * No-arg constructor.
	 */
	public AbstractProperty() {
	}

	/**
	 * Constructor that sets all attributes.
	 * 
	 * @param name
	 *            Display name of the property. Can not be <code>null</code>.
	 * @param description
	 *            Description providing more information on property.
	 * @throws IllegalArgumentException
	 *             If name is <code>null</code>.
	 */
	public AbstractProperty(String name, String description) throws IllegalArgumentException {
		if (null == name) {
			throw new IllegalArgumentException("Name of the property must not be null.");
		}
		this.name = name;
		this.description = description;
	}

	/**
	 * Returns if the property is advanced, thus should be available only to expert users.
	 * 
	 * @return Returns <code>true</code> is property is considered as advanced, <code>false</code>
	 *         otherwise.
	 */
	public abstract boolean isAdvanced();

	/**
	 * Returns if the change of this property should trigger server restart.
	 * 
	 * @return Returns <code>true</code> if changing of property should trigger server restart,
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean isServerRestartRequired();

	/**
	 * Executes the validation of the property and reports and validation break to the
	 * {@link PropertyValidation}.
	 * 
	 * @param propertyValidation
	 *            {@link PropertyValidation} to report errors during validation.
	 */
	protected abstract void validate(PropertyValidation propertyValidation);

	/**
	 * Adds the value/key property pair(s) contained in this property to the given
	 * {@link Properties} object.
	 * 
	 * @param properties
	 *            {@link Properties} to fill.
	 */
	public abstract void register(Properties properties);

	/**
	 * Returns {@link SingleProperty} for the given logical name if this property knows about it.
	 * 
	 * @param propertyLogicalName
	 *            Logical name to search for.
	 * @return {@link SingleProperty} or <code>null</code> if the property does not know about
	 *         single property with such logical name.
	 */
	public abstract SingleProperty<?> forLogicalname(String propertyLogicalName);

	/**
	 * Validate current property value(s) and report any validation problems via the
	 * {@link PropertyValidation} that is returned.
	 * 
	 * @return {@link PropertyValidation} containing validation errors if found.
	 */
	public PropertyValidation validate() {
		PropertyValidation errors = PropertyValidation.createFor(this);
		validate(errors);
		return errors;
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
	 * Gets {@link #description}.
	 * 
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

}
