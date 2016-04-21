package rocks.inspectit.server.diagnosis.engine.session;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngine;
import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineConfiguration;
import rocks.inspectit.server.diagnosis.engine.rule.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;

/**
 * Default implementation to represent result of a {@link Session}. The {@link DiagnosisEngine} can
 * be configured to produce all kinds of results. If the engine should provide other results an
 * other {@link ISessionResultCollector} should be provided to the engine.
 *
 * <br>
 * <br>
 * The DefaultSessionResult provides:
 * <ul>
 * <li>The original input {@link #input}</li>
 * <li>Map of all Tags ({@link #endTags}) of type {@link TagState#LEAF}</li>
 * <li>Map of {@link ConditionFailure}s ({@link #conditionFailures})</li>
 * </ul>
 *
 * @param <I>
 *            The type of input which was passed to the session to be analyzed.
 * @author Claudio Waldvogel, Alexander Wert
 * @see ConditionFailure
 * @see DiagnosisEngineConfiguration
 * @see ISessionResultCollector
 * @see DefaultSessionResultCollector
 */
public class DefaultSessionResult<I> {

	/**
	 * The original input value which was passed to {@link DiagnosisEngine#analyze(Object)}.
	 */
	private final I input;

	/**
	 * Map of all {@link ConditionFailure} which were discovered while running a diagnosis
	 * {@link Session}. The index of the map is the name of the rule which produced the
	 * {@link ConditionFailure}.
	 *
	 * @see ConditionFailure
	 */
	private final Multimap<String, ConditionFailure> conditionFailures;

	/**
	 * Map of all {@link Tag}s which were produced but not consumed. These {@link Tag} are
	 * considered to be the relevant ones. Those have state {@link TagState#LEAF}
	 */
	private final Multimap<String, Tag> endTags;

	/**
	 * Default Constructor.
	 *
	 * @param input
	 *            The original input
	 * @param conditionFailures
	 *            Map of {@link ConditionFailure}s
	 * @param endTags
	 *            Map of {@link Tag}s
	 */
	public DefaultSessionResult(I input, Multimap<String, ConditionFailure> conditionFailures, Multimap<String, Tag> endTags) {
		this.input = checkNotNull(input, "Input must not be null!");
		this.conditionFailures = checkNotNull(conditionFailures, "Map of condition failures must not be null!");
		this.endTags = checkNotNull(endTags, "Map of end tags must not be null!");
	}

	// -------------------------------------------------------------
	// Methods: accessors
	// -------------------------------------------------------------

	/**
	 * Gets {@link #input}.
	 *
	 * @return {@link #input}
	 */
	public I getInput() {
		return input;
	}

	/**
	 * Gets {@link #conditionFailures}.
	 *
	 * @return {@link #conditionFailures}
	 */
	public Multimap<String, ConditionFailure> getConditionFailures() {
		return conditionFailures;
	}

	/**
	 * Gets {@link #endTags}.
	 *
	 * @return {@link #endTags}
	 */
	public Multimap<String, Tag> getEndTags() {
		return endTags;
	}

	// -------------------------------------------------------------
	// Methods: Generated
	// -------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DefaultSessionResult{" + "input=" + input + ", conditionFailures=" + conditionFailures + ", endTags=" + endTags + '}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.conditionFailures == null) ? 0 : this.conditionFailures.hashCode());
		result = (prime * result) + ((this.endTags == null) ? 0 : this.endTags.hashCode());
		result = (prime * result) + ((this.input == null) ? 0 : this.input.hashCode());
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
		DefaultSessionResult<?> other = (DefaultSessionResult<?>) obj;
		if (this.conditionFailures == null) {
			if (other.conditionFailures != null) {
				return false;
			}
		} else if (!this.conditionFailures.equals(other.conditionFailures)) {
			return false;
		}
		if (this.endTags == null) {
			if (other.endTags != null) {
				return false;
			}
		} else if (!this.endTags.equals(other.endTags)) {
			return false;
		}
		if (this.input == null) {
			if (other.input != null) {
				return false;
			}
		} else if (!this.input.equals(other.input)) {
			return false;
		}
		return true;
	}
}
