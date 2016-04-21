package rocks.inspectit.server.diagnosis.engine.rule.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.ExecutionContext;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDefinition;
import rocks.inspectit.server.diagnosis.engine.rule.RuleDummy;
import rocks.inspectit.server.diagnosis.engine.rule.RuleInput;
import rocks.inspectit.server.diagnosis.engine.rule.TagInjection;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
public class TagInjectionTest extends TestBase {

	@Mock
	RuleInput input;

	@Mock
	RuleDefinition definition;

	@Mock
	RuleDummy ruleImpl;

	/**
	 * Tests {@link TagInjection#execute(ExecutionContext)} method.
	 */
	public static class Execute extends TagInjectionTest {
		private ExecutionContext context;

		@BeforeMethod
		public void init() {
			context = new ExecutionContext(definition, ruleImpl, input);
		}

		@Test
		public void shouldInjectValue() {
			// prepare
			when(input.getUnraveled()).thenReturn(Tags.tags("T1", null, "InjectedValue"));
			// execute
			new TagInjection("T1", RuleDummy.tagStringValueField()).execute(context);
			// verify
			assertThat(ruleImpl.tagStringValueField, is("InjectedValue"));
		}

		@Test
		public void shouldInjectTag() {
			// prepare
			when(input.getUnraveled()).thenReturn(Tags.tags("T1", null, "InjectedValue"));
			// execute
			new TagInjection("T1", RuleDummy.tagAsTagField(), TagValue.InjectionStrategy.BY_TAG).execute(context);
			// verify
			assertThat(ruleImpl.tagAsTagField, is(Tags.tag("T1", "InjectedValue")));
		}
	}
}
