package rocks.inspectit.server.diagnosis.engine.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation to define a class as Rule implementation. The <code>Rule</code> annotation is
 * only valid for <code>Types</code>. The following examples illustrates the common skeleton of a
 * rule implementation. The top level <code>Rule</code> annotation is used to define the name, a
 * description of the rule, and a fireCondition. The <code>fireCondition</code> property defines the
 * * condition at what time the rule will be executed. This property is translated to a
 * {@link rocks.inspectit.server.diagnosis.engine.rule.FireCondition} instance. As shown a rule is
 * completed by {@link TagValue}, {@link SessionVariable}, {@link Condition}, and {@link Action}
 * annotations.
 * <p>
 *
 * <pre>
 * {@literal @}Rule(name = "MyRule", description = "A description", fireCondition = { "Tag1" })
 * public static class MyRule {
 *
 * 	{@literal @}TagValue(type = "Tag1", InjectionStrategy.BY_VALUE)
 * 	private String tag1Value;
 *
 * 	{@literal @}SessionVariable(name = "var", optional = false)
 * 	private String sessionVariable;
 *
 * 	{@literal @}Condition(name = "check", hint = "How to pass the condition.")
 * 	public boolean condition() {
 * 		return true;
 * 	}
 *
 * 	{@literal @}Action(resultTag = "Tag2",resultQuantity = Quantity.SINGLE)
 * 	public String action() {
 * 		return "A new Value";
 * 	}
 * }
 * </pre>
 *
 * @author Claudio Waldvogel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Rule {

	// Note on Checkstyle ignores:
	// Due to bug http://sourceforge.net/p/checkstyle/bugs/641/ it is currently not possible to add
	// @return tags to methods within an @interface definition, thus we currently ignore these
	// incorrect findings.

	/**
	 * The name of the rule.
	 *
	 * @return The name of the rule. //NOCHK
	 */
	String name() default "";

	/**
	 * The description what the purpose of this rule is.
	 *
	 * @return A description what the purpose of this rule is. //NOCHK
	 */
	String description() default "";

	/**
	 * The fireCondition defines which {@link rocks.inspectit.server.diagnosis.engine.tag.Tag}s have
	 * to be already available before this rule can executed. If the fireCondition property is
	 * omitted, the fireCondition is constructed from all {@link TagValue}s.
	 *
	 * @return A list of tag types. //NOCHK
	 * @see TagValue
	 */
	String[] fireCondition() default {};// NOCHK
}
