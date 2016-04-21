package rocks.inspectit.server.diagnosis.engine.session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleA;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleB;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleC;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link SessionContext} class.
 *
 * @author Alexander Wert
 *
 */
public class SessionContextTest extends TestBase {

	SessionContext<String> sessionContext;

	@Mock
	DefaultRuleOutputStorage storage;

	Set<RuleDefinition> rules;

	@BeforeMethod
	public void init() throws RuleDefinitionException {
		rules = Rules.define(RuleA.class, RuleB.class, RuleC.class);
		sessionContext = new SessionContext<>(rules, storage);
	}

	/**
	 * Tests the {@link SessionContext#activate(Object, java.util.Map)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Activate extends SessionContextTest {

		@Test
		public void activateOnce() {
			String input = "input";
			Map<String, String> variables = new HashMap<>();
			variables.put("Key", "Value");

			assertThat(sessionContext.getRuleSet(), empty());

			sessionContext.activate(input, variables);

			assertThat(sessionContext.getExecutions().keySet(), empty());
			assertThat(sessionContext.getRuleSet(), hasSize(3));
			assertThat(sessionContext.getRuleSet(), containsInAnyOrder(rules.toArray()));
			assertThat(sessionContext.getInput(), equalTo(input));
			assertThat(sessionContext.getSessionVariables().get("Key").toString(), equalTo("Value"));
		}

		@Test
		public void activateTwice() {
			String input = "input";
			Map<String, String> variables = new HashMap<>();
			variables.put("Key", "Value");

			sessionContext.activate(input, variables);
			sessionContext.activate(input, variables);

			assertThat(sessionContext.getExecutions().keySet(), empty());
			assertThat(sessionContext.getRuleSet(), hasSize(3));
			assertThat(sessionContext.getRuleSet(), containsInAnyOrder(rules.toArray()));
			assertThat(sessionContext.getInput(), equalTo(input));
			assertThat(sessionContext.getSessionVariables().get("Key").toString(), equalTo("Value"));
		}
	}

	/**
	 * Tests the {@link SessionContext#passivate()} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class Passivate extends SessionContextTest {

		@Test
		public void passivateOnce() {
			String input = "input";
			Map<String, String> variables = new HashMap<>();
			variables.put("Key", "Value");
			sessionContext.activate(input, variables);

			sessionContext.passivate();

			assertThat(sessionContext.getExecutions().keySet(), empty());
			assertThat(sessionContext.getRuleSet(), empty());
			assertThat(sessionContext.getInput(), equalTo(null));
			assertThat(sessionContext.getSessionVariables().keySet(), empty());
		}

		@Test
		public void passivateTwice() {
			String input = "input";
			Map<String, String> variables = new HashMap<>();
			variables.put("Key", "Value");
			sessionContext.activate(input, variables);

			sessionContext.passivate();
			sessionContext.passivate();

			assertThat(sessionContext.getExecutions().keySet(), empty());
			assertThat(sessionContext.getRuleSet(), empty());
			assertThat(sessionContext.getInput(), equalTo(null));
			assertThat(sessionContext.getSessionVariables().keySet(), empty());
		}
	}

	/**
	 * Tests the
	 * {@link SessionContext#addExecution(RuleDefinition, rocks.inspectit.server.diagnosis.engine.rule.RuleInput)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class AddExecution extends SessionContextTest {

		@Test
		public void add() {
			RuleInput input = new RuleInput(Tags.tag("A", "input"));

			sessionContext.addExecution(rules.iterator().next(), input);

			assertThat(sessionContext.getExecutions().get(rules.iterator().next()), containsInAnyOrder(input));

		}
	}
}
