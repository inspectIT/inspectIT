package rocks.inspectit.server.diagnosis.service.rules.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleB")
public class RuleB {

	@TagValue(type = "A")
	String input;

	@Action(resultTag = "B")
	public int action() {
		return input.length();
	}
}
