package rocks.inspectit.server.diagnosis.engine.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleE")
public class RuleE {

	@TagValue(type = Tags.ROOT_TAG)
	String input;

	@Action(resultTag = "E")
	public String action() {
		return input + "E";
	}
}
