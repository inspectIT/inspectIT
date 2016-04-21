package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provides information about a failed condition.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
public class ConditionFailure {

	/**
	 * The name of the failed condition.
	 */
	private final String conditionName;

	/**
	 * A hint why the condition failed. If possible, a solution can be provided.
	 */
	private final String hint;

	/**
	 * Default Constructor.
	 *
	 * @param conditionName
	 *            The name of the failed condition
	 * @param hint
	 *            A hint why the condition failed
	 */
	public ConditionFailure(String conditionName, String hint) {
		checkArgument(!isNullOrEmpty(conditionName), "The condition name must not be null!");
		this.conditionName = conditionName;
		this.hint = hint;
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #conditionName}.
	 *
	 * @return {@link #conditionName}
	 */
	public String getConditionName() {
		return conditionName;
	}

	/**
	 * Gets {@link #hint}.
	 *
	 * @return {@link #hint}
	 */
	public String getHint() {
		return hint;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ConditionFailure{" + "conditionName='" + conditionName + '\'' + ", hint='" + hint + '\'' + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.conditionName == null) ? 0 : this.conditionName.hashCode());
		result = (prime * result) + ((this.hint == null) ? 0 : this.hint.hashCode());
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
		ConditionFailure other = (ConditionFailure) obj;
		if (this.conditionName == null) {
			if (other.conditionName != null) {
				return false;
			}
		} else if (!this.conditionName.equals(other.conditionName)) {
			return false;
		}
		if (this.hint == null) {
			if (other.hint != null) {
				return false;
			}
		} else if (!this.hint.equals(other.hint)) {
			return false;
		}
		return true;
	}
}
