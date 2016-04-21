package rocks.inspectit.server.diagnosis.engine.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleDummy;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link ConditionMethod} class.
 *
 * @author Alexander Wert
 *
 */
public class ConditionMethodTest extends TestBase {

	@Mock
	RuleInput input;

	@Mock
	RuleDefinition definition;

	@Mock
	Map<String, Object> variables;

	@Mock(name = "instance")
	RuleDummy dummy;

	/**
	 * Tests the {@link ConditionMethod#execute(ExecutionContext)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Execute extends ConditionMethodTest {
		private ExecutionContext context;

		@BeforeMethod
		public void init() {
			context = new ExecutionContext(definition, dummy, input, variables);
		}

		@Test
		public void shouldProduceNull() throws Exception {
			// prepare Mocks
			when(dummy.successCondiction()).thenReturn(true);
			ConditionMethod condition = new ConditionMethod("TestSuccessCondition", "success", RuleDummy.successConditionMethod());

			ConditionFailure failure = condition.execute(context);

			assertThat(failure, nullValue());
		}

		@Test
		public void shouldProduceConditionFailure() throws Exception {
			// prepare Mocks
			when(dummy.successCondiction()).thenReturn(true);
			ConditionMethod condition = new ConditionMethod("TestFailCondition", "failure", RuleDummy.failConditionMethod());

			ConditionFailure failure = condition.execute(context);

			assertThat(failure.getConditionName(), equalTo(condition.getName()));
			assertThat(failure.getHint(), equalTo(condition.getHint()));
		}

		@Test(expectedExceptions = { RuleExecutionException.class })
		public void shouldthrowException() throws Exception {
			// prepare Mocks
			when(dummy.invalidCondition()).thenThrow(new RuntimeException());
			ConditionMethod condition = new ConditionMethod("TestInvalidCondition", "invalid", RuleDummy.invalidConditionMethod());

			ConditionFailure failure = condition.execute(context);
			System.out.println(failure);
		}
	}
}
