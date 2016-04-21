package rocks.inspectit.server.diagnosis.engine.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleA", description = "RuleADescription")
public class RuleA {

	@TagValue(type = Tags.ROOT_TAG)
	String input;

	@Action(resultTag = "A")
	public String action() {
		return input + input;
	}
}
