package rocks.inspectit.agent.java.sensor.method.invocationsequence;

import rocks.inspectit.agent.java.core.ICoreService;

/**
 * Exception used in the {@link InvocationSequenceHook} to mark the methods from the
 * {@link ICoreService} which should never be called if the invocation sequence hook mimics the real
 * core service implementation.
 *
 * @author Patrice Bouillet
 *
 */
public class UnsupportedMethodException extends RuntimeException {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 6187250477502855273L;

}
