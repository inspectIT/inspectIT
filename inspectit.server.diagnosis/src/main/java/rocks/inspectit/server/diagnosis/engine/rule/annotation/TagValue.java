package rocks.inspectit.server.diagnosis.engine.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used to inject results of already executed rules to this rule. Since results of
 * rules are stored as {@link rocks.inspectit.server.diagnosis.engine.tag.Tag}s, results can also be
 * injected as a <code>Tag</code>. If the #injectionStrategy property is <code>BY_VALUE</code> a
 * Tag's value is injected. The examples shows how a Tag and/or the value of <code>Tag</code> can be
 * injected to a rule implementation. <p\> <b>Be aware that a single rule can define more than one
 * <code>TagValue</code> injections. But all requested <code>Tags</code> need to be on one rule
 * execution path!</b>
 *
 * <pre>
 * 	{@literal @}TagValue(type = "Tag1", injectionStrategy = BY_TAG)
 * 	private Tag tag1;
 *
 * 	{@literal @}TagValue(type = "Tag1", injectionStrategy = BY_VALUE)
 * 	private Object value;
 * </pre>
 *
 * @author Claudio Waldvogel
 * @see InjectionStrategy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TagValue {

	/**
	 * Defines the injection strategy for tags.
	 */
	enum InjectionStrategy {
		/**
		 * The value of <code>Tag</code> will be injected.
		 */
		BY_VALUE,

		/**
		 * The <code>Tag</code> itself will be injected.
		 */
		BY_TAG
	}

	// Note on Checkstyle ignores:
	// Due to bug http://sourceforge.net/p/checkstyle/bugs/641/ it is currently not possible to add
	// @return tags to methods within an @interface definition, thus we currently ignore these
	// incorrect findings.

	/**
	 * @return The type/name of the Tag to be injected. //NOCHK
	 */
	String type();

	/**
	 * The strategy how the Tag is injected. Either the value or the Tag itself can be injected.
	 *
	 * @return The required InjectionStrategy, the default is BY_VALUE. //NOCHK
	 */
	InjectionStrategy injectionStrategy() default InjectionStrategy.BY_VALUE;
}
