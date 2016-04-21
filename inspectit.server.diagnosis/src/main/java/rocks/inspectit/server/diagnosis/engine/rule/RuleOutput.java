package rocks.inspectit.server.diagnosis.engine.rule;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.base.Strings;

import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * Represents the result of a single execution of a {@link RuleDefinition}.
 *
 * @author Claudio Waldvogel
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
		return !conditionFailures.isEmpty();
	}

	/**
	 * Convenience method to check if the rule execution produced result tags.
	 *
	 * @return true if {@link Tags} are available, false otherwise.
	 */
	public boolean hasResultTags() {
		return !tags.isEmpty();
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
}
