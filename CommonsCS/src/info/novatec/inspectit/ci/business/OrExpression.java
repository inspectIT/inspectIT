package info.novatec.inspectit.ci.business;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * OR expression definition. Allows to disjunct multiple expressions.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "or")
public class OrExpression extends Expression {

	/**
	 *
	 */
	private static final long serialVersionUID = 4617962590412580851L;

	/**
	 * List of expressions constituting the operands of the disjunction expression.
	 */
	@XmlElementRef
	private List<Expression> operands = new ArrayList<Expression>();

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
	public OrExpression(Expression... operands) {
		for (Expression exp : operands) {
			this.operands.add(exp);
		}

	}

	/**
	 * Gets {@link #operands}.
	 *
	 * @return {@link #operands}
	 */
	public List<Expression> getOperands() {
		return operands;
	}

	/**
	 * Sets {@link #operands}.
	 *
	 * @param operands
	 *            New value for {@link #operands}
	 */
	public void setOperands(List<Expression> operands) {
		this.operands = operands;
	}

}
