package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.base.Strings;

import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * Represents the result of a single execution of a {@link RuleDefinition}.
 *
 * @author Claudio Waldvogel, Alexander Wert
 * @see RuleDefinition
 */
public class RuleOutput {

	/**
	 * The name of the rule which produced this RuleOutput. Field is never empty nor null.
	 */
	private final String ruleName;

	/**
	 * The type of the {@link Tags} which were produced by this rule. Field is never empty nor null.
	 */
	private final String embeddedTagType;

	/**
	 * Collection of {@link ConditionFailure}s, if conditions fail.
	 */
	private final Collection<ConditionFailure> conditionFailures;

	/**
	 * Collection of {@link Tag}s created by the latest rule execution.
	 */
	private final Collection<Tag> tags;

	/**
	 * Default constructor.
	 *
	 * @param ruleName
	 *            The name of the executed rule.
	 * @param embeddedTagType
	 *            The type of the produced {@link Tag}s.
	 * @param conditionFailures
	 *            The collected {@link ConditionFailure}s.
	 * @param tags
	 *            The collected {@link Tag}s.
	 */
	public RuleOutput(String ruleName, String embeddedTagType, Collection<ConditionFailure> conditionFailures, Collection<Tag> tags) {
		checkArgument(!Strings.isNullOrEmpty(ruleName), "Rule name must not be empty!");
		checkArgument(!Strings.isNullOrEmpty(embeddedTagType), "Contained tag type name must not be empty!");
		this.ruleName = ruleName;
		this.embeddedTagType = embeddedTagType;
		this.conditionFailures = checkNotNull(conditionFailures, "Collection must not be empty!");
		this.tags = checkNotNull(tags, "Collections must not be empty!");
	}

	// -------------------------------------------------------------
	// Methods: utils
	// -------------------------------------------------------------

	/**
	 * Convenience method to check if the rule execution failed due to failed conditions.
	 *
	 * @return true if conditions failures are available, false otherwise.
	 */
	public boolean hasConditionFailures() {
		return CollectionUtils.isNotEmpty(conditionFailures);
	}

	/**
	 * Convenience method to check if the rule execution produced result tags.
	 *
	 * @return true if {@link Tags} are available, false otherwise.
	 */
	public boolean hasResultTags() {
		return CollectionUtils.isNotEmpty(tags);
	}

	// -------------------------------------------------------------
	// Methods: Accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #ruleName}.
	 *
	 * @return {@link #ruleName}
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 * Gets {@link #embeddedTagType}.
	 *
	 * @return {@link #embeddedTagType}
	 */
	public String getEmbeddedTagType() {
		return embeddedTagType;
	}

	/**
	 * Gets {@link #conditionFailures}.
	 *
	 * @return {@link #conditionFailures}
	 */
	public Collection<ConditionFailure> getConditionFailures() {
		return conditionFailures;
	}

	/**
	 * Gets {@link #tags}.
	 *
	 * @return {@link #tags}
	 */
	public Collection<Tag> getTags() {
		return tags;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.conditionFailures == null) ? 0 : this.conditionFailures.hashCode());
		result = (prime * result) + ((this.embeddedTagType == null) ? 0 : this.embeddedTagType.hashCode());
		result = (prime * result) + ((this.ruleName == null) ? 0 : this.ruleName.hashCode());
		result = (prime * result) + ((this.tags == null) ? 0 : this.tags.hashCode());
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
		RuleOutput other = (RuleOutput) obj;
		if (this.conditionFailures == null) {
			if (other.conditionFailures != null) {
				return false;
			}
		} else if (!this.conditionFailures.equals(other.conditionFailures)) {
			return false;
		}
		if (this.embeddedTagType == null) {
			if (other.embeddedTagType != null) {
				return false;
			}
		} else if (!this.embeddedTagType.equals(other.embeddedTagType)) {
			return false;
		}
		if (this.ruleName == null) {
			if (other.ruleName != null) {
				return false;
			}
		} else if (!this.ruleName.equals(other.ruleName)) {
			return false;
		}
		if (this.tags == null) {
			if (other.tags != null) {
				return false;
			}
		} else if (!this.tags.equals(other.tags)) {
			return false;
		}
		return true;
	}
}
