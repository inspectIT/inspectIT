package rocks.inspectit.agent.java.proxy;

import java.io.IOException;

/**
 * @author Jonas Kunz
 *
 *         Used to test interface proxying and exception throwing.
 *
 */
public interface InterfaceToProxy {

	void throwException() throws IOException;

}
