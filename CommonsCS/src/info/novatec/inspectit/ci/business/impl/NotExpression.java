package info.novatec.inspectit.ci.business.impl;

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
public class NotExpression extends AbstractExpression {
	/**
	 * Operand.
	 */
	@XmlElementRef(name = "operand", type = AbstractExpression.class)
	private AbstractExpression operand;

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
	public NotExpression(AbstractExpression operand) {
		this.operand = operand;
	}

	/**
	 * Gets {@link #operand}.
	 *
	 * @return {@link #operand}
	 */
	public AbstractExpression getOperand() {
		return operand;
	}

	/**
	 * Sets {@link #operand}.
	 *
	 * @param operand
	 *            New value for {@link #operand}
	 */
	public void setOperand(AbstractExpression operand) {
		this.operand = operand;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSupportedNumberOfChildExpressions() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfChildExpressions() {
		return null == operand ? 0 : 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExpressionType getExpressionType() {
		return ExpressionType.NOT;
	}

}
