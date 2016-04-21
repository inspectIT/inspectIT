package rocks.inspectit.server.diagnosis.engine.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action.Quantity;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.exception.SessionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
public class SessionTest extends TestBase {

	@InjectMocks
	Session<String, DefaultSessionResult<String>> session;

	@Spy
	Set<RuleDefinition> ruleDefinitions = new HashSet<>();

	@Spy
	DefaultSessionResultCollector<String> resultCollector = new DefaultSessionResultCollector<>();

	@Spy
	DefaultRuleOutputStorage outputStorage = new DefaultRuleOutputStorage();

	@AfterMethod
	public void cleanUp() {
		outputStorage.clear();
	}

	public static class Activate extends SessionTest {
		@Test
		public void shouldActivateFromNew() {
			session.activate("Input", SessionVariables.EMPTY_VARIABLES);
			assertThat(session.getState(), equalTo(Session.State.ACTIVATED));
		}

		@Test
		public void shouldReActivateFromPassivate() {
			session.activate("Input", SessionVariables.EMPTY_VARIABLES);
			assertThat(session.getState(), equalTo(Session.State.ACTIVATED));
			session.passivate();
			assertThat(session.getState(), equalTo(Session.State.PASSIVATED));
			session.activate("String", SessionVariables.EMPTY_VARIABLES);
			assertThat(session.getState(), equalTo(Session.State.ACTIVATED));
		}

		@Test(expectedExceptions = SessionException.class)
		public void shouldNotActivateBecauseActivated() {
			session.activate("Input", SessionVariables.EMPTY_VARIABLES);
			// should fail because session is already activated
			session.activate("String", SessionVariables.EMPTY_VARIABLES);
		}
	}

	public static class Call extends SessionTest {
		private static final String INPUT_STRING = "INPUT";
		@Test
		public void normalExecution() throws Exception {
			session.getSessionContext().getRuleSet().clear();
			session.getSessionContext().getRuleSet().addAll(Rules.define(RootRule.class, SimpleRule.class));

			session.activate(INPUT_STRING, SessionVariables.EMPTY_VARIABLES);
			DefaultSessionResult<String> result = session.call();

			assertThat(result.getEndTags().size(), equalTo(1));
			assertThat(result.getEndTags().get(SimpleRule.RESULT_TAG).size(), equalTo(1));
			String resultValue = (String) result.getEndTags().get(SimpleRule.RESULT_TAG).iterator().next().getValue();
			assertThat(resultValue, containsString(INPUT_STRING));
			assertThat(resultValue, containsString(RootRule.INSIGHT));
			assertThat(resultValue, containsString(SimpleRule.INSIGHT));
		}

		@Test
		public void multipleInputs() throws Exception {
			session.getSessionContext().getRuleSet().clear();
			session.getSessionContext().getRuleSet().addAll(Rules.define(RootRule.class, SimpleRule.class, TwoInputsRule.class));

			session.activate(INPUT_STRING, SessionVariables.EMPTY_VARIABLES);
			DefaultSessionResult<String> result = session.call();

			assertThat(result.getEndTags().size(), equalTo(1));
			assertThat(result.getEndTags().get(TwoInputsRule.RESULT_TAG).size(), equalTo(1));
			String resultValue = (String) result.getEndTags().get(TwoInputsRule.RESULT_TAG).iterator().next().getValue();
			assertThat(resultValue, equalTo(TwoInputsRule.INSIGHT));
		}

		@Test
		public void multipleOutputs() throws Exception {
			session.getSessionContext().getRuleSet().clear();
			session.getSessionContext().getRuleSet().addAll(Rules.define(RootRule.class, TwoOutputsRule.class, SimpleLeafRule.class));

			session.activate(INPUT_STRING, SessionVariables.EMPTY_VARIABLES);
			DefaultSessionResult<String> result = session.call();

			assertThat(result.getEndTags().size(), equalTo(2));
			assertThat(result.getEndTags().get(SimpleLeafRule.RESULT_TAG).size(), equalTo(2));
			Iterator<Tag> iterator = result.getEndTags().get(SimpleLeafRule.RESULT_TAG).iterator();
			List<String> stringValues = new ArrayList<>();

			while (iterator.hasNext()) {
				stringValues.add((String) iterator.next().getValue());
			}

			List<String> expectedValues = new ArrayList<>();
			expectedValues.add(StringUtils.join(new String[] { INPUT_STRING, RootRule.INSIGHT, TwoOutputsRule.INSIGHT_1, SimpleLeafRule.INSIGHT }));
			expectedValues.add(StringUtils.join(new String[] { INPUT_STRING, RootRule.INSIGHT, TwoOutputsRule.INSIGHT_2, SimpleLeafRule.INSIGHT }));

			assertThat(CollectionUtils.isEqualCollection(expectedValues, stringValues), equalTo(true));
		}

