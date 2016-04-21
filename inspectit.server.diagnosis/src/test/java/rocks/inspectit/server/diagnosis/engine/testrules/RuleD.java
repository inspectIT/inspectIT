package rocks.inspectit.server.diagnosis.engine.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleD")
public class RuleD {

	@TagValue(type = Tags.ROOT_TAG)
	String input;

	@Action(resultTag = "D")
	public int action() {
		throw new RuntimeException("Expected Rule exception");
	}
}
