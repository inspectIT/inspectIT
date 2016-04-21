/**
 *
 */
package rocks.inspectit.server.diagnosis.engine.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;

/**
 * Annotation is used to injected a session variable to a rule. All required session variables needs
 * to be passed to the
 * {@link rocks.inspectit.server.diagnosis.engine.DiagnosisEngine#analyze(Object, SessionVariables)}
 * before a new analysis is kicked off.
 *
 * @author Claudio Waldvogel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SessionVariable {

	// Note on Checkstyle ignores:
	// Due to bug http://sourceforge.net/p/checkstyle/bugs/641/ it is currently not possible to add
	// @return tags to methods within an @interface definition, thus we currently ignore these
	// incorrect findings.

	/**
	 * @return The name of the session variable //NOCHK
	 */
	String name();

	/**
	 * Flag to indicate if this session variable is optional. If a session variable is not available
	 * and is not optional the analysis is stopped and the current analysis session will fail.
	 *
	 * @return flag if session variable is optional. //NOCHK
	 */
	boolean optional() default false;
}
