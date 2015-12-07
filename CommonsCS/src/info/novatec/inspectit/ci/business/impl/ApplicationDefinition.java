package info.novatec.inspectit.ci.business.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.enumeration.BusinessContextErrorCodeEnum;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration element defining an application context.
 *
 * @author Alexander Wert
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "applicaction")
public class ApplicationDefinition implements IMatchingRuleProvider {
	/**
	 * The default identifier.
	 */
	public static final int DEFAULT_ID = 0;

	/**
	 * The name of the default business transaction.
	 */
	public static final String UNKNOWN_BUSINESS_TX = "Unknown Transaction";

	/**
	 * The default business transaction definition that matches any data.
	 */
	private static final BusinessTransactionDefinition DEFAULT_BUSINESS_TRANSACTION_DEFINITION = new BusinessTransactionDefinition(BusinessTransactionDefinition.DEFAULT_ID, UNKNOWN_BUSINESS_TX,
			new BooleanExpression(true, true));

	/**
	 * Identifier of the application. Needs to be unique!
	 */
	@XmlAttribute(name = "id", required = true)
	private int id = (int) UUID.randomUUID().getMostSignificantBits();

	/**
	 * Name of the application.
	 */
	@XmlAttribute(name = "name", required = true)
	private String applicationName;

	/**
	 * Description.
	 */
	@XmlAttribute(name = "description")
	private String description;

	/**
	 * Revision. Server for version control and updating control.
	 */
	@XmlAttribute(name = "revision")
	private Integer revision = 1;

	/**
	 * Rule definition for matching measurement data to applications.
	 *
	 * Default Matching rule should not match any data, therefore {@link BooleanExpression} with
	 * false is used.
	 */
	@XmlElementRef
	private AbstractExpression matchingRuleExpression = new BooleanExpression(false);

	/**
	 * Business transaction definitions.
	 */
	@XmlElementWrapper(name = "business-transactions")
	@XmlElementRef(type = BusinessTransactionDefinition.class)
	private final List<BusinessTransactionDefinition> businessTransactionDefinitions = new LinkedList<BusinessTransactionDefinition>();

	/**
	 * Default constructor.
	 */
	public ApplicationDefinition() {
	}

