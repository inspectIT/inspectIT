package info.novatec.inspectit.ci.business;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * NOT expression definition.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "not")
public class NotExpression extends Expression {

	/**
	 *
	 */
	private static final long serialVersionUID = -7975370192607561226L;

	/**
	 * Operand.
	 */
	@XmlElementRef(name = "operand")
	private Expression operand;

	/**
	 * Default Constructor.
	 */
	public NotExpression() {
	}

	/**
	 * Constructor.
	 *
	 * @param operand
	 *            expression to negate
	 */
	public NotExpression(Expression operand) {
		super();
		this.operand = operand;
	}

	/**
	 * Gets {@link #operand}.
	 *
	 * @return {@link #operand}
	 */
	public Expression getOperand() {
		return operand;
	}

	/**
	 * Sets {@link #operand}.
	 *
	 * @param operand
	 *            New value for {@link #operand}
	 */
	public void setOperand(Expression operand) {
		this.operand = operand;
	}

}
