package info.novatec.inspectit.cmr.property.update.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * {@link AbstractPropertyUpdate} for percentage property.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "percentage-property-update")
public class PercentagePropertyUpdate extends AbstractPropertyUpdate<Float> {

	/**
	 * Update value.
	 */
	@XmlAttribute(name = "update-value", required = true)
	private Float updateValue;

	/**
	 * No-arg constructor.
	 */
	protected PercentagePropertyUpdate() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property update is related to.
	 * @param updateValue
	 *            Updated value.
	 */
	public PercentagePropertyUpdate(SingleProperty<Float> property, Float updateValue) {
		super(property, updateValue);
	}

	/**
	 * Gets {@link #updateValue}.
	 * 
	 * @return {@link #updateValue}
	 */
	public Float getUpdateValue() {
		return Float.valueOf(updateValue.floatValue() / 100f);
	}

	/**
	 * Sets {@link #updateValue}.
	 * 
	 * @param updateValue
	 *            New value for {@link #updateValue}
	 */
	protected void setUpdateValue(Float updateValue) {
		this.updateValue = Float.valueOf(updateValue.floatValue() * 100f);
	}

}