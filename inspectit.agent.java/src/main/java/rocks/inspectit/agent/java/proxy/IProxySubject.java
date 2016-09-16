package rocks.inspectit.agent.java.proxy;


/**
 * Interface implemented by all proxy subjects.
 * <p>
 * A ProxySubject is an object, which recieves calls from a proxy Object.
 * The proxy itself is defined in the ProxySubject class using the {@link ProxyFor}
 * and {@link ProxyMethod} annotations.
 *
 * @author Jonas Kunz
 */
public interface IProxySubject {

	/**
	 * Provides the arguments which are passed to the super-constructor of a proxy.<br>
	 * The types must match the types speified using the {@link ProxyFor} annotation.<br>
	 * If the default constructor is used, please return an empty array.
	 *
	 * @return the arguments to pass to the super cosntructor.
	 */
	Object[] getProxyConstructorArguments();

	/**
	 * Callback method when a proy is created. <br>
	 * This method is called just after a new proxy delegating to this subject has been created. <br>
	 * NOTE: THis method is not necessarily the first method which is called for this proxy!
	 * If the superclass constructor called by the proxy uses any proxied methods, these
	 * calls we be received by the subject before the proxyLinked method has been called.
	 *
	 * @param proxyObject the proxy instance, which was just created
	 * @param linker the linker which was used to create the proxy
	 */
	void proxyLinked(Object proxyObject, IRuntimeLinker linker);

}
