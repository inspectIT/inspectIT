package info.novatec.inspectit.cmr.configuration.business;

import info.novatec.inspectit.cmr.configuration.business.expression.Expression;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.BooleanExpression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration of a data matching rule.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "matching-rule")
public class MatchingRule implements IMatchingRule {

	/**
	 *
	 */
	private static final long serialVersionUID = 3615147959296809472L;

	/**
	 * Boolean expression.
	 */
	@XmlElementRef(type = Expression.class)
	private IExpression expression = new BooleanExpression(false);

	/**
	 * Default Constructor.
	 */
	public MatchingRule() {
	}

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            boolean expression to use for evaluation
	 */
	public MatchingRule(IExpression expression) {
		super();
		this.expression = expression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IExpression getExpression() {
		return expression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setExpression(IExpression expression) {
		this.expression = expression;
	}

}
