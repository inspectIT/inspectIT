package info.novatec.inspectit.ci.business.expression.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.IContainerExpression;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

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
public class OrExpression extends AbstractExpression implements IContainerExpression {
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
		if (null == operands) {
			throw new IllegalArgumentException("Operands must not be null!");
		}
		for (AbstractExpression exp : operands) {
			this.operands.add(exp);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	public int getNumberOfChildExpressions() {
		return null == operands ? 0 : operands.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		for (AbstractExpression expr : getOperands()) {
			if (expr.evaluate(invocSequence, cachedDataService)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOperand(AbstractExpression operand) {
		getOperands().add(operand);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canAddOperand() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeOperand(AbstractExpression operand) {
		getOperands().remove(operand);
	}

}
