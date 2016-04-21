package rocks.inspectit.server.diagnosis.engine.rule.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks the actual executing method of a rule implementation. <b> Be aware that a
 * rule can define only one action method.</b>
 * <p>
 *
 * <pre>
 *   {@literal @}Action(resultTag = "SomeTag", resultQuantity = SINGLE)
 *   public Object action(){
 *   	return "A new Value";
 *   }
 * </pre>
 *
 * @author Claudio Waldvogel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action {

	/**
	 * Defines how the output of a rule is processed.
	 */
	enum Quantity {
		/**
		 * Regardless what kind of output the rules produces, it results it is wrapped in a single
		 * <code>Tag</code> of type #resultTag.
		 */
		SINGLE,

		/**
		 * If a MULTIPLE output quantity is requested, the rule implementation has to return either
		 * an Array or a Collection. For each element within the Array/Collection a new
		 * <code>Tag</code> of type #resultTag is created.
		 */
		MULTIPLE
	}

	// Note on Checkstyle ignores:
	// Due to bug http://sourceforge.net/p/checkstyle/bugs/641/ it is currently not possible to add
	// @return tags to methods within an @interface definition, thus we currently ignore these
	// incorrect findings.

	/**
	 * The type of tag this rules produces. The rule itself must not return <code>Tag</code>
	 * instances. The rule can return any kind of object. The returned value(s) are wrapped and
	 * stored by the diagnosis engine itself. Anyway, the rule can control how the result is wrapped
	 * by providing a #resultQuantity.
	 *
	 * @return The type of tag this rule produces. //NOCHK
	 */
	String resultTag();

	/**
	 * @return The required resultQuantity. Default is SINGLE. //NOCHK
	 * @see Quantity
	 */
	Action.Quantity resultQuantity() default Action.Quantity.SINGLE;

}
