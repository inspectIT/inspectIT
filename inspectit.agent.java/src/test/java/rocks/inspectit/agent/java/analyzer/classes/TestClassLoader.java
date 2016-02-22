package rocks.inspectit.agent.java.analyzer.classes;

/**
 * Test {@link ClassLoader} that always returns it's own class in {@link #loadClass(String)}.
 * 
 * @author Ivan Senic
 * 
 */
public class TestClassLoader extends ClassLoader {

	public java.lang.Class<?> loadClass(String name) throws ClassNotFoundException {
		return this.getClass();
	};
}
