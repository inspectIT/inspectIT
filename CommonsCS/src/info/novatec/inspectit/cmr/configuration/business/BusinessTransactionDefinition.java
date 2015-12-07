package info.novatec.inspectit.cmr.configuration.business;

import info.novatec.inspectit.cmr.configuration.business.expression.impl.BooleanExpression;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration element defining a business transaction context.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "business-transaction")
public class BusinessTransactionDefinition implements IBusinessTransactionDefinition {

	/**
	 *
	 */
	private static final long serialVersionUID = -8564597724252120550L;

	/**
	 * Identifier of the business transaction. Needs to be unique!
	 */
	@XmlAttribute(name = "id", required = true)
	private long id = UUID.randomUUID().getMostSignificantBits();

	/**
	 * Name of the business transaction. Needs to be unique!
	 */
	@XmlAttribute(name = "name", required = true)
	private String businessTransactionName;

	/**
	 * Description.
	 */
	@XmlAttribute(name = "description", required = false)
	private String description;

	/**
	 * Rule definition for matching measurement data to business transactions.
	 */
	@XmlElementRef(type = MatchingRule.class)
	private IMatchingRule matchingRule = new MatchingRule(new BooleanExpression(false));

	/**
	 * Default Constructor.
	 */
	public BusinessTransactionDefinition() {
	}

	/**
	 * Constructor.
	 *
	 * @param businessTransactionName
	 *            name of the business transaction
	 */
	public BusinessTransactionDefinition(String businessTransactionName) {
		this.businessTransactionName = businessTransactionName;
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            unique identifier to use for the {@link BusinessTransactionDefinition}
	 * @param businessTransactionName
	 *            name of the business transaction
	 * @param matchingRule
	 *            matching rule to use for recognition of this business transaction
	 */
	protected BusinessTransactionDefinition(long id, String businessTransactionName, MatchingRule matchingRule) {
		this(businessTransactionName);
		this.matchingRule = matchingRule;
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBusinessTransactionName() {
		return businessTransactionName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBusinessTransactionName(String businessTransactionName) {
		this.businessTransactionName = businessTransactionName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IMatchingRule getMatchingRule() {
		return matchingRule;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMatchingRule(IMatchingRule matchingRule) {
		this.matchingRule = matchingRule;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
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
		BusinessTransactionDefinition other = (BusinessTransactionDefinition) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
}
