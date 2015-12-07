package info.novatec.inspectit.ci.business.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * OR expression definition. Allows to disjunct multiple expressions.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "or")
public class OrExpression extends AbstractExpression {
	/**
	 * List of expressions constituting the operands of the disjunction expression.
	 */
	@XmlElementWrapper(name = "operands")
	@XmlElementRef(type = AbstractExpression.class)
	private List<AbstractExpression> operands = new ArrayList<AbstractExpression>(2);

	/**
	 * Default Constructor.
	 */
	public OrExpression() {
	}

	/**
	 * Constructor.
	 *
	 * @param operands
	 *            set of operands to be used in the OR disjunction
	 */
	public OrExpression(AbstractExpression... operands) {
		for (AbstractExpression exp : operands) {
			this.operands.add(exp);
		}

	}

	/**
	 * Gets {@link #operands}.
	 *
	 * @return {@link #operands}
	 */
	public List<AbstractExpression> getOperands() {
		return operands;
	}

	/**
	 * Sets {@link #operands}.
	 *
	 * @param operands
	 *            New value for {@link #operands}
	 */
	public void setOperands(List<AbstractExpression> operands) {
		this.operands = operands;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSupportedNumberOfChildExpressions() {
		return Integer.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfChildExpressions() {
		return null == operands ? 0 : operands.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExpressionType getExpressionType() {
		return ExpressionType.OR;
	}

}
