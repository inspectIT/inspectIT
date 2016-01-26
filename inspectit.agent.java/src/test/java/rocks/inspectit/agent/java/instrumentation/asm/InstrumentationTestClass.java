package rocks.inspectit.agent.java.instrumentation.asm;

/**
 * Class that will be used in the
 * {@link rocks.inspectit.agent.java.instrumentation.asm.ClassInstrumenterTest}. Contains method
 * that will be tested.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class InstrumentationTestClass {

	public static String s;

	static {
		s = "";
	}

	public InstrumentationTestClass() {
	}

	public InstrumentationTestClass(String text) {
	}

	public InstrumentationTestClass(boolean delegate) {
		this("delegate");
	}

	public InstrumentationTestClass(int i) {
		throw new NullPointerException();
	}

	@SuppressWarnings("null")
	public InstrumentationTestClass(Object o) {
		Object c = o != null ? null : o;
		c.toString();
	}

	public InstrumentationTestClass(long l) {
		try {
			throw new NullPointerException();
		} catch (NullPointerException e) {
		}
	}

	// Ivan's instrumentation methods

	public int unexpectedExceptionThrowing() {
		throw new NullPointerException();
	}

	@SuppressWarnings("null")
	public int unexpectedExceptionNotThrowing(Object o) {
		Object c = o != null ? null : o;
		c.toString();
		return 3;
	}

	public int exceptionHandledResultReturned() {
		try {
			throw new NullPointerException();
		} catch (NullPointerException e) {
			return 3;
		}
	}

	// Patrice's test methods below

	public void voidNullParameter() {
	}

	public String stringNullParameter() {
		return "stringNullParameter";
	}

	public int intNullParameter() {
		return 3;
	}

	public double doubleNullParameter() {
		return 5.3D;
	}

	public float floatNullParameter() {
		return Float.MAX_VALUE;
	}

	public byte byteNullParameter() {
		return 127;
	}

	public short shortNullParameter() {
		return 16345;
	}

	public boolean booleanNullParameter() {
		return false;
	}

	public char charNullParameter() {
		return '\u1234';
	}

	public static void voidNullParameterStatic() {
	}

	public static String stringNullParameterStatic() {
		return "stringNullParameterStatic";
	}

	public void voidOneParameter(String parameterOne) {
	}

	public String stringOneParameter(String parameterOne) {
		return "stringOneParameter";
	}

	public void voidTwoParameters(String parameterOne, Object parameterTwo) {
	}

	public void mixedTwoParameters(int parameterOne, boolean parameterTwo) {
	}

	public int[] intArrayNullParameter() {
		return new int[] { 1, 2, 3 };
	}

	public String[] stringArrayNullParameter() {
		return new String[] { "test123", "bla" };
	}

}
