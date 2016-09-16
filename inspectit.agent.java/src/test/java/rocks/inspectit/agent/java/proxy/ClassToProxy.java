package rocks.inspectit.agent.java.proxy;

/**
 * @author Jonas Kunz
 *
 */
public abstract class ClassToProxy {

	public ClassToProxy(String pleaseSayHello) {
		if (!"Hello".equalsIgnoreCase(pleaseSayHello)) {
			throw new IllegalArgumentException();
		}
	}

	public abstract int doubleInt(int a);

	protected String sayHello() {
		return "Bye!";
	}

	public abstract StringBuffer createStringBuffer();

	public abstract void appendToStringBuffer(StringBuffer sb, String stringToAppend);


}
