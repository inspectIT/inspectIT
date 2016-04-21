package rocks.inspectit.server.diagnosis.engine.util;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;

/**
 * @author Alexander Wert
 *
 */
public class NoDefaultConstructorClass {
	@TagValue(type = "Test")
	public int par3 = 1;

	@TagValue(type = "Test")
	protected int par4 = 2;
	/**
	 *
	 */
	public NoDefaultConstructorClass(int x) {
		par3 = x;
	}

	@Action(resultTag = "Test2")
	public void actionC() {

	}

	@Action(resultTag = "Test2")
	private void actionD() {

	}
}
