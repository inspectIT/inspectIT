package info.novatec.inspectit.agent.instrumentation.asm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Test annotation to be used during
 * {@link info.novatec.inspectit.agent.instrumentation.asm.ClassAnalyzerTest}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotation {

	String value() default "";
}