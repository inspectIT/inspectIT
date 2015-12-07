package info.novatec.inspectit.ci.business.expression.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This {@link BooleanExpression} is evaluated by returning the boolean value.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "boolean")
public class BooleanExpression extends AbstractExpression {
	/**
	 * Boolean value.
	 */
	@XmlAttribute(name = "boolean-value", required = true)
	private boolean value;

	/**
	 * Default constructor.
	 */
	public BooleanExpression() {
	}

	/**
	 * Constructor.
	 *
	 * @param value
	 *            boolean value to use for evaluation
	 */
	public BooleanExpression(boolean value) {
		this.value = value;
	}

	/**
	 * Constructor.
	 *
	 * @param value
	 *            boolean value to use for evaluation
	 * @param advanced
	 *            indicates whether this expression has been created in advanced mode.
	 */
	public BooleanExpression(boolean value, boolean advanced) {
		this.value = value;
		this.setAdvanced(advanced);
	}

	/**
	 * Gets {@link #value}.
	 *
	 * @return {@link #value}
	 */
	public boolean isValue() {
		return value;
	}

	/**
	 * Sets {@link #value}.
	 *
	 * @param value
	 *            New value for {@link #value}
	 */
	public void setValue(boolean value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		return isValue();
	}

}
