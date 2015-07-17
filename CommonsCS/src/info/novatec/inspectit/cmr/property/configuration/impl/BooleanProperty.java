package info.novatec.inspectit.cmr.property.configuration.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.BooleanPropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Property holding boolean values.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "boolean-property")
public class BooleanProperty extends SingleProperty<Boolean> {

	/**
	 * Used value.
	 */
	@XmlAttribute(name = "used-value")
	private Boolean usedValue;

	/**
	 * Default value.
	 */
	@XmlAttribute(name = "default-value", required = true)
	private Boolean defaultValue;

	/**
	 * No-arg constructor.
	 */
	public BooleanProperty() {
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
	public BooleanProperty(String name, String description, String logicalName, Boolean defaultValue, boolean advanced, boolean serverRestartRequired) throws IllegalArgumentException {
		super(name, description, logicalName, defaultValue, advanced, serverRestartRequired);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractPropertyUpdate<Boolean> createPropertyUpdate(Boolean updateValue) {
		return new BooleanPropertyUpdate(this, updateValue);
	}

	/**
	 * Gets {@link #usedValue}.
	 * 
	 * @return {@link #usedValue}
	 */
	protected Boolean getUsedValue() {
		return usedValue;
	}

	/**
	 * Sets {@link #usedValue}.
	 * 
	 * @param usedValue
	 *            New value for {@link #usedValue}
	 */
	protected void setUsedValue(Boolean usedValue) {
		this.usedValue = usedValue;
	}

	/**
	 * Gets {@link #defaultValue}.
	 * 
	 * @return {@link #defaultValue}
	 */
	public Boolean getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets {@link #defaultValue}.
	 * 
	 * @param defaultValue
	 *            New value for {@link #defaultValue}
	 */
	protected void setDefaultValue(Boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean parseLiteral(String literal) {
		return Boolean.parseBoolean(literal);
	}

}
