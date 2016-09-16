package rocks.inspectit.agent.java.proxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A meta level description of a proxy class.
 *
 * @author Jonas Kunz
 */
public interface IProxyBuildPlan {

	/**
	 * @return The full qualified name of the proxy class to create.
	 */
	String getProxyClassName();

	/**
	 * @return the super class the proxy will inherit from
	 */
	Class<?> getSuperClass();

	/**
	 * @return a list of all interfaces implmented by the proxy class.
	 */
	List<Class<?>> getImplementedInterfaces();

	/**
	 * @return The parameter types of the constructor for the super class ({@link #getSuperClass()})
	 *         that will be used.
	 */
	List<Class<?>> getConstructorParameterTypes();

	/**
	 * @return a list of all checked exceptions the constructor of the proxies super class throws
	 */
	List<Class<?>> getConstructorExceptions();

	/**
	 * @return A List of all proxied methods.
	 */
	List<? extends IMethodBuildPlan> getMethods();

	/**
	 * @return the classloader in which the described proxy class should be placed
	 */
	ClassLoader getTargetClassLoader();

	/**
	 *
	 * Holds the information about a proxied method.
	 *
	 * @author Jonas Kunz
	 */
	interface IMethodBuildPlan {

		/**
		 * @return The name of the method which will be proxied.
		 */
		String getMethodName();

		/**
		 * @return The return type of the method which will be proxied.
		 */
		Class<?> getReturnType();

		/**
		 * @return The parameter types of the proxied method.
		 */
		List<Class<?>> getParameterTypes();

		/**
		 * @return The method to which the proxied method will delegate its calls.
		 */
		Method getTargetMethod();

	}
}
