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
	 * Returns whether retransformation is specified by the current environment.
	 *
	 * @return <code>true</code> if retransformation should be used
	 */
	boolean isUsingRetransformation();
}