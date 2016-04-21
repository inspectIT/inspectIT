package rocks.inspectit.server.diagnosis.engine.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleG")
public class RuleG {

	@TagValue(type = "A")
	String input1;

	@Condition
	public boolean condition() {
		return false;
	}

	@Action(resultTag = "G")
	public String action() {
		return input1;
	}
}
