package rocks.inspectit.server.diagnosis.engine.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import rocks.inspectit.server.diagnosis.engine.rule.ConditionFailure;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.RuleOutput;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.IRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.Session.State;
import rocks.inspectit.server.diagnosis.engine.session.exception.SessionException;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleA;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleB;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleC;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleD;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleE;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleF;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleG;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link Session} class.
 *
 * @author Claudio Waldvogel, Alexander Wert
 */
@SuppressWarnings("all")
public class SessionTest extends TestBase {

	/**
	 * In this Test class we inject the mocks manually in the init() method as otherwise Mockito
	 * uses the wrong constructor for mock injection.
	 */
	Session<String, DefaultSessionResult<String>> session;

	@Mock
	DefaultSessionResultCollector<String> resultCollector;

	@Mock
	SessionContext<String> sessionContext;

	/**
	 * Manual injection of mocks.
	 *
	 */
	@BeforeMethod
	public void init() throws Exception {
		session = new Session<>(sessionContext, resultCollector);
	}

	/**
	 * Tests the {@link Session#activate(Object, java.util.Map)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class Activate extends SessionTest {
		Field stateField;

		@BeforeMethod
		public void initStateField() throws SecurityException, NoSuchFieldException {
			stateField = Session.class.getDeclaredField("state");
			stateField.setAccessible(true);
		}

		@DataProvider(name = "failureStates")
		public Object[][] dataProviderMethod() {
			return new Object[][] { { State.DESTROYED }, { State.FAILURE }, { State.PROCESSED }, { State.ACTIVATED } };
		}

		@Test
		public void fromNew() throws SessionException {
			String input = "Input";
			session.activate(input, Session.EMPTY_SESSION_VARIABLES);

			assertThat(session.getState(), equalTo(Session.State.ACTIVATED));
			verify(sessionContext, times(1)).activate(input, Session.EMPTY_SESSION_VARIABLES);
		}

		@Test
		public void fromPassivate() throws SessionException, IllegalArgumentException, IllegalAccessException {
			stateField.set(session, State.PASSIVATED);

			String input = "Input";
			session.activate(input, Session.EMPTY_SESSION_VARIABLES);

			assertThat(session.getState(), equalTo(Session.State.ACTIVATED));
			verify(sessionContext, times(1)).activate(input, Session.EMPTY_SESSION_VARIABLES);
		}

		@Test(expectedExceptions = SessionException.class, dataProvider = "failureStates")
		public void testWithFailureStates(State state) throws SessionException, IllegalArgumentException, IllegalAccessException {
			stateField.set(session, state);

			session.activate("Input", Session.EMPTY_SESSION_VARIABLES);
		}
	}

	/**
	 * Tests the {@link Session#passivate()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class Passivate extends SessionTest {
		Field stateField;

		@BeforeMethod
		public void initStateField() throws SecurityException, NoSuchFieldException {
			stateField = Session.class.getDeclaredField("state");
			stateField.setAccessible(true);
		}

		@DataProvider(name = "allStates")
		public Object[][] dataProviderMethod() {
			return new Object[][] { { State.NEW }, { State.PASSIVATED }, { State.DESTROYED }, { State.FAILURE }, { State.PROCESSED }, { State.ACTIVATED } };
		}

		@Test(dataProvider = "allStates")
		public void testFromDifferentStates(State state) throws IllegalArgumentException, IllegalAccessException {
			stateField.set(session, state);

			session.passivate();

			assertThat(session.getState(), equalTo(Session.State.PASSIVATED));
			verify(sessionContext, times(1)).passivate();
		}
	}

	/**
	 * Tests the {@link Session#destroy()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class Destroy extends SessionTest {
		Field stateField;

		@BeforeMethod
		public void initStateField() throws SecurityException, NoSuchFieldException {
			stateField = Session.class.getDeclaredField("state");
			stateField.setAccessible(true);
		}

		@DataProvider(name = "destroyableStates")
		public Object[][] dataProviderMethod() {
			return new Object[][] { { State.NEW }, { State.PASSIVATED }, { State.FAILURE }, { State.ACTIVATED } };
		}

		@Test()
		public void fromProcessed() throws IllegalArgumentException, IllegalAccessException {
			stateField.set(session, State.PROCESSED);

			session.destroy();

			assertThat(session.getState(), equalTo(Session.State.DESTROYED));
			verify(sessionContext, times(1)).passivate();
		}

		@Test()
		public void fromDestroyed() throws IllegalArgumentException, IllegalAccessException {
			stateField.set(session, State.DESTROYED);

			session.destroy();

			assertThat(session.getState(), equalTo(Session.State.DESTROYED));
			verifyNoMoreInteractions(sessionContext);
		}

		@Test(dataProvider = "destroyableStates")
		public void testFromDestroyableStates(State state) throws IllegalArgumentException, IllegalAccessException {
			stateField.set(session, state);

			session.destroy();

			assertThat(session.getState(), equalTo(Session.State.DESTROYED));
		}
	}

	/**
	 * Test the {@link Session#findNextRules(Set, Set)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class FindNextRules extends SessionTest {

		@Test
		public void allRulesMatch() throws RuleDefinitionException {
			Set<RuleDefinition> ruleDefinitions = Rules.define(RuleA.class, RuleB.class, RuleC.class);
			Set<String> availableTagTypes = new HashSet<>(Arrays.asList(Tags.ROOT_TAG, "A", "B"));

			Collection<RuleDefinition> nextRules = session.findNextRules(availableTagTypes, ruleDefinitions);

			assertThat(nextRules, containsInAnyOrder(ruleDefinitions.toArray(new RuleDefinition[0])));
		}

		@Test
		public void onlyRuleBMatch() throws RuleDefinitionException {
			RuleDefinition ruleA = Rules.define(RuleA.class);
			RuleDefinition ruleB = Rules.define(RuleB.class);
			RuleDefinition ruleC = Rules.define(RuleC.class);

			Set<RuleDefinition> ruleDefinitions = new HashSet<>(Arrays.asList(ruleA, ruleB, ruleC));
			Set<String> availableTagTypes = new HashSet<>(Arrays.asList(new String[] { "A" }));

			Collection<RuleDefinition> nextRules = session.findNextRules(availableTagTypes, ruleDefinitions);

			assertThat(nextRules, containsInAnyOrder(ruleB));
		}

		@Test
		public void onlyRuleBAndCMatch() throws RuleDefinitionException {
			RuleDefinition ruleA = Rules.define(RuleA.class);
			RuleDefinition ruleB = Rules.define(RuleB.class);
			RuleDefinition ruleC = Rules.define(RuleC.class);

			Set<RuleDefinition> ruleDefinitions = new HashSet<>(Arrays.asList(ruleA, ruleB, ruleC));
			Set<String> availableTagTypes = new HashSet<>(Arrays.asList(new String[] { "A", "B" }));

			Collection<RuleDefinition> nextRules = session.findNextRules(availableTagTypes, ruleDefinitions);

			assertThat(nextRules, containsInAnyOrder(ruleB, ruleC));
		}

		@Test
		public void noneMatch() throws RuleDefinitionException {
			RuleDefinition ruleA = Rules.define(RuleA.class);
			RuleDefinition ruleB = Rules.define(RuleB.class);
			RuleDefinition ruleC = Rules.define(RuleC.class);

			Set<RuleDefinition> ruleDefinitions = new HashSet<>(Arrays.asList(ruleA, ruleB, ruleC));
			Set<String> availableTagTypes = new HashSet<>(Arrays.asList(new String[] { "B" }));

			Collection<RuleDefinition> nextRules = session.findNextRules(availableTagTypes, ruleDefinitions);

			assertThat(nextRules, empty());
		}

	}

	/**
	 * Test the {@link Session#collectInputs(RuleDefinition, IRuleOutputStorage)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class CollectInputs extends SessionTest {
		@Mock
		IRuleOutputStorage outputStorage;

		@Test
		public void singleTagTypeSingleInput() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleB = Rules.define(RuleB.class);
			Tag rootTag = Tags.rootTag("input");
			Tag tagA = Tags.tag("A", "input", rootTag);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptyList(), Collections.singletonList(tagA));
			when(outputStorage.findLatestResultsByTagType(Matchers.<Set<String>> anyObject())).thenReturn(Collections.singleton(outputA));

			List<RuleInput> ruleInputs = new ArrayList<>(session.collectInputs(ruleB, outputStorage));

			assertThat(ruleInputs, hasSize(1));
			assertThat(ruleInputs.get(0).getRoot(), equalTo(tagA));
		}

		@Test
		public void singleTagTypeMultipleInputs() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleB = Rules.define(RuleB.class);
			Tag rootTag = Tags.rootTag("input");
			Tag tagA1 = Tags.tag("A", "A1", rootTag);
			Tag tagA2 = Tags.tag("A", "A2", rootTag);
			Tag tagA3 = Tags.tag("A", "A3", rootTag);
			RuleOutput outputA = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptyList(), Arrays.asList(tagA1, tagA2, tagA3));
			when(outputStorage.findLatestResultsByTagType(Matchers.<Set<String>> anyObject())).thenReturn(Collections.singleton(outputA));

			Collection<RuleInput> ruleInputs = session.collectInputs(ruleB, outputStorage);

			assertThat(ruleInputs, hasSize(3));
			assertThat(ruleInputs, containsInAnyOrder(new RuleInput(tagA1, Sets.newHashSet(tagA1)), new RuleInput(tagA2, Sets.newHashSet(tagA2)), new RuleInput(tagA3, Sets.newHashSet(tagA3))));
		}

		@Test
		public void multipleTagTypesSingleInput() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleC = Rules.define(RuleC.class);
			Tag rootTag = Tags.rootTag("input");
			Tag tagA = Tags.tag("A", "A1", rootTag);
			Tag tagB = Tags.tag("B", "B1", tagA);
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptyList(), Collections.singletonList(tagB));
			when(outputStorage.findLatestResultsByTagType(Matchers.<Set<String>> anyObject())).thenReturn(Collections.singleton(outputB));

			List<RuleInput> ruleInputs = new ArrayList<>(session.collectInputs(ruleC, outputStorage));

			assertThat(ruleInputs, hasSize(1));
			assertThat(ruleInputs.get(0).getRoot(), equalTo(tagB));
			assertThat(ruleInputs.get(0).getUnraveled(), containsInAnyOrder(tagB, tagA));
		}

		@Test
		public void multipleTagTypesMultipleInputs1() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleC = Rules.define(RuleC.class);
			Tag rootTag = Tags.rootTag("input");
			Tag tagA = Tags.tag("A", "A1", rootTag);
			Tag tagB1 = Tags.tag("B", "B1", tagA);
			Tag tagB2 = Tags.tag("B", "B2", tagA);
			RuleOutput outputB = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptyList(), Arrays.asList(tagB1, tagB2));
			when(outputStorage.findLatestResultsByTagType(Matchers.<Set<String>> anyObject())).thenReturn(Collections.singleton(outputB));

			Collection<RuleInput> ruleInputs = session.collectInputs(ruleC, outputStorage);

			assertThat(ruleInputs, hasSize(2));
			assertThat(ruleInputs, containsInAnyOrder(new RuleInput(tagB1, Sets.newHashSet(tagA, tagB1)), new RuleInput(tagB2, Sets.newHashSet(tagA, tagB2))));
		}

		@Test
		public void multipleTagTypesMultipleInputs2() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleC = Rules.define(RuleC.class);
			Tag rootTag = Tags.rootTag("input");
			Tag tagA1 = Tags.tag("A", "A1", rootTag);
			Tag tagB1 = Tags.tag("B", "B1", tagA1);
			Tag tagA2 = Tags.tag("A", "A2", rootTag);
			Tag tagB2 = Tags.tag("B", "B2", tagA2);
			RuleOutput outputB1 = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptyList(), Arrays.asList(tagB1));
			RuleOutput outputB2 = new RuleOutput("RuleB", "B", Collections.<ConditionFailure> emptyList(), Arrays.asList(tagB2));
			when(outputStorage.findLatestResultsByTagType(Matchers.<Set<String>> anyObject())).thenReturn(Arrays.asList(outputB1, outputB2));

			Collection<RuleInput> ruleInputs = session.collectInputs(ruleC, outputStorage);

			assertThat(ruleInputs, hasSize(2));
			assertThat(ruleInputs, containsInAnyOrder(new RuleInput(tagB1, Sets.newHashSet(tagA1, tagB1)), new RuleInput(tagB2, Sets.newHashSet(tagA2, tagB2))));
		}

		@Test
		public void noMatch1() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleC = Rules.define(RuleC.class);
			when(outputStorage.findLatestResultsByTagType(Matchers.<Set<String>> anyObject())).thenReturn(Collections.singleton(Rules.triggerRuleOutput("input")));

			Collection<RuleInput> ruleInputs = session.collectInputs(ruleC, outputStorage);

			assertThat(ruleInputs, empty());
		}

		@Test
		public void noMatch2() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleC = Rules.define(RuleC.class);
			Tag rootTag = Tags.rootTag("input");
			Tag tagA1 = Tags.tag("A", "A1", rootTag);
			Tag tagB1 = Tags.tag("B", "B1", rootTag);
			RuleOutput outputB1 = new RuleOutput("RuleA", "A", Collections.<ConditionFailure> emptyList(), Arrays.asList(tagB1));
			when(outputStorage.findLatestResultsByTagType(Matchers.<Set<String>> anyObject())).thenReturn(Arrays.asList(outputB1));

			Collection<RuleInput> ruleInputs = session.collectInputs(ruleC, outputStorage);

			assertThat(ruleInputs, empty());
		}
	}

	/**
	 * Test the {@link Session#filterProcessedInputs(Multimap, RuleDefinition, Collection)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class FilterProcessedInputs extends SessionTest {

		@Test
		public void filterOne() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleB = Rules.define(RuleB.class);
			RuleInput inputA1 = new RuleInput(Tags.tag("A", "A1"));
			RuleInput inputA2 = new RuleInput(Tags.tag("A", "A2"));
			RuleInput inputA3 = new RuleInput(Tags.tag("A", "A3"));
			RuleInput inputB = new RuleInput(Tags.tag("B", "XY"));
			RuleInput inputC = new RuleInput(Tags.tag("C", "XY"));
			Collection<RuleInput> inputs = Arrays.asList(inputA1, inputA2, inputA3, inputB, inputC);
			Multimap<RuleDefinition, RuleInput> executions = ArrayListMultimap.create();
			executions.put(ruleB, inputA1);

			inputs = session.filterProcessedInputs(executions, ruleB, inputs);

			assertThat(inputs, containsInAnyOrder(inputA2, inputA3, inputB, inputC));
		}

		@Test
		public void filterTwo() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleB = Rules.define(RuleB.class);
			RuleDefinition ruleA = Rules.define(RuleA.class);
			RuleInput inputA1 = new RuleInput(Tags.tag("A", "A1"));
			RuleInput inputA2 = new RuleInput(Tags.tag("A", "A2"));
			RuleInput inputA3 = new RuleInput(Tags.tag("A", "A3"));
			RuleInput inputB = new RuleInput(Tags.tag("B", "XY"));
			RuleInput inputC = new RuleInput(Tags.tag("C", "XY"));
			Collection<RuleInput> inputs = Arrays.asList(inputA1, inputA2, inputA3, inputB, inputC);
			Multimap<RuleDefinition, RuleInput> executions = ArrayListMultimap.create();
			executions.put(ruleA, inputA1);
			executions.put(ruleB, inputA2);
			executions.put(ruleB, inputA3);

			inputs = session.filterProcessedInputs(executions, ruleB, inputs);

			assertThat(inputs, containsInAnyOrder(inputA1, inputB, inputC));
		}

		@Test
		public void filterAll() throws RuleDefinitionException, SessionException {
			RuleDefinition ruleB = Rules.define(RuleB.class);
			RuleDefinition ruleA = Rules.define(RuleA.class);
			RuleInput inputA1 = new RuleInput(Tags.tag("A", "A1"));
			RuleInput inputA2 = new RuleInput(Tags.tag("A", "A2"));
			RuleInput inputA3 = new RuleInput(Tags.tag("A", "A3"));
			RuleInput inputB = new RuleInput(Tags.tag("B", "XY"));
			RuleInput inputC = new RuleInput(Tags.tag("C", "XY"));
			Collection<RuleInput> inputs = Arrays.asList(inputA1, inputA2, inputA3, inputB, inputC);
			Multimap<RuleDefinition, RuleInput> executions = ArrayListMultimap.create();
			executions.put(ruleB, inputA1);
			executions.put(ruleB, inputA2);
			executions.put(ruleB, inputA3);
			executions.put(ruleB, inputB);
			executions.put(ruleB, inputC);

			inputs = session.filterProcessedInputs(executions, ruleB, inputs);

			assertThat(inputs, empty());
		}
	}

	/**
	 * Test the {@link Session#call()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class Call extends SessionTest {


		@Override
		@BeforeMethod
		public void init() {
			// nothing to do here
		}

		@Test
		public void fromActivated() throws Exception {
			session = new Session<>(Rules.define(RuleA.class, RuleB.class, RuleC.class), new DefaultSessionResultCollector<String>());
			String input = "input";
			session.activate(input, Session.EMPTY_SESSION_VARIABLES);
			DefaultSessionResult<String> result = session.call();

			assertThat(session.getState(), equalTo(State.PROCESSED));
			assertThat(result.getConditionFailures().keys(), empty());
			assertThat(result.getEndTags().keySet(), containsInAnyOrder("C"));
			assertThat(result.getEndTags().get("C"), hasSize(1));
			assertThat(((Integer) result.getEndTags().get("C").iterator().next().getValue()), equalTo((input.length() * 2) + 2));
		}

		@Test
		public void twoOfThreeRulesApply() throws Exception {
			session = new Session<>(Rules.define(RuleA.class, RuleE.class, RuleF.class), new DefaultSessionResultCollector<String>());
			String input = "input";
			session.activate(input, Session.EMPTY_SESSION_VARIABLES);
			DefaultSessionResult<String> result = session.call();

			assertThat(session.getState(), equalTo(State.PROCESSED));
			assertThat(result.getConditionFailures().keys(), empty());
			assertThat(result.getEndTags().keySet(), hasSize(2));
			assertThat(result.getEndTags().keySet(), containsInAnyOrder("A", "E"));
		}

		@Test
		public void ruleWithConditionFailure() throws Exception {
			session = new Session<>(Rules.define(RuleA.class, RuleG.class), new DefaultSessionResultCollector<String>());
			String input = "input";
			session.activate(input, Session.EMPTY_SESSION_VARIABLES);
			DefaultSessionResult<String> result = session.call();

			assertThat(session.getState(), equalTo(State.PROCESSED));
			assertThat(result.getConditionFailures().keySet(), containsInAnyOrder("RuleG"));
			assertThat(result.getEndTags().keySet(), hasSize(1));
			assertThat(result.getEndTags().keySet(), containsInAnyOrder("A"));
		}

		@Test(expectedExceptions = { SessionException.class })
		public void fromNonActivatedState() throws Exception {
			session = new Session<>(Rules.define(RuleA.class, RuleB.class, RuleC.class), new DefaultSessionResultCollector<String>());

			session.call();
		}

		@Test(expectedExceptions = { SessionException.class })
		public void withRuleException() throws Exception {
			session = new Session<>(Rules.define(RuleA.class, RuleD.class), new DefaultSessionResultCollector<String>());
			String input = "input";
			session.activate(input, Session.EMPTY_SESSION_VARIABLES);

			session.call();
		}
	}
}
