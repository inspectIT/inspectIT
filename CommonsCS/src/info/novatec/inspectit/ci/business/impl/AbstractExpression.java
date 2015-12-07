package info.novatec.inspectit.ci.business.impl;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Abstract class for a boolean expression definition.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ AndExpression.class, OrExpression.class, NotExpression.class, StringMatchingExpression.class, BooleanExpression.class })
public abstract class AbstractExpression {
	/**
	 * Identifier of the expression.
	 */
	@XmlAttribute(name = "id", required = true)
	private final long id = UUID.randomUUID().getMostSignificantBits();

	/**
	 * Indicates whether the expression is modified in advanced mode.
	 */
	@XmlAttribute(name = "advanced", required = false)
	private boolean advanced = false;

	/**
	 * Returns the identifier of the expression.
	 *
	 * @return the identifier of the expression.
	 */

	public long getId() {
		return id;
	}

	/**
	 * Returns the number of child expressions supported by this expressions.
	 *
	 * @return the number of child expressions supported by this expressions.
	 */
	public abstract int getSupportedNumberOfChildExpressions();

	/**
	 * Returns the number of child expression currently attached to this expression.
	 *
	 * @return The number of child expression currently attached to this expression.
	 */
	public abstract int getNumberOfChildExpressions();

	/**
	 * Returns the {@link ExpressionType} of this expression.
	 *
	 * @return the {@link ExpressionType} of this expression.
	 */
	public abstract ExpressionType getExpressionType();

	public boolean isAdvanced() {
		return advanced;
	}

	public void setAdvanced(boolean advanced) {
		this.advanced = advanced;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractExpression other = (AbstractExpression) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	/**
	 * Enumeration of available expression types.
	 *
	 * @author Alexander Wert
	 *
	 */
	public enum ExpressionType {
		/**
		 * Conjunction.
		 */
		AND,

		/**
		 * Disjunction.
		 */
		OR,

		/**
		 * Negation.
		 */
		NOT,

		/**
		 * Boolean expression.
		 */
		BOOLEAN,

		/**
		 * String matching.
		 */
		STRING_MATCHING;
	}

}