	/**
	 * Constructor.
	 *
	 * @param applicationName
	 *            name of the application
	 */
	public ApplicationDefinition(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            unique identifier to use for this {@link ApplicationDefinition}
	 * @param applicationName
	 *            name of the application
	 * @param matchingRuleExpression
	 *            matching rule to use for recognition of this application
	 */
	public ApplicationDefinition(int id, String applicationName, AbstractExpression matchingRuleExpression) {
		this(applicationName);
		this.matchingRuleExpression = matchingRuleExpression;
		this.id = id;
	}

	/**
	 * Returns the name of the application.
	 *
	 * @return Returns the name of the application
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * Sets the name of the application.
	 *
	 * @param applicationName
	 *            New value for the name of the application.
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
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
	 * Returns an unmodifiable list of all {@link BusinessTransactionDefinition} instances known in
	 * this application definition.
	 *
	 * @return unmodifiable list of all {@link BusinessTransactionDefinition} instances known in
	 *         this application definition
	 */
	public List<BusinessTransactionDefinition> getBusinessTransactionDefinitions() {
		List<BusinessTransactionDefinition> allbusinessTxDefinitions = new LinkedList<BusinessTransactionDefinition>(businessTransactionDefinitions);
		allbusinessTxDefinitions.add(getDefaultBusinessTransactionDefinition());
		return Collections.unmodifiableList(allbusinessTxDefinitions);
	}

	/**
	 * Retrieves the {@link BusinessTransactionDefinition} with the given identifier.
	 *
	 * @param id
	 *            unique id identifying the business transaction to retrieve
	 * @return Return the {@link BusinessTransactionDefinition} with the given id, or null if no
	 *         {@link BusinessTransactionDefinition} with the passed id could be found.
	 *
	 * @throws BusinessException
	 *             if no {@link BusinessTransactionDefinition} with the given identifier exists.
	 */
	public BusinessTransactionDefinition getBusinessTransactionDefinition(int id) throws BusinessException {
		if (id == BusinessTransactionDefinition.DEFAULT_ID) {
			return getDefaultBusinessTransactionDefinition();
		}
		for (BusinessTransactionDefinition businessTxDef : businessTransactionDefinitions) {
			if (businessTxDef.getId() == id) {
				return businessTxDef;
			}
		}

		throw new BusinessException("Retrieve business transaction with id '" + id + "'.", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
	}

	/**
	 * Returns the default {@link BusinessTransactionDefinition}.
	 *
	 * @return Returns the default {@link BusinessTransactionDefinition}
	 */
	public BusinessTransactionDefinition getDefaultBusinessTransactionDefinition() {
		return DEFAULT_BUSINESS_TRANSACTION_DEFINITION;
	}

	/**
	 * Adds business transaction definition to the application definition.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} instance to add
	 * @throws BusinessException
	 *             If the application definition already contains a business transaction with same
	 *             identifier.
	 */
	public void addBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition) throws BusinessException {
		addBusinessTransactionDefinition(businessTransactionDefinition, businessTransactionDefinitions.size());
	}

	/**
	 * Adds business transaction definition to the application definition. Inserts it to the list
	 * before the element with the passed index.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} instance to add
	 * @param insertBeforeIndex
	 *            insert before this index
	 * @throws BusinessException
	 *             If the application definition already contains a business transaction with same
	 *             identifier or the insertBeforeIndex is not valid.
	 */
	public void addBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition, int insertBeforeIndex) throws BusinessException {
		if (businessTransactionDefinition == null) {
			throw new BusinessException("Adding business transaction 'null'.", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
		} else if (businessTransactionDefinitions.contains(businessTransactionDefinition)) {
			throw new BusinessException(
					"Adding business transaction " + businessTransactionDefinition.getBusinessTransactionDefinitionName() + " with id " + businessTransactionDefinition.getId() + ".",
					BusinessContextErrorCodeEnum.DUPLICATE_ITEM);
		} else if (insertBeforeIndex < 0 || insertBeforeIndex > businessTransactionDefinitions.size()) {
			throw new BusinessException("Adding business transaction " + businessTransactionDefinition.getBusinessTransactionDefinitionName() + " with id " + businessTransactionDefinition.getId()
					+ " at index " + insertBeforeIndex + ".", BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		} else {
			businessTransactionDefinitions.add(insertBeforeIndex, businessTransactionDefinition);

		}

	}

	/**
	 * Deletes the {@link BusinessTransactionDefinition} from the application definition.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} to delete
	 *
	 * @return Returns true if the application definition contained the business transaction
	 */
	public boolean deleteBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition) {
		return businessTransactionDefinitions.remove(businessTransactionDefinition);
	}

	/**
	 * Moves the {@link BusinessTransactionDefinition} to a different position specified by the
	 * index parameter.
	 *
	 * @param businessTransactionDefinition
	 *            {@link BusinessTransactionDefinition} to move
	 * @param index
	 *            position to move the {@link BusinessTransactionDefinition} to
	 * @throws BusinessException
	 *             If the moving the {@link BusinessTransactionDefinition} fails.
	 */
	public void moveBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition, int index) throws BusinessException {
		if (index < 0 || index >= businessTransactionDefinitions.size()) {
			throw new BusinessException("Moving business transaction to index " + index + ".", BusinessContextErrorCodeEnum.INVALID_MOVE_OPRATION);
		}

		int currentIndex = businessTransactionDefinitions.indexOf(businessTransactionDefinition);
		if (currentIndex < 0) {
			throw new BusinessException("Moving business transaction to index " + index + ".", BusinessContextErrorCodeEnum.UNKNOWN_BUSINESS_TRANSACTION);
		}

		if (index != currentIndex) {
			BusinessTransactionDefinition definitionToMove = businessTransactionDefinitions.remove(currentIndex);
			businessTransactionDefinitions.add(index, definitionToMove);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isChangeable() {
		return getId() != DEFAULT_ID;
	}

	/**
	 * Returns the unique identifier of this application definition.
	 *
	 * @return Returns the unique identifier of this application definition.
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
	 * Gets {@link #revision}.
	 *
	 * @return {@link #revision}
	 */
	public Integer getRevision() {
		return revision;
	}

	/**
	 * Sets {@link #revision}.
	 *
	 * @param revision
	 *            New value for {@link #revision}
	 */
	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	/**
	 * Creates identifier for an application using the {@link ApplicationDefinition#id} and
	 * {@link ApplicationDefinition#applicationName}.
	 *
	 * @return id for application instance.
	 */
	public int createApplicationId() {
		if (getId() == 0) {
			return 0;
		} else {
			final int prime = 31;
			int result = 1;
			result = prime * result + getApplicationName().hashCode();
			result = prime * result + getId();
			return result;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		ApplicationDefinition other = (ApplicationDefinition) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}
}
