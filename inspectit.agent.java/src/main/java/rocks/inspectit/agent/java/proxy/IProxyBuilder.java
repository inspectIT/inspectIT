package rocks.inspectit.agent.java.proxy;

/**
 * Provides the ability to create proxy classes at runtime using bytecode
 * generation.
 *
 * @author Jonas Kunz
 */
public interface IProxyBuilder {

	/**
	 * Creates and loads a new proxy class using the given buildplan. <br>
	 * This method does not check whether the class is already existent, it is
	 * only responsible for creation.
	 *
	 * @param plan
	 *            the plan describing the proxy to create
	 * @return the newly created proxy class together with additional
	 *         information
	 */
	IProxyClassInfo createProxyClass(IProxyBuildPlan plan);
}
