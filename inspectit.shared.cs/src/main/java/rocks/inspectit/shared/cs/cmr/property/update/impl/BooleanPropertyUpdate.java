package rocks.inspectit.shared.cs.cmr.property.update.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;

/**
 * {@link AbstractPropertyUpdate} for boolean property.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "boolean-property-update")
public class BooleanPropertyUpdate extends AbstractPropertyUpdate<Boolean> {

	/**
	 * Update value.
	 */
	@XmlAttribute(name = "update-value", required = true)
	private Boolean updateValue;

	/**
	 * No-arg constructor.
	 */
	protected BooleanPropertyUpdate() {
	}

	/**
	 * Default constructor.
	 *
	 * @param property
	 *            Property update is related to.
	 * @param updateValue
	 *            Updated value.
	 */
	public BooleanPropertyUpdate(SingleProperty<Boolean> property, Boolean updateValue) {
		super(property, updateValue);
	}

	/**
	 * Gets {@link #updateValue}.
	 *
	 * @return {@link #updateValue}
	 */
	@Override
	public Boolean getUpdateValue() {
		return updateValue;
	}

	/**
	 * Sets {@link #updateValue}.
	 *
	 * @param updateValue
	 *            New value for {@link #updateValue}
	 */
	@Override
	protected void setUpdateValue(Boolean updateValue) {
		this.updateValue = updateValue;
	}

}