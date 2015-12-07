package info.novatec.inspectit.rcp.validation;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;

/**
 * Objects of this class comprise required information to provide an identification of an
 * {@link info.novatec.inspectit.rcp.validation.ValidationControlDecoration} instance.
 *
 * @author Alexander Wert
 *
 */
public class ValidatorKey {
	/**
	 * The {@link BusinessTransactionDefinition} the
	 * {@link info.novatec.inspectit.rcp.validation.ValidationControlDecoration} instance belongs
	 * to.
	 */
	private BusinessTransactionDefinition businessTransactionDefinition;

	/**
	 * The {@link AbstractExpression} the
	 * {@link info.novatec.inspectit.rcp.validation.ValidationControlDecoration} instance belongs
	 * to.
	 */
	private AbstractExpression abstractExpression;

	/**
	 * The index of the corresponding control in the parent form part the
	 * {@link info.novatec.inspectit.rcp.validation.ValidationControlDecoration} instance belongs
	 * to. If negative, then this index is not specified.
	 */
	private int controlIndex = -1;

	/**
	 * The name of the control group / form part.
	 */
	private String groupName;

	/**
	 * Gets {@link #businessTransactionDefinition}.
	 *
	 * @return {@link #businessTransactionDefinition}
	 */
	public BusinessTransactionDefinition getBusinessTransactionDefinition() {
		return businessTransactionDefinition;
	}

	/**
	 * Sets {@link #businessTransactionDefinition}.
	 *
	 * @param businessTransactionDefinition
	 *            New value for {@link #businessTransactionDefinition}
	 */
	public void setBusinessTransactionDefinition(BusinessTransactionDefinition businessTransactionDefinition) {
		this.businessTransactionDefinition = businessTransactionDefinition;
	}

	/**
	 * Gets {@link #abstractExpression}.
	 *
	 * @return {@link #abstractExpression}
	 */
	public AbstractExpression getAbstractExpression() {
		return abstractExpression;
	}

	/**
	 * Sets {@link #abstractExpression}.
	 *
	 * @param abstractExpression
	 *            New value for {@link #abstractExpression}
	 */
	public void setAbstractExpression(AbstractExpression abstractExpression) {
		this.abstractExpression = abstractExpression;
	}

	/**
	 * Gets {@link #controlIndex}.
	 *
	 * @return {@link #controlIndex}
	 */
	public int getControlIndex() {
		return controlIndex;
	}

	/**
	 * Sets {@link #controlIndex}.
	 *
	 * @param controlIndex
	 *            New value for {@link #controlIndex}
	 */
	public void setControlIndex(int controlIndex) {
		this.controlIndex = controlIndex;
	}

	/**
	 * Gets {@link #groupName}.
	 *
	 * @return {@link #groupName}
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets {@link #groupName}.
	 *
	 * @param groupName
	 *            New value for {@link #groupName}
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Returns a hash for the control group, the corresponding
	 * {@link info.novatec.inspectit.rcp.validation.ValidationControlDecoration} instance belongs
	 * to.
	 *
	 * @return a group ID.
	 */
	public int getGroupId() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abstractExpression == null) ? 0 : abstractExpression.hashCode());
		result = prime * result + ((businessTransactionDefinition == null) ? 0 : businessTransactionDefinition.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abstractExpression == null) ? 0 : abstractExpression.hashCode());
		result = prime * result + ((businessTransactionDefinition == null) ? 0 : businessTransactionDefinition.hashCode());
		result = prime * result + controlIndex;
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
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
		ValidatorKey other = (ValidatorKey) obj;
		if (abstractExpression == null) {
			if (other.abstractExpression != null) {
				return false;
			}
		} else if (!abstractExpression.equals(other.abstractExpression)) {
			return false;
		}
		if (businessTransactionDefinition == null) {
			if (other.businessTransactionDefinition != null) {
				return false;
			}
		} else if (!businessTransactionDefinition.equals(other.businessTransactionDefinition)) {
			return false;
		}
		if (controlIndex != other.controlIndex) {
			return false;
		}
		if (groupName == null) {
			if (other.groupName != null) {
				return false;
			}
		} else if (!groupName.equals(other.groupName)) {
			return false;
		}
		return true;
	}

}
