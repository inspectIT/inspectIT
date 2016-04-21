package rocks.inspectit.server.diagnosis.engine.rule.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.beust.jcommander.internal.Lists;

import rocks.inspectit.server.diagnosis.engine.rule.ActionMethod;
import rocks.inspectit.server.diagnosis.engine.rule.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDummy;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
public class ActionMethodTest extends TestBase {

	@Mock
	RuleInput input;

	@Mock
	RuleDefinition definition;

	@Mock
	SessionVariables variables;

	@Mock(name = "instance")
	RuleDummy dummy;

	/**
	 * Tests {@link ActionMethod#execute(ExecutionContext)} method.
	 */
	public static class Execute extends ActionMethodTest {
		private ExecutionContext context;

		@BeforeMethod
		public void init() {
			context = new ExecutionContext(definition, dummy, input, variables);
		}

		@Test
		public void shouldProduceSingleTagWithSingleObjectValue() throws Exception {
			// prepare Mocks
			Tag rootTag = Tags.rootTag("Input");
			Tag expectedResultTag = new Tag("T2", "oneResult", rootTag);
			when(dummy.action()).thenReturn("oneResult");
			when(input.getRoot()).thenReturn(rootTag);
			// Create TestMethod
			ActionMethod action = new ActionMethod(RuleDummy.actionMethod(), "T2", Action.Quantity.SINGLE);

			// execute
			Collection<Tag> result = action.execute(context);

			// verify
			assertThat(result, hasSize(1));
			assertThat(result, hasItem(expectedResultTag));
		}

		@Test
		public void shouldProduceSingleTagWithArrayValue() throws Exception {
			// prepare Mocks
			Tag rootTag = Tags.rootTag("Input");
			Tag expectedResultTag = new Tag("T2", new String[] { "one", "two", "three" }, rootTag);
			when(dummy.action()).thenReturn(new String[] { "one", "two", "three" });
			when(this.input.getRoot()).thenReturn(rootTag);
			// Create TestMethod
			ActionMethod action = new ActionMethod(RuleDummy.actionMethod(), "T2", Action.Quantity.SINGLE);

			// execute
			Collection<Tag> result = action.execute(context);

			// verify
			assertThat(result, hasSize(1));
			assertThat(result, hasItem(expectedResultTag));
		}

		@Test
		public void shouldProduceMultipleTagsFromSingleArray() throws Exception {
			Tag rootTag = Tags.rootTag("Input");
			// prepare Mocks
			when(dummy.action()).thenReturn(new String[] { "one", "two", "three" });
			when(this.input.getRoot()).thenReturn(rootTag);
			// Create TestMethod
			ActionMethod action = new ActionMethod(RuleDummy.actionMethod(), "T2", Action.Quantity.MULTIPLE);

			// execute
			Collection<Tag> result = action.execute(context);

			// verify
			Collection<Tag> tags = Tags.tags("T2", rootTag, "one", "two", "three");
			assertThat(result, containsInAnyOrder(tags.toArray()));
		}

		@Test
		public void shouldProduceMultipleTagsFromCollection() throws Exception {
			// prepare Mocks
			Tag rootTag = Tags.rootTag("Input");
			when(dummy.action()).thenReturn(Lists.newArrayList("one", "two", "three"));
			when(this.input.getRoot()).thenReturn(rootTag);
			// Create TestMethod
			ActionMethod action = new ActionMethod(RuleDummy.actionMethod(), "T2", Action.Quantity.MULTIPLE);

			// execute
			Collection<Tag> result = action.execute(context);

			// verify
			Collection<Tag> tags = Tags.tags("T2", rootTag, "one", "two", "three");
			assertThat(result, containsInAnyOrder(tags.toArray()));
		}

		@Test(expectedExceptions = RuleExecutionException.class)
		public void shouldFailDueToQuantityAndResultMismatch() {
			Tag rootTag = Tags.rootTag("Input");
			when(dummy.action()).thenReturn("Fail");
			when(this.input.getRoot()).thenReturn(rootTag);

			// Execute and fail. ActionMethod would expect array/collection as result from ruleImpl
			// implementation.
			// But receives "Fail" String
			new ActionMethod(RuleDummy.actionMethod(), "T2", Action.Quantity.MULTIPLE).execute(context);
		}
	}
}
