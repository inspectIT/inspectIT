package info.novatec.inspectit.agent;

import info.novatec.inspectit.agent.hooking.IHookDispatcher;

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
	 * Loads class with the given parameters that have been passed to the target class loader.
	 * <p>
	 * Loading will be delegated only if parameters are of size 1 and that single parameter is
	 * String type.
	 * 
	 * @see info.novatec.inspectit.agent.PicoAgent#loadClass(String)
	 * @param params
	 *            Original parameters passed to class loader.
	 * @return Loaded class or <code>null</code>.
	 */
	Class<?> loadClass(Object[] params);

}