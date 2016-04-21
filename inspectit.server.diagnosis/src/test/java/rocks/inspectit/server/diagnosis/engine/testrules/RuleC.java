package rocks.inspectit.server.diagnosis.engine.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleC")
public class RuleC {

	@TagValue(type = "A")
	String input;

	@TagValue(type = "B")
	int input2;

	@Action(resultTag = "C")
	public int action() {
		return input2 + 2;
	}
}
