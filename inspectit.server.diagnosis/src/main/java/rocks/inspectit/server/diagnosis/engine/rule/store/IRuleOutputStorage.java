package rocks.inspectit.server.diagnosis.engine.rule.store;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;

/**
 * Defines a container to store all outputs generated while executing rules.
 *
 * @author Claudio Waldvogel
 */
public interface IRuleOutputStorage {

	/**
	 * Stores a new {@link RuleOutput} to the storage.
	 *
	 * @param output
	 *            The {@link RuleOutput} to be stored
	 */
	void store(RuleOutput output);

	/**
	 * Stores a collection of {@link RuleOutput}s.
	 *
	 * @param outputs
	 *            The {@link RuleOutput}s to be stored
	 */
	void store(Collection<RuleOutput> outputs);

	/**
	 * Method is used to check which different kind of tags are currently available in the storage.
	 *
	 * @return All currently available types of tags
	 */
	Set<String> getAvailableTagTypes();

	/**
	 * Finds the latest inserted {@link RuleOutput}s which embed a certain type of {@link Tag}.
	 *
	 * @param requestedTypes
	 *            The requested tag types.
	 * @return The latest {@link RuleOutput}s which match one of the {@link requestedTypes}.
	 */
	Collection<RuleOutput> findLatestResultsByTagType(Set<String> requestedTypes);

	/**
	 * Delivers a <code>Multimap</code> of all {@link RuleOutput}s which did not complete due to
	 * condition errors. The <code>Multimap</code> is indexed by the the tag type this RuleOutput
	 * should contain, if the execution would not have been skipped.
	 *
	 * @return A Multimap containing all {@link RuleOutput}s with condition failures
	 */
	Multimap<String, RuleOutput> getAllOutputsWithConditionFailures();

	/**
	 * @return All already collected {@link RuleOutput} indexed by the containing tag type
	 */
	Multimap<String, RuleOutput> getAllOutputs();

	/**
	 * Creates a Multimap, indexed by the tag type, of all {@link Tag}s of a certain state.
	 *
	 * @param state
	 *            The state which is used to filter the Tags
	 * @return Multimap of all Tags matching a certain state
	 */
	Multimap<String, Tag> mapTags(TagState state);

	/**
	 * Clear the storage. After the storage is cleared it has to be in the initial state. This means
	 * that it can directly be reused.
	 */
	void clear();
}
