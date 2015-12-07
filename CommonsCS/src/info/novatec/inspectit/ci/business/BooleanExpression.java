package info.novatec.inspectit.ci.business;

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
public class BooleanExpression extends Expression {
	/**
	 *
	 */
	private static final long serialVersionUID = 2967400707062638277L;
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
		super();
		this.value = value;
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

}
