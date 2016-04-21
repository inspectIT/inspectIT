package rocks.inspectit.server.diagnosis.engine.util;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;

/**
 * @author Alexander Wert
 *
 */
@Rule
public class TestClass extends NoDefaultConstructorClass {
	@TagValue(type = "Test")
	public int par1 = 1;

	@TagValue(type = "Test")
	public int par2 = 2;

	/**
	 * @param x
	 */
	public TestClass() {
		super(2);
	}

	@Action(resultTag = "Test2")
	public void actionA() {

	}

	@Action(resultTag = "Test2")
	private void actionB() {

	}
}
