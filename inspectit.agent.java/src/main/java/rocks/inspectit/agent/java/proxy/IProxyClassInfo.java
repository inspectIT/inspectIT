package rocks.inspectit.agent.java.proxy;


/**
 * Holds the necessary information about a generated proxy class, alongside with a factory method.
 *
 * @author Jonas Kunz
 */
public interface IProxyClassInfo {

	/**
	 * @return the proxy class.
	 */
	Class<?> getProxyClass();

	/**
	 * Creates and initializes a new proxy. <br>
	 * The proxy is constructed, but {@link IProxySubject#proxyLinked(Object, IRuntimeLinker)} is NOT called.
	 *
	 * @param proxySubject the subject receiving the calls from the proxy to create
	 * @return an initalized proxy delegating method calls on it to the given subject
	 */
	Object createProxy(IProxySubject proxySubject);

}