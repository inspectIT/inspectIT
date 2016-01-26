package info.novatec.inspectit.agent.instrumentation.asm;

import java.lang.annotation.Annotation;

@SuppressWarnings("all")
final public class TestClass extends AbstractTestClass implements TestInterface, TestAnnotation {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 7191887277972979547L;

	public String method1(int i, long[] j, String s, Object[][][] array3Dim) {
		return null;
	}

	@TestAnnotation
	protected void method0() {
	}

	void methodWithException() throws Exception {
	}

	public Class<? extends Annotation> annotationType() {
		return TestAnnotation.class;
	}

	public String value() {
		return "";
	}
}