		@Test
		public void recursiveExecution() throws Exception {
			session.getSessionContext().getRuleSet().clear();
			session.getSessionContext().getRuleSet().addAll(Rules.define(RootRule.class, RecursiveRule.class));

			session.activate(INPUT_STRING, SessionVariables.EMPTY_VARIABLES);
			DefaultSessionResult<String> result = session.call();

			assertThat(result.getEndTags().size(), equalTo(1));
			assertThat(result.getEndTags().get(RecursiveRule.RESULT_TAG).size(), equalTo(1));
			String resultValue = (String) result.getEndTags().get(RecursiveRule.RESULT_TAG).iterator().next().getValue();
			assertThat(resultValue, containsString(INPUT_STRING));
			assertThat(RecursiveRule.countOccurrence(RecursiveRule.INSIGHT, resultValue), equalTo(RecursiveRule.MAX_RECURSION_DEPTH));
		}

	}

	/**
	 * Entry Rule used for all scenarios.
	 *
	 * @author Alexander Wert
	 *
	 */
	@Rule
	public static class RootRule {
		public static final String INSIGHT = "RootInsight";
		public static final String RESULT_TAG = "Tag_1";

		@TagValue(type = Tags.ROOT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = RESULT_TAG)
		public String action() {
			return input + INSIGHT;
		}

	}

	/**
	 * Simple Rule depending on {@link RootRule}.
	 *
	 * @author Alexander Wert
	 *
	 */
	@Rule
	public static class SimpleRule {
		public static final String INSIGHT = "SimpleInsight";
		public static final String RESULT_TAG = "SimpleTag";

		@TagValue(type = RootRule.RESULT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = RESULT_TAG)
		public String action() {
			return input + INSIGHT;
		}
	}

	/**
	 * Recursive rule that generates results of the same type as consumed. Recursion is limited by a
	 * recursion depth condition.
	 *
	 * Depends on {@link RootRule}.
	 *
	 * @author Alexander Wert
	 *
	 */
	@Rule
	public static class RecursiveRule {
		public static final String INSIGHT = "Recursive";
		public static final String RESULT_TAG = RootRule.RESULT_TAG;
		public static final int MAX_RECURSION_DEPTH = 3;

		public static int countOccurrence(String snippet, String string) {
			String copy;
			String tmp = string;
			int count = -1;
			do {
				count++;
				copy = tmp;
				tmp = copy.replaceFirst(snippet, "");
			} while (!tmp.equals(copy));
			return count;
		}

		@TagValue(type = RootRule.RESULT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Condition(name = "Recursion Depth Check")
		public boolean checkRecursionDepth() {
			return countOccurrence(INSIGHT, input) < MAX_RECURSION_DEPTH;
		}

		@Action(resultTag = RESULT_TAG)
		public String action() {
			return input + INSIGHT;
		}
	}

	/**
	 * A rule consuming two types of inputs.
	 *
	 * Depends on {@link RootRule} and {@link SimpleRule}.
	 *
	 * @author Alexander Wert
	 *
	 */
	@Rule
	public static class TwoInputsRule {
		public static final String INSIGHT = "TwoInputs";
		public static final String RESULT_TAG = "TwoInputsTag";


		@TagValue(type = RootRule.RESULT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@TagValue(type = SimpleRule.RESULT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input2;

		@Condition(name = "Check Containment")
		public boolean checkContainment() {
			return !input.isEmpty() && !input2.isEmpty() && input2.contains(input);
		}

		@Action(resultTag = RESULT_TAG)
		public String action() {
			return INSIGHT;
		}
	}

	@Rule
	public static class TwoOutputsRule {
		public static final String INSIGHT_1 = "TwoOutputs1";
		public static final String INSIGHT_2 = "TwoOutputs2";
		public static final String RESULT_TAG = "TwoOutputsTag";

		@TagValue(type = RootRule.RESULT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = RESULT_TAG, resultQuantity = Quantity.MULTIPLE)
		public String[] action() {
			return new String[] { input + INSIGHT_1, input + INSIGHT_2 };
		}
	}

	@Rule
	public static class SimpleLeafRule {
		public static final String INSIGHT = "SimpleLeafInsight";
		public static final String RESULT_TAG = "SimpleLeafTag";

		@TagValue(type = TwoOutputsRule.RESULT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = RESULT_TAG)
		public String action() {
			return input + INSIGHT;
		}
	}

}
