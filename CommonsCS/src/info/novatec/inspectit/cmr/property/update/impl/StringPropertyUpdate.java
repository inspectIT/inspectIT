package info.novatec.inspectit.cmr.property.update.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * {@link AbstractPropertyUpdate} for string property.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "string-property-update")
public class StringPropertyUpdate extends AbstractPropertyUpdate<String> {

	/**
	 * Update value.
	 */
	@XmlAttribute(name = "update-value", required = true)
	private String updateValue;

	/**
	 * No-arg constructor.
	 */
	protected StringPropertyUpdate() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property update is related to.
	 * @param updateValue
	 *            Updated value.
	 */
	public StringPropertyUpdate(SingleProperty<String> property, String updateValue) {
		super(property, updateValue);
	}

	/**
	 * Gets {@link #updateValue}.
	 * 
	 * @return {@link #updateValue}
	 */
	public String getUpdateValue() {
		return updateValue;
	}

	/**
	 * Sets {@link #updateValue}.
	 * 
	 * @param updateValue
	 *            New value for {@link #updateValue}
	 */
	protected void setUpdateValue(String updateValue) {
		this.updateValue = updateValue;
	}

}