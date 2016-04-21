package rocks.inspectit.server.diagnosis.engine.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleF")
public class RuleF {

	@TagValue(type = "A")
	String input1;

	@TagValue(type = "E")
	String input2;

	@Condition
	public boolean condition() {
		return true;
	}

	@Action(resultTag = "F")
	public String action() {
		return input1 + input2;
	}
}
