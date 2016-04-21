package rocks.inspectit.server.diagnosis.engine.rule.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;

/**
 * The default implementation of {@link IRuleOutputStorage}.
 *
 * @author Claudio Waldvogel
 */
public class DefaultRuleOutputStorage implements IRuleOutputStorage {

	/**
	 * The backing Multimap to store all {@link RuleOutput}s.
	 */
	private final Multimap<String, RuleOutput> allOutputs = LinkedHashMultimap.create();

	/**
	 * The backing Multimap to store all {@link RuleOutput}s where one or more condition failed.
	 */
	private final Multimap<String, RuleOutput> conditionFailures = ArrayListMultimap.create();

	// -------------------------------------------------------------
	// Interface Implementation: IRuleOutputStorage
	// -------------------------------------------------------------

	@Override
	public void store(Collection<RuleOutput> output) {
		for (RuleOutput single : output) {
			store(single);
		}
	}

	@Override
	public void store(RuleOutput output) {
		if (output.hasConditionFailures()) {
			conditionFailures.put(output.getEmbeddedTagType(), output);
		} else {
			allOutputs.put(output.getEmbeddedTagType(), output);
		}
	}

	@Override
	public Set<String> getAvailableTagTypes() {
		return allOutputs.keySet();
	}

	@Override
	public Multimap<String, RuleOutput> getAllOutputsWithConditionFailures() {
		return conditionFailures;
	}

	@Override
	public Multimap<String, RuleOutput> getAllOutputs() {
		return allOutputs;
	}

	@Override
	public Multimap<String, Tag> mapTags(TagState state) {
		Multimap<String, Tag> tags = ArrayListMultimap.create();
		for (Map.Entry<String, RuleOutput> entry : allOutputs.entries()) {
			if (entry.getValue().hasResultTags()) {
				for (Tag tag : entry.getValue().getTags()) {
					if (tag.getState().equals(state)) {
						tags.put(tag.getType(), tag);
					}
				}
			}
		}
		return tags;
	}

	@Override
	public Collection<RuleOutput> findLatestResultsByTagType(Set<String> tagTypes) {
		Set<String> keys = allOutputs.keySet();
		ListIterator<String> iterator = new ArrayList<>(keys).listIterator(keys.size());
		while (iterator.hasPrevious()) {
			String previous = iterator.previous();
			if (tagTypes.contains(previous)) {
				return allOutputs.get(previous);
			}
		}
		return null;
	}

	@Override
	public void clear() {
		this.allOutputs.clear();
	}
}
