package rocks.inspectit.shared.cs.ci.business.expression;

import java.util.List;

/**
 * This interface provides a common access to {@link AbstractExpression} instances that serve as
 * container expression (i.e. expressions that contain other expressions).
 *
 * @author Alexander Wert
 *
 */
public interface IContainerExpression {

	/**
	 * Adds an operand to this container expression.
	 *
	 * @param operand
	 *            {@link AbstractExpression} instance to add as operand
	 */
	void addOperand(AbstractExpression operand);

	/**
	 * Indicates whether an operand can be added.
	 *
	 * @return true, if operand can be added, otherwise false.
	 */
	boolean canAddOperand();

	/**
	 * Returns the list of operands ({@link AbstractExpression} instances).
	 *
	 * @return Returns the list of operands ({@link AbstractExpression} instances).
	 */
	List<AbstractExpression> getOperands();

	/**
	 * Removes the given operand from this container expression.
	 *
	 * @param operand
	 *            {@link AbstractExpression} instance to be removed
	 */
	void removeOperand(AbstractExpression operand);

	/**
	 * Returns the number of child expression currently attached to this expression.
	 *
	 * @return The number of child expression currently attached to this expression.
	 */
	int getNumberOfChildExpressions();
}
