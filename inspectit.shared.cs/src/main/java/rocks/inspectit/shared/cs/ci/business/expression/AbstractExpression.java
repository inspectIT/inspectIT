package rocks.inspectit.shared.cs.ci.business.expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.expression.impl.AndExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.BooleanExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.NotExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.OrExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;

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
	 * Indicates whether the expression is modified in advanced mode.
	 */
	@XmlAttribute(name = "advanced")
	private Boolean advanced = Boolean.FALSE;

	/**
	 * Evaluates the {@link AbstractExpression} against the evaluation context defined by the
	 * {@link InvocationSequenceData} instance.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} instance defining the evaluation context.
	 * @param cachedDataService
	 *            {@link ICachedDataService} instance for retrieving method names, etc.
	 * @return Boolean result of evaluating the {@link AbstractExpression}
	 */
	public abstract boolean evaluate(InvocationSequenceData invocSequence, ICachedDataService cachedDataService);

	/**
	 * Gets {@link #advanced}.
	 *
	 * @return {@link #advanced}
	 */
	public boolean isAdvanced() {
		return advanced.booleanValue();
	}

	/**
	 * Sets {@link #advanced}.
	 *
	 * @param advanced
	 *            New value for {@link #advanced}
	 */
	public void setAdvanced(boolean advanced) {
		this.advanced = Boolean.valueOf(advanced);
	}
}
