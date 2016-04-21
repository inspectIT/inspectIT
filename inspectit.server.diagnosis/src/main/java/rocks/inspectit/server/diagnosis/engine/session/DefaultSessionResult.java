package rocks.inspectit.server.diagnosis.engine.session;

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
 * <pre>
 *     The DefaultSessionResult provides:
 *     <ul>
 *         <li>The original input {@link #input}</li>
 *         <li>Map of all Tags ({@link #endTags}) of type {@link TagState#LEAF}</li>
 *          <li>Map of {@link ConditionFailure}s ({@link #conditionFailures})</li>
 *     </ul>
 * </pre>
 *
 * @param <I>
 *            The type of input which was passed to the session to be analyzed.
 * @author Claudio Waldvogel
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
		this.input = input;
		this.conditionFailures = conditionFailures;
		this.endTags = endTags;
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

	@Override
	public String toString() {
		return "DefaultSessionResult{" + "input=" + input + ", conditionFailures=" + conditionFailures + ", endTags=" + endTags + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DefaultSessionResult<?> that = (DefaultSessionResult<?>) o;

		if (getInput() != null ? !getInput().equals(that.getInput()) : that.getInput() != null) {
			return false;
		}
		if (getConditionFailures() != null ? !getConditionFailures().equals(that.getConditionFailures()) : that.getConditionFailures() != null) {
			return false;
		}
		return getEndTags() != null ? getEndTags().equals(that.getEndTags()) : that.getEndTags() == null;

	}

	@Override
	public int hashCode() {
		int result = getInput() != null ? getInput().hashCode() : 0;
		result = 31 * result + (getConditionFailures() != null ? getConditionFailures().hashCode() : 0);
		result = 31 * result + (getEndTags() != null ? getEndTags().hashCode() : 0);
		return result;
	}
}
