package info.novatec.inspectit.cmr.property.configuration.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.StringPropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Property holding string values.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "string-property")
public class StringProperty extends SingleProperty<String> {

	/**
	 * Used value.
	 */
	@XmlAttribute(name = "used-value")
	private String usedValue;

	/**
	 * Default value.
	 */
	@XmlAttribute(name = "default-value", required = true)
	private String defaultValue;

	/**
	 * No-arg constructor.
	 */
	public StringProperty() {
	}

	/**
	 * 
	 * @param name
	 *            Display name of the property. Can not be <code>null</code>.
	 * @param description
	 *            Description providing more information on property.
	 * @param logicalName
	 *            The logical name of the property that is used in the configuration.
	 * @param defaultValue
	 *            Default value.
	 * @param advanced
	 *            If the property is advanced, thus should be available only to expert users.
	 * @param serverRestartRequired
	 *            If the change of this property should trigger server restart.
	 * @throws IllegalArgumentException
	 *             If name, section, logical name or default value are <code>null</code>.
	 * @see SingleProperty#SingleProperty(String, String, String, Object, boolean, boolean)
	 */
	public StringProperty(String name, String description, String logicalName, String defaultValue, boolean advanced, boolean serverRestartRequired) throws IllegalArgumentException {
		super(name, description, logicalName, defaultValue, advanced, serverRestartRequired);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractPropertyUpdate<String> createPropertyUpdate(String updateValue) {
		return new StringPropertyUpdate(this, updateValue);
	}

	/**
	 * Gets {@link #usedValue}.
	 * 
	 * @return {@link #usedValue}
	 */
	protected String getUsedValue() {
		return usedValue;
	}

	/**
	 * Sets {@link #usedValue}.
	 * 
	 * @param usedValue
	 *            New value for {@link #usedValue}
	 */
	protected void setUsedValue(String usedValue) {
		this.usedValue = usedValue;
	}

	/**
	 * Gets {@link #defaultValue}.
	 * 
	 * @return {@link #defaultValue}
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets {@link #defaultValue}.
	 * 
	 * @param defaultValue
	 *            New value for {@link #defaultValue}
	 */
	protected void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public String parseLiteral(String literal) {
		return literal;
	}

}
