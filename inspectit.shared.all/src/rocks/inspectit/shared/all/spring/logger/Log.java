package info.novatec.inspectit.spring.logger;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker for a field where the Log object has to be automatically injected by the spring.
 * 
 * @author Patrice Bouillet
 * 
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface Log {
}