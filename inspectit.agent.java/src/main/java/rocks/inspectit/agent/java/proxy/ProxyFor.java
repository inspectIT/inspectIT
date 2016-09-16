package rocks.inspectit.agent.java.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation has to be placed on subtypes of {@link IProxySubject}, to define the proxy class.
 * This way, the superclass and the implemented interfaces of the runtime generated proxy are
 * defined, the actual proxied methods are defined using the {@link ProxyMethod} annotation.
 * Additionally, this annotation can define the constructor of the superclass of the proxy which
 * will be used.
 *
 * @author Jonas Kunz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProxyFor {

	// NOCHKALL
	/**
	 * @return the full qualified name of the super class of the proxy class. Defaults to
	 *         "java.lang.Object"
	 */
	String superClass() default "java.lang.Object";

	/**
	 * @return the full qualified name of all implemented interfaces of the proxy class.
	 */
	String[] implementedInterfaces() default {};

	/**
	 * @return the signature of the superclass constructor to use. Default is the no-args
	 *         constructor
	 */
	String[] constructorParameterTypes() default {};

}
