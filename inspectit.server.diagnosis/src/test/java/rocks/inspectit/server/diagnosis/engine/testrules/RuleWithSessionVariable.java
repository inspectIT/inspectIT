package rocks.inspectit.server.diagnosis.engine.testrules;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * @author Alexander Wert
 *
 */
@Rule(name = "RuleWithSessionVariable")
public class RuleWithSessionVariable {

	@TagValue(injectionStrategy = TagValue.InjectionStrategy.BY_VALUE, type = Tags.ROOT_TAG)
	String input;

	@SessionVariable(name = "sessionVar")
	String sessionVar;

	@Action(resultTag = "A")
	public String action() {
		return input + sessionVar;
	}
}
