package rocks.inspectit.agent.java.util;

import org.springframework.stereotype.Component;

/**
 * Class which was used as a wrapper around a timer factory. As the move to Java 5 was done, the
 * factory is currently not needed anymore, but this class stays if some new timer implementations
 * will be needed in the future (higher precision, performance, ...).
 *
 * @author Patrice Bouillet
 *
 */
@Component
public class Timer {

	/**
	 * Returns the current time in milliseconds.
	 *
	 * @return The time as a double value.
	 */
	public double getCurrentTime() {
		return System.nanoTime() / 1000000.0d;
	}

}
