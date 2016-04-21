package rocks.inspectit.server.diagnosis.engine.util;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;

/**
 * @author Alexander Wert
 *
 */
public class TestClass2 extends AnnotatedSuperClass {

	/**
	 * @param x
	 */
	public TestClass2(int x) {
		super(x);
	}

	@Action(resultTag = "Test2")
	private void actionB() {

	}
}
