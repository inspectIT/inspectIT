package info.novatec.inspectit.ci.business;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * AND expression definition. Allows to conjunct multiple expressions.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "and")
public class AndExpression extends Expression {

	/**
	 *
	 */
	private static final long serialVersionUID = 6335793336453882332L;
	/**
	 * List of expressions constituting the operands of the conjunction expression.
	 */
	@XmlElementRef
	private List<Expression> operands = new ArrayList<Expression>();

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
	public AndExpression(Expression... operands) {
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
