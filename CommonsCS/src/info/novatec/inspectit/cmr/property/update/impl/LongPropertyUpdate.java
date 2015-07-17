package info.novatec.inspectit.cmr.property.update.impl;

import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * {@link AbstractPropertyUpdate} for long property.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "long-property-update")
public class LongPropertyUpdate extends AbstractPropertyUpdate<Long> {

	/**
	 * Update value.
	 */
	@XmlAttribute(name = "update-value", required = true)
	private Long updateValue;

	/**
	 * No-arg constructor.
	 */
	protected LongPropertyUpdate() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param property
	 *            Property update is related to.
	 * @param updateValue
	 *            Updated value.
	 */
	public LongPropertyUpdate(SingleProperty<Long> property, Long updateValue) {
		super(property, updateValue);
	}

	/**
	 * Gets {@link #updateValue}.
	 * 
	 * @return {@link #updateValue}
	 */
	public Long getUpdateValue() {
		return updateValue;
	}

	/**
	 * Sets {@link #updateValue}.
	 * 
	 * @param updateValue
	 *            New value for {@link #updateValue}
	 */
	protected void setUpdateValue(Long updateValue) {
		this.updateValue = updateValue;
	}

}