package rocks.inspectit.agent.java.instrumentation.asm;

import java.net.URL;

/**
 * Dummy class loader to test the class loading delegation instrumentation in the
 * {@link rocks.inspectit.agent.java.instrumentation.asm.ClassInstrumenterTest}.
 * 
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class MyTestClassLoader extends ClassLoader {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return super.loadClass(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getResource(String name) {
		return super.getResource(name);
	}

}
