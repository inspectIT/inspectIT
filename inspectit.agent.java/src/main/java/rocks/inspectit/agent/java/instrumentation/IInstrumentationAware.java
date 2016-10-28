package rocks.inspectit.agent.java.instrumentation;

import java.lang.instrument.Instrumentation;

/**
 * Classes implementing this interface are considered as instrumentation aware and will get injected
 * an {@link Instrumentation} instance.
 *
 * @author Marius Oehler
 *
 */
public interface IInstrumentationAware {

	/**
	 * Sets the {@link #instrumentation}.
	 *
	 * @param instrumentation
	 *            {@link Instrumentation} to use
	 */
	void setInstrumentation(Instrumentation instrumentation);
}
