package rocks.inspectit.agent.java.analyzer.classes;

/**
 * DO NOT MODIFY UNLESS YOU ARE SURE WHAT YOU ARE DOING!
 * 
 * @author Patrice Bouillet
 * 
 */
@SuppressWarnings("PMD")
public class TestClass extends AbstractSubTest {

	public TestClass() {
	}

	public TestClass(String text) {
	}

	public TestClass(boolean delegate) {
		this("delegate");
	}

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

	// added for class loading delegation testing
	public Class<?> loadClass(String name) {
		return null;
	}

}
