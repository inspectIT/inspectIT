package rocks.inspectit.agent.java.proxy;

/**
 * Provides to create proxies at runtime. <br>
 * This class is responsible for caching generated proxy classes.
 *
 * The proxies are described using classes with the {@link ProxyFor} and
 * {@link ProxyMethod} annotation.
 *
 * @author Jonas Kunz
 */
public interface IRuntimeLinker {

	/**
	 * Creates a proxy for the given subject.<br>
	 * The proxys features (e.g. inherited classes, proxied methods) are
	 * described in proxy subjects class.
	 *
	 * @param <T>
	 *            the proxy subject class describing the features of the proxy
	 *            to create
	 * @param proxySubjectType
	 *            the class of {@code <T>}
	 * @param proxySubject
	 *            the instance to which the proxied methods will delegate their
	 *            calls
	 * @param context
	 *            the classloader which has access to the classes the proxy
	 *            inherits from or uses
	 * @return the initialized proxy instance refering to the the given proxy
	 *         subject, or {@code null} if something went wrong
	 */
	<T extends IProxySubject> Object createProxy(Class<T> proxySubjectType,
			T proxySubject, ClassLoader context);

	/**
	 * Checks whether the given object is a proxy instance delegating method calls to an instance of the the given proxy subject type.
	 * @param <T> the type of the proxy subject to check
	 * @param inst the instance to check
	 * @param proxySubjectType the class of {@code <T>}
	 * @return {@code true} if the given instance is such a proxy, {@code false} otherwise
	 */
	<T extends IProxySubject> boolean isProxyInstance(Object inst, Class<T> proxySubjectType);

}
