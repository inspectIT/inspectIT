package rocks.inspectit.agent.java.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods which will be proxied.
 *
 * For every method of a {@link IProxySubject} with this annotation, a delegation method will be placed in the proxy class.
 * Methods without this annotation will not be proxied!
 *
 * @author Jonas Kunz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProxyMethod {
	
	//NOCHKON
	/**
	 * @return the full qualified return type of the proxied method. Defaults to the return type of the annotated method.
	 */
	//NOCHKOFF
	String returnType() default "";
	
	//NOCHKON
	/**
	 * @return the name of the proxied method. Defaults to the name of the annotated method.
	 */
	//NOCHKOFF
	String methodName() default "";
	
	//NOCHKON
	/**
	 * @return a list of the full qualified names of all parameter types of the proxied method.
	 * The types must be subtypes of the annotated Methods types.
	 * If left empty, this defaults to the annotated methods parameter types.
	 */
	//NOCHKOFF
	String[] parameterTypes() default { };

	//NOCHKON
	/**
	 * @return @{code true}, if this method is optional.
	 * 		If a method is optional, the proxy generator will silently ignore missing parameter types (specified by {@link #parameterTypes})
	 * 		and the method will just not be proxied.
	 * 		If a method is not optional (which is the default), the proxy generation will fail in the case of missing depedencies.
	 */
	//NOCHKOFF
	boolean isOptional() default false;
}
