package rocks.inspectit.server.diagnosis.engine.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleA;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleF;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleG;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleWithSessionVariable;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link RuleDefinition} class.
 *
 * @author Alexander Wert
 *
 */
public class RuleDefinitionTest extends TestBase {
	/**
	 * Tests the {@link RuleDefinition#execute(RuleInput, java.util.Map)} and
	 * {@link RuleDefinition#execute(java.util.Collection, java.util.Map)} methods.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Execute extends RuleDefinitionTest {

		@Test
		public void simpleRuleExecution() throws RuleDefinitionException, RuleExecutionException {
			RuleDefinition ruleDefinition = Rules.define(RuleA.class);
			String inputStr = "hallo";
			RuleInput input = new RuleInput(Tags.tag(Tags.ROOT_TAG, inputStr));

			RuleOutput output = ruleDefinition.execute(input, Collections.<String, Object> emptyMap());

			assertThat(output.getRuleName(), equalTo(ruleDefinition.getName()));
			assertThat(output.getTags(), hasSize(1));
			assertThat(output.getTags().iterator().next().getValue(), equalTo((Object) (inputStr + inputStr)));
		}

		@Test
		public void ruleWithSessionVariableExecution() throws RuleDefinitionException, RuleExecutionException {
			RuleDefinition ruleDefinition = Rules.define(RuleWithSessionVariable.class);
			String inputStr = "hallo";
			String sessionVar = "sessionVar";
			RuleInput input = new RuleInput(Tags.tag(Tags.ROOT_TAG, inputStr));

			RuleOutput output = ruleDefinition.execute(input, Collections.singletonMap(sessionVar, (Object) sessionVar));

			assertThat(output.getRuleName(), equalTo(ruleDefinition.getName()));
			assertThat(output.getTags(), hasSize(1));
			assertThat(output.getTags().iterator().next().getValue(), equalTo((Object) (inputStr + sessionVar)));
		}

		@Test
		public void ruleWithConditionSuccessExecution() throws RuleDefinitionException, RuleExecutionException {
			RuleDefinition ruleDefinition = Rules.define(RuleF.class);
			String inputStrA = "hallo";
			String inputStrE = "again";
			Tag tagA = Tags.tag("A", inputStrA);
			Tag tagE = Tags.tag("E", inputStrE);
			RuleInput inputA = new RuleInput(tagA);
			RuleInput inputE = new RuleInput(tagE, Arrays.asList(tagA, tagE));

			RuleOutput output = ruleDefinition.execute(inputE, Collections.<String, Object> emptyMap());

			assertThat(output.getRuleName(), equalTo(ruleDefinition.getName()));
			assertThat(output.getTags(), hasSize(1));
			assertThat(output.getTags().iterator().next().getValue(), equalTo((Object) (inputStrA + inputStrE)));
		}

		@Test
		public void ruleWithConditionFailureExecution() throws RuleDefinitionException, RuleExecutionException {
			RuleDefinition ruleDefinition = Rules.define(RuleG.class);
			String inputStr = "hallo";
			RuleInput input = new RuleInput(Tags.tag("A", inputStr));

			RuleOutput output = ruleDefinition.execute(input, Collections.<String, Object> emptyMap());

			assertThat(output.getRuleName(), equalTo(ruleDefinition.getName()));
			assertThat(output.getConditionFailures(), hasSize(1));
			assertThat(output.getTags(), empty());
		}
	}

	/**
	 * Tests the {@link RuleDefinition.RuleDefinitionBuilder#build()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class RuleDefinitionBuilder_Build extends RuleDefinitionTest {

		@InjectMocks
		rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition.RuleDefinitionBuilder builder;

		@Mock
		ActionMethod actionMethod;

		@Mock
		ConditionMethod conditionMethod;

		@Mock
		FireCondition fireCondition;

		@Mock
		SessionVariableInjection sessionVariableInjection;

		@Mock
		TagInjection tagInjection;

		@Test
		public void valid() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();

			assertThat(ruleDefinition.getActionMethod(), equalTo(actionMethod));
			assertThat(ruleDefinition.getConditionMethods(), equalTo(Arrays.asList(conditionMethod)));
			assertThat(ruleDefinition.getDescription(), equalTo("Some Description"));
			assertThat(ruleDefinition.getFireCondition(), equalTo(fireCondition));
			assertThat((Class) ruleDefinition.getImplementation(), equalTo((Class) RuleA.class));
			assertThat(ruleDefinition.getName(), equalTo("SomeName"));
			assertThat(ruleDefinition.getSessionVariableInjections(), equalTo(Arrays.asList(sessionVariableInjection)));
			assertThat(ruleDefinition.getTagInjections(), equalTo(Arrays.asList(tagInjection)));
		}

		@Test
		public void emptyConditionMethods() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Collections.<ConditionMethod> emptyList());
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();

			assertThat(ruleDefinition.getActionMethod(), equalTo(actionMethod));
			assertThat(ruleDefinition.getConditionMethods(), empty());
			assertThat(ruleDefinition.getDescription(), equalTo("Some Description"));
			assertThat(ruleDefinition.getFireCondition(), equalTo(fireCondition));
			assertThat((Class) ruleDefinition.getImplementation(), equalTo((Class) RuleA.class));
			assertThat(ruleDefinition.getName(), equalTo("SomeName"));
			assertThat(ruleDefinition.getSessionVariableInjections(), equalTo(Arrays.asList(sessionVariableInjection)));
			assertThat(ruleDefinition.getTagInjections(), equalTo(Arrays.asList(tagInjection)));
		}

		@Test
		public void emptySessionVariables() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Collections.<SessionVariableInjection> emptyList());
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();

			assertThat(ruleDefinition.getActionMethod(), equalTo(actionMethod));
			assertThat(ruleDefinition.getConditionMethods(), equalTo(Arrays.asList(conditionMethod)));
			assertThat(ruleDefinition.getDescription(), equalTo("Some Description"));
			assertThat(ruleDefinition.getFireCondition(), equalTo(fireCondition));
			assertThat((Class) ruleDefinition.getImplementation(), equalTo((Class) RuleA.class));
			assertThat(ruleDefinition.getName(), equalTo("SomeName"));
			assertThat(ruleDefinition.getSessionVariableInjections(), empty());
			assertThat(ruleDefinition.getTagInjections(), equalTo(Arrays.asList(tagInjection)));
		}

		@Test
		public void missingDescription() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();

			assertThat(ruleDefinition.getActionMethod(), equalTo(actionMethod));
			assertThat(ruleDefinition.getConditionMethods(), equalTo(Arrays.asList(conditionMethod)));
			assertThat(ruleDefinition.getDescription(), equalTo(RuleDefinition.EMPTY_DESCRIPTION));
			assertThat(ruleDefinition.getFireCondition(), equalTo(fireCondition));
			assertThat((Class) ruleDefinition.getImplementation(), equalTo((Class) RuleA.class));
			assertThat(ruleDefinition.getName(), equalTo("SomeName"));
			assertThat(ruleDefinition.getSessionVariableInjections(), equalTo(Arrays.asList(sessionVariableInjection)));
			assertThat(ruleDefinition.getTagInjections(), equalTo(Arrays.asList(tagInjection)));
		}

		@Test
		public void missingName() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();

			assertThat(ruleDefinition.getActionMethod(), equalTo(actionMethod));
			assertThat(ruleDefinition.getConditionMethods(), equalTo(Arrays.asList(conditionMethod)));
			assertThat(ruleDefinition.getDescription(), equalTo("Some Description"));
			assertThat(ruleDefinition.getFireCondition(), equalTo(fireCondition));
			assertThat((Class) ruleDefinition.getImplementation(), equalTo((Class) RuleA.class));
			assertThat(ruleDefinition.getName(), equalTo(RuleA.class.getCanonicalName()));
			assertThat(ruleDefinition.getSessionVariableInjections(), equalTo(Arrays.asList(sessionVariableInjection)));
			assertThat(ruleDefinition.getTagInjections(), equalTo(Arrays.asList(tagInjection)));
		}

		@Test(expectedExceptions = { IllegalStateException.class })
		public void missingActionMethod() {
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();
		}

		@Test(expectedExceptions = { IllegalStateException.class })
		public void missingConditionMethods() {
			builder.setActionMethod(actionMethod);
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();
		}

		@Test(expectedExceptions = { IllegalStateException.class })
		public void missingFireCondition() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();
		}

		@Test(expectedExceptions = { IllegalStateException.class })
		public void missingImplementation() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();
		}

		@Test(expectedExceptions = { IllegalStateException.class })
		public void missingSessionVariables() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setTagInjections(Arrays.asList(tagInjection));

			RuleDefinition ruleDefinition = builder.build();
		}

		@Test(expectedExceptions = { IllegalStateException.class })
		public void missingTagInjections() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));

			RuleDefinition ruleDefinition = builder.build();
		}

		@Test(expectedExceptions = { IllegalStateException.class })
		public void emptyTagInjections() {
			builder.setActionMethod(actionMethod);
			builder.setConditionMethods(Arrays.asList(conditionMethod));
			builder.setDescription("Some Description");
			builder.setFireCondition(fireCondition);
			builder.setImplementation(RuleA.class);
			builder.setName("SomeName");
			builder.setSessionVariableInjections(Arrays.asList(sessionVariableInjection));
			builder.setTagInjections(Collections.<TagInjection> emptyList());

			RuleDefinition ruleDefinition = builder.build();
		}
	}
}
