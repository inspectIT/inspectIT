package rocks.inspectit.server.diagnosis.engine.rule.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDummy;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.SessionVariableInjection;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleExecutionException;
import rocks.inspectit.server.diagnosis.engine.session.SessionVariables;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
public class SessionVariableInjectionTest extends TestBase {

	@Mock
	RuleInput input;

	@Mock
	RuleDefinition definition;

	@Mock
	SessionVariables variables;

	@Mock(name = "instance")
	RuleDummy ruleImpl;

	/**
	 * Tests {@link SessionVariableInjection#execute(ExecutionContext)} method.
	 */
	public static class Execute extends SessionVariableInjectionTest {
		private ExecutionContext context;

		@BeforeMethod
		public void init() {
			context = new ExecutionContext(definition, ruleImpl, input, variables);
		}

		@Test
		public void shouldInjectValue() {
			// prepare
			doReturn(true).when(variables).containsKey("var");
			when(variables.get("var")).thenReturn(123);
			// execute
			new SessionVariableInjection("var", false, RuleDummy.sessionVariableIntField()).execute(context);
			// verify
			assertThat(ruleImpl.sessionIntVariable, is(123));
		}

		@Test
		public void shouldRespectOptional() {
			// execute
			new SessionVariableInjection("var", true, RuleDummy.sessionVariableIntField()).execute(context);
			// verify
			assertThat(ruleImpl.sessionIntVariable, is(nullValue()));
		}

		@Test(expectedExceptions = RuleExecutionException.class)
		public void shouldFailDueToMissingVariable() {
			// execute
			new SessionVariableInjection("var", false, RuleDummy.sessionVariableIntField()).execute(context);
		}
	}
}
