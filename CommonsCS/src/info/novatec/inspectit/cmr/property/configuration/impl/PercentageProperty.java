package info.novatec.inspectit.cmr.property.configuration.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.PercentagePropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Property holding float values. This property writes and reads the values from 0-100%, but reports
 * them in decimal format (0-1).
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "percentage-property")
public class PercentageProperty extends SingleProperty<Float> {

	/**
	 * Used value.
	 */
	@XmlAttribute(name = "used-value")
	private Float usedValue;

	/**
	 * Default value.
	 */
	@XmlAttribute(name = "default-value", required = true)
	private Float defaultValue;

	/**
	 * No-arg constructor.
	 */
	public PercentageProperty() {
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
	public PercentageProperty(String name, String description, String logicalName, Float defaultValue, boolean advanced, boolean serverRestartRequired) throws IllegalArgumentException {
		super(name, description, logicalName, defaultValue, advanced, serverRestartRequired);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AbstractPropertyUpdate<Float> createPropertyUpdate(Float updateValue) {
		return new PercentagePropertyUpdate(this, updateValue);
	}

	/**
	 * Gets {@link #usedValue}.
	 * 
	 * @return {@link #usedValue}
	 */
	protected Float getUsedValue() {
		if (null != usedValue) {
			return Float.valueOf(usedValue.floatValue() / 100f);
		}
		return null;
	}

	/**
	 * Sets {@link #usedValue}.
	 * 
	 * @param usedValue
	 *            New value for {@link #usedValue}
	 */
	protected void setUsedValue(Float usedValue) {
		if (null != usedValue) {
			this.usedValue = Float.valueOf(usedValue.floatValue() * 100f);
		} else {
			this.usedValue = null; // NOPMD
		}
	}

	/**
	 * Gets {@link #defaultValue}.
	 * 
	 * @return {@link #defaultValue}
	 */
	public Float getDefaultValue() {
		return Float.valueOf(defaultValue.floatValue() / 100f);
	}

	/**
	 * Sets {@link #defaultValue}.
	 * 
	 * @param defaultValue
	 *            New value for {@link #defaultValue}
	 */
	protected void setDefaultValue(Float defaultValue) {
		this.defaultValue = Float.valueOf(defaultValue.floatValue() * 100f);
	}

	/**
	 * {@inheritDoc}
	 */
	public Float parseLiteral(String literal) {
		try {
			return Float.parseFloat(literal);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedValue() {
		if (null != usedValue) {
			return usedValue + "%";
		} else {
			return defaultValue + "%";
		}
	}

}
