package rocks.inspectit.shared.cs.ci.business.expression.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.IContainerExpression;

/**
 * AND expression definition. Allows to conjunct multiple expressions.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "and")
public class AndExpression extends AbstractExpression implements IContainerExpression {
	/**
	 * List of expressions constituting the operands of the conjunction expression.
	 */
	@XmlElementWrapper(name = "operands")
	@XmlElementRef(type = AbstractExpression.class, required = false)
	private List<AbstractExpression> operands = new ArrayList<AbstractExpression>(2);

	/**
	 * Default Constructor.
	 */
	public AndExpression() {
	}

	/**
	 * Constructor.
	 *
	 * @param operands
	 *            set of operands to be used in the AND conjunction
	 */
	public AndExpression(AbstractExpression... operands) {
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
			if (!expr.evaluate(invocSequence, cachedDataService)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addOperand(AbstractExpression operand) {
		if (canAddOperand() && !getOperands().contains(operand)) {
			getOperands().add(operand);
		}
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
