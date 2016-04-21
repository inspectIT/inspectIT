package rocks.inspectit.server.diagnosis.engine.rule.factory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.ActionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.ConditionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.SessionVariableInjection;
import rocks.inspectit.server.diagnosis.engine.rule.TagInjection;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link Rules} class.
 * 
 * @author Claudio Waldvogel
 */
@SuppressWarnings("PMD")
public class RulesTest extends TestBase {

	/**
	 * Tests the {@link Rules#define(Class)} method;
	 * 
	 * @author Alexander Wert
	 *
	 */
	public static class Define extends RulesTest {

		@Test
		public void testWithRuleAnnotation() throws Exception {
			RuleDefinition definition = Rules.define(ValidAndAnnotated.class);
			assertThat(definition.getName(), is("AnnotatedRule"));
			assertThat(definition.getDescription(), is("Description"));
			assertThat(definition.getFireCondition().getTagTypes(), containsInAnyOrder("T1", "T2"));

			// Test tag injections
			TagInjection tagInjection = new TagInjection("T1", ValidAndAnnotated.class.getDeclaredField("t1AsTag"), TagValue.InjectionStrategy.BY_TAG);
			TagInjection tagInjection1 = new TagInjection("T2", ValidAndAnnotated.class.getDeclaredField("t2TagValue"), TagValue.InjectionStrategy.BY_VALUE);
			assertThat(definition.getTagInjections(), is(notNullValue()));
			assertThat(definition.getTagInjections(), containsInAnyOrder(tagInjection, tagInjection1));

			// Test session variables
			SessionVariableInjection s1 = new SessionVariableInjection("baseline", false, ValidAndAnnotated.class.getDeclaredField("baseline"));
			SessionVariableInjection s2 = new SessionVariableInjection("baseline2", true, ValidAndAnnotated.class.getDeclaredField("baseline2"));
			assertThat(definition.getSessionVariableInjections(), containsInAnyOrder(s1, s2));

			// Test action method
			assertThat(definition.getActionMethod(), is(new ActionMethod(ValidAndAnnotated.class.getDeclaredMethod("action"), "T2", Action.Quantity.SINGLE)));

			// Test condition method
			ConditionMethod conditionMethod = new ConditionMethod("myCondition", "No way out", ValidAndAnnotated.class.getDeclaredMethod("condition"));
			assertThat(definition.getConditionMethods(), containsInAnyOrder(conditionMethod));

		}

		@Test(expectedExceptions = RuleDefinitionException.class)
		public void testWithoutRuleAnnotation() throws RuleDefinitionException {
			Rules.define(MissingAnnotation.class);
		}

		@Test(expectedExceptions = RuleDefinitionException.class)
		public void testMissingActionMethod() throws RuleDefinitionException {
			Rules.define(NoActionMethodDefined.class);
		}

		@Test(expectedExceptions = RuleDefinitionException.class)
		public void testInvalidActionMethodReturnType() throws RuleDefinitionException {
			Rules.define(InvalidActionMethodReturnType.class);
		}

		@Test(expectedExceptions = RuleDefinitionException.class)
		public void testMultipleActionMethods() throws RuleDefinitionException {
			Rules.define(MultipleActionMethodsDefined.class);
		}

		@Test(expectedExceptions = RuleDefinitionException.class)
		public void testMissingTagValueMethod() throws RuleDefinitionException {
			Rules.define(NoTagValueDefined.class);
		}
	}

	@Rule(name = "AnnotatedRule", description = "Description", fireCondition = { "T1", "T2" })
	public static class ValidAndAnnotated {

		@SessionVariable(name = "baseline")
		private int baseline;

		@SessionVariable(name = "baseline2", optional = true)
		private int baseline2;

		@TagValue(type = "T1", injectionStrategy = TagValue.InjectionStrategy.BY_TAG)
		public Tag t1AsTag;

		@TagValue(type = "T2", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		public String t2TagValue;

		@Condition(name = "myCondition", hint = "No way out")
		public boolean condition() {
			return false;
		}

		@Action(resultTag = "T2")
		public String action() {
			return "executed";
		}
	}

	public static class MissingAnnotation {

		@TagValue(type = "root", injectionStrategy = TagValue.InjectionStrategy.BY_TAG)
		private Tag rootTag;

		@TagValue(type = "T1")
		private String t1Value;

		@SessionVariable(name = "baseline", optional = false)
		private int baseline;

		@Action(resultTag = "T2")
		public String execute() {
			return "executed";
		}

	}

	public static class NoActionMethodDefined {
		@TagValue(type = "T1", injectionStrategy = TagValue.InjectionStrategy.BY_TAG)
		public Tag t;
	}

	public static class InvalidActionMethodReturnType {
		@Action(resultTag = "T2", resultQuantity = Action.Quantity.MULTIPLE)
		public String execute() {
			return "executed";
		}
	}

	public static class MultipleActionMethodsDefined {
		@TagValue(type = "T1", injectionStrategy = TagValue.InjectionStrategy.BY_TAG)
		public Tag t;

		@Action(resultTag = "T1")
		public String execute() {
			return "executed";
		}

		@Action(resultTag = "T2")
		public String execut2() {
			return "executed";
		}
	}

	public static class NoTagValueDefined {
		@Action(resultTag = "T2")
		public String execute() {
			return "executed";
		}
	}

}
