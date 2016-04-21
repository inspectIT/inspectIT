package rocks.inspectit.server.diagnosis.engine.util;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;

/**
 * @author Alexander Wert
 *
 */
@Rule
public class AnnotatedSuperClass {
	@TagValue(type = "Test")
	public int par3 = 1;

	/**
	 *
	 */
	public AnnotatedSuperClass(int x) {
		par3 = x;
	}

	@Action(resultTag = "Test2")
	private void actionD() {

	}
}
