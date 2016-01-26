package rocks.inspectit.agent.java.instrumentation.asm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Test annotation to be used during
 * {@link rocks.inspectit.agent.java.instrumentation.asm.ClassAnalyzerTest}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAnnotation {

	String value() default "";
}