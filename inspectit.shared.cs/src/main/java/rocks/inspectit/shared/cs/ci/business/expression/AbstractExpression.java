package rocks.inspectit.shared.cs.ci.business.expression;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

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
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = AndExpression.class, name = "AND"),
	@JsonSubTypes.Type(value = NotExpression.class, name = "NOT"),
	@JsonSubTypes.Type(value = OrExpression.class, name = "OR"), @JsonSubTypes.Type(value = BooleanExpression.class, name = "Boolean"),
	@JsonSubTypes.Type(value = StringMatchingExpression.class, name = "StringMatching") })
public abstract class AbstractExpression {
	/**
	 * Identifier of the expression.
	 */
	@JsonIgnore
	@XmlAttribute(name = "id", required = true)
	private final long id = UUID.randomUUID().getMostSignificantBits();

	/**
	 * Indicates whether the expression is modified in advanced mode.
	 */
	@JsonIgnore
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

	/**
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public long getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (id ^ (id >>> 32));
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
}
