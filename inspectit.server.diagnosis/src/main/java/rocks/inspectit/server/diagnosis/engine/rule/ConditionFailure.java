package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Objects;

/**
 * Provides information about a failed condition.
 *
 * @author Claudio Waldvogel
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

	@Override
	public String toString() {
		return "ConditionFailure{" + "conditionName='" + conditionName + '\'' + ", hint='" + hint + '\'' + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ConditionFailure that = (ConditionFailure) o;
		return Objects.equal(getConditionName(), that.getConditionName()) && Objects.equal(getHint(), that.getHint());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getConditionName(), getHint());
	}
}
