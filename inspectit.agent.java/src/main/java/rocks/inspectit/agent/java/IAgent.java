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
	 * Returns if the instrumentation transform method for the thread calling this method is
	 * currently disabled.
	 *
	 * @return Returns true if the transform is disabled for current thread, otherwise
	 *         <code>false</code>.
	 */
	boolean isThreadTransformDisabled();

	/**
	 * Sets if the instrumentation transform method for the thread calling this method is currently
	 * disabled.
	 *
	 * @param disabled
	 *            <code>true</code> to disable, <code>false</code> otherwise
	 */
	void setThreadTransformDisabled(boolean disabled);

	/**
	 * Defines if the agent should ignore the class with the given name.
	 *
	 * @param className
	 *            name of the class
	 * @return <code>true</code> if class should be ignored (not byte code changes needed, no
	 *         re-transformation needed, etc), <code>false</code> otherwise
	 */
	boolean shouldClassBeIgnored(String className);

}