package rocks.inspectit.shared.cs.ci.business.impl;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.BooleanExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.NameExtractionExpression;

/**
 * Configuration element defining a business transaction context.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "business-transaction")
public class BusinessTransactionDefinition implements IMatchingRuleProvider {
	/**
	 * The name of the default business transaction.
	 */
	public static final String UNKNOWN_BUSINESS_TX = "Unknown Transaction";

	/**
	 * The default identifier.
	 */
	public static final int DEFAULT_ID = 0;

	/**
	 * Default {@link BusinessTransactionDefinition} instance.
	 */
	public static final BusinessTransactionDefinition DEFAULT_BUSINESS_TRANSACTION_DEFINITION = new BusinessTransactionDefinition(BusinessTransactionDefinition.DEFAULT_ID, UNKNOWN_BUSINESS_TX,
			new BooleanExpression(true, true));

	/**
	 * Identifier of the business transaction. Needs to be unique!
	 */
	@XmlAttribute(name = "id", required = true)
	private int id = (int) UUID.randomUUID().getMostSignificantBits();

	/**
	 * Name of the business transaction. Needs to be unique!
	 */
	@XmlAttribute(name = "name", required = true)
	private String businessTransactionDefinitionName;

	/**
	 * Source of the string value to be compared against the snippet.
	 */
	@XmlElementRef(name = "nameExtractionExpression", required = false)
	private NameExtractionExpression nameExtractionExpression;

	/**
	 * Description.
	 */
	@XmlAttribute(name = "description")
	private String description;

	/**
	 * Rule definition for matching measurement data to business transactions.
	 */
	@XmlElementRef
	private AbstractExpression matchingRuleExpression = new BooleanExpression(false);

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
		this.businessTransactionDefinitionName = businessTransactionName;
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            unique identifier to use for the {@link BusinessTransactionDefinition}
	 * @param businessTransactionName
	 *            name of the business transaction
	 * @param matchingRuleExpression
	 *            matching rule to use for recognition of this business transaction
	 */
	public BusinessTransactionDefinition(int id, String businessTransactionName, AbstractExpression matchingRuleExpression) {
		this(businessTransactionName);
		this.matchingRuleExpression = matchingRuleExpression;
		this.id = id;
	}

	/**
	 * Indicates whether the name of the business transaction shell be extracted dynamically from
	 * the measurement data.
	 *
	 * @return true, if dynamic extraction shell be used.
	 */
	public boolean dynamicNameExtractionActive() {
		return null != getNameExtractionExpression();
	}

	/**
	 * Determines the business transaction name for the given {@link InvocationSequenceData} using
	 * the passed {@link BusinessTransactionDefinition}.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} to determine the business transaction name for.
	 * @param cachedDataService
	 *            {@link ICachedDataService} instance for retrieving method names, etc.
	 * @return a business transaction name
	 */
	public String determineBusinessTransactionName(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		String businessTxName;
		if (dynamicNameExtractionActive()) {
			businessTxName = extractNameDynamically(invocSequence, cachedDataService, 0);
			if (null == businessTxName) {
				businessTxName = getBusinessTransactionDefinitionName() + NameExtractionExpression.UNKNOWN_DYNAMIC_BUSINESS_TRANSACTION_POSTFIX;
			}
		} else {
			businessTxName = getBusinessTransactionDefinitionName();
		}
		return businessTxName;
	}

	/**
	 * Extracts the business transaction name dynamically from the {@link InvocationSequenceData} by
	 * recursively iterating over the invocation sequence.
	 *
	 * @param invocSequence
	 *            {@link InvocationSequenceData} to extract the business transaction from.
	 * @param cachedDataService
	 *            {@link ICachedDataService} instance for retrieving method names, etc.
	 * @param depth
	 *            current recursion depth. THis is used stop the recursion at a specified maximum
	 *            search depth.
	 * @return extracted name
	 */
	private String extractNameDynamically(InvocationSequenceData invocSequence, ICachedDataService cachedDataService, int depth) {
		String name = getNameExtractionExpression().extractName(invocSequence, cachedDataService);
		if ((null == name) && (null != invocSequence.getNestedSequences())) {
			if ((getNameExtractionExpression().getMaxSearchDepth() < 0) || (depth < getNameExtractionExpression().getMaxSearchDepth())) {
				for (InvocationSequenceData child : invocSequence.getNestedSequences()) {
					name = extractNameDynamically(child, cachedDataService, depth + 1);
					if (null != name) {
						return name;
					}
				}
			}
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isChangeable() {
		return getId() != DEFAULT_ID;
	}

	/**
	 * Returns the name of the business transaction.
	 *
	 * @return the name of the business transaction
	 */
	public String getBusinessTransactionDefinitionName() {
		return businessTransactionDefinitionName;
	}

	/**
	 * Sets the name of the business transaction.
	 *
	 * @param businessTransactionName
	 *            New value for the name of the business transaction
	 */
	public void setBusinessTransactionDefinitionName(String businessTransactionName) {
		this.businessTransactionDefinitionName = businessTransactionName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractExpression getMatchingRuleExpression() {
		return matchingRuleExpression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMatchingRuleExpression(AbstractExpression matchingRuleExpression) {
		this.matchingRuleExpression = matchingRuleExpression;
	}

	/**
	 * Returns the identifier of this business transaction.
	 *
	 * @return Returns the identifier of this business transaction
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the description text.
	 *
	 * @return Returns the description text.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description text.
	 *
	 * @param description
	 *            New value for the description text.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets {@link #nameExtractionExpression}.
	 *
	 * @return {@link #nameExtractionExpression}
	 */
	public NameExtractionExpression getNameExtractionExpression() {
		return nameExtractionExpression;
	}

	/**
	 * Sets {@link #nameExtractionExpression}.
	 *
	 * @param nameExtractionExpression
	 *            New value for {@link #nameExtractionExpression}
	 */
	public void setNameExtractionExpression(NameExtractionExpression nameExtractionExpression) {
		this.nameExtractionExpression = nameExtractionExpression;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + id;
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
