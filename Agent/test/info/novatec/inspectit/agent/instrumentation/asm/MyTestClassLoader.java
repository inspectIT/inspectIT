package info.novatec.inspectit.agent.instrumentation.asm;

import java.net.URL;

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
