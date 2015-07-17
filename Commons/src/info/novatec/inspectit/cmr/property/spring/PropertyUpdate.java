package info.novatec.inspectit.cmr.property.spring;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for methods that should be fired-up on the property changes. The annotation allow the
 * definition of the properties names that when changed should execute the annotated method.
 * <p>
 * Methods annotated with this annotation should have no arguments.
 * 
 * @author Ivan Senic
 * 
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ METHOD })
public @interface PropertyUpdate {

	/**
	 * List of all property names that when change will fire up annotated method.
	 * 
	 * @return
	 */
	String[] properties() default { };
}
