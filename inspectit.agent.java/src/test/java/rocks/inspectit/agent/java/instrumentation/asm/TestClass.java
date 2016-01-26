package rocks.inspectit.agent.java.instrumentation.asm;

import java.lang.annotation.Annotation;

/**
 * Class to be analyzed with the asm during tests.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("all")
final public class TestClass extends AbstractTestClass implements TestInterface, TestAnnotation {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 7191887277972979547L;

	public String method1(int i, long[] j, String s, Object[][][] array3Dim) {
		return null;
	}

	@Override
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