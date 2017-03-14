package rocks.inspectit.agent.java;

import rocks.inspectit.agent.java.hooking.IHookDispatcher;

/**
 * Interface for accessing the real agent implementation from the SUD and our JavaAgent.
 *
 * @author Patrice Bouillet
 *
 */
public interface IAgent {

	/**
	 * This method will inspect the given byte code and class name to check if it needs to be
	 * instrumented by the Agent. The class loader is needed as different versions of the same class
	 * can be loaded.
	 *
	 * @param byteCode
	 *            The byte code.
	 * @param className
	 *            The name of the class
	 * @param classLoader
	 *            The class loader of the passed class.
	 * @return Returns the instrumented byte code if something has been changed, otherwise
	 *         <code>null</code>.
	 */
	byte[] inspectByteCode(byte[] byteCode, String className, ClassLoader classLoader);

	/**
	 * Returns the hook dispatcher. This is needed for the instrumented methods in the target
	 * application! Otherwise the entry point for them would be missing.
	 *
	 * @return The hook dispatcher
	 */
	IHookDispatcher getHookDispatcher();

	/**
	 * Defines if the agent should ignore the class with the given name.
	 *
	 * @param className
	 *            name of the class
	 * @return <code>true</code> if class should be ignored (not byte code changes needed, no
	 *         re-transformation needed, etc), <code>false</code> otherwise
	 */
	boolean shouldClassBeIgnored(String className);

	/**
	 * Defines if the class with the given name should be re-transformed / re-defined on the agent
	 * startup. This condition is <code>false</code> when the class with the given FQN is already
	 * analyzed by us and there is no instrumentation definition for the class.
	 *
	 * @param className
	 *            Fully qualified name of the class.
	 * @return <code>false</code> if this class can be skipped during analyzing already loaded
	 *         classes on the agent startup
	 */
	boolean shouldAnalyzeOnStartup(String className);

	/**
	 * Returns whether retransformation is specified by the current environment.
	 *
	 * @return <code>true</code> if retransformation should be used.
	 */
	boolean isUsingRetransformation();

	/**
	 * Returns whether the instrumentation has been disabled or not.
	 *
	 * @return <code>true</code> if the instrumentation is disabled
	 */
	boolean isInstrumentationDisabled();
}