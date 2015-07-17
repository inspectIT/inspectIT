package info.novatec.inspectit.cmr.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for methods that will be logged and/or profiled, By placing this annotation on a method
 * spring will proxy the service and call the interceptor that provides advice to the real method
 * call.
 * 
 * @author Patrice Bouillet
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MethodLog {

	/**
	 * The log level which can be used. The level from logback cannot be used directly as it is not
	 * allowed as a return type.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum Level {
		OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL; // NOCHK
	}

	/**
	 * The defined level on which the time messages shall be printed to.
	 */
	Level timeLogLevel() default Level.DEBUG;

	/**
	 * The defined level on which the trace messages shall be printed to.
	 */
	Level traceLogLevel() default Level.TRACE;

	/**
	 * Defines a duration limit on this method. If the methods duration exceed the specified one, a
	 * message will be printed into the log.
	 */
	long durationLimit() default -1;

}
