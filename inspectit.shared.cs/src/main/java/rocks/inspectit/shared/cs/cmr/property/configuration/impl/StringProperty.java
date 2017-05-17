package rocks.inspectit.shared.cs.cmr.property.configuration.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.impl.StringPropertyUpdate;

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
	 * If property is a password.
	 */
	@XmlAttribute(name = "password")
	private Boolean password = Boolean.FALSE;

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
	@Override
	protected String getUsedValue() {
		return usedValue;
	}

	/**
	 * Sets {@link #usedValue}.
	 *
	 * @param usedValue
	 *            New value for {@link #usedValue}
	 */
	@Override
	protected void setUsedValue(String usedValue) {
		this.usedValue = usedValue;
	}

	/**
	 * Gets {@link #defaultValue}.
	 *
	 * @return {@link #defaultValue}
	 */
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets {@link #defaultValue}.
	 *
	 * @param defaultValue
	 *            New value for {@link #defaultValue}
	 */
	@Override
	protected void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String parseLiteral(String literal) {
		return literal;
	}

	/**
	 * Gets {@link #password}.
	 *
	 * @return {@link #password}
	 */
	public boolean isPassword() {
		return this.password;
	}

}
