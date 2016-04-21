package rocks.inspectit.server.diagnosis.engine;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Condition;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
public class DiagnosisEngineTest {

	@Test
	public void testEngine() throws DiagnosisEngineException {

		final List<DefaultSessionResult<String>> results = new ArrayList<>();

		DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> configuration = new DiagnosisEngineConfiguration<String, DefaultSessionResult<String>>().setNumSessionWorkers(1)
				.addRuleClasses(R1.class, R2.class, R3.class, R4.class, R5.class).setStorageClass(DefaultRuleOutputStorage.class).setResultCollector(new DefaultSessionResultCollector<String>())
				.addSessionCallback(new ISessionCallback<DefaultSessionResult<String>>() {
					@Override
					public void onSuccess(DefaultSessionResult<String> result) {
						results.add(result);
					}

					@Override
					public void onFailure(Throwable t) {
					}

				});
		configuration.setShutdownTimeout(6000);
		DiagnosisEngine<String, DefaultSessionResult<String>> diagnosisEngine = new DiagnosisEngine<>(configuration);

		diagnosisEngine.analyze("Trace");

		try {
			diagnosisEngine.shutdown(true);
			Assert.assertEquals(results.size(), 1);
			Assert.assertEquals(results.get(0).getEndTags().get("Tag2").size(), 2);
			Assert.assertEquals(results.get(0).getEndTags().get("Tag3").size(), 1);
			Assert.assertEquals(results.get(0).getEndTags().get("Tag5").size(), 3);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Rule
	public static class R1 {

		@TagValue(type = Tags.ROOT_TAG, injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = "Tag1")
		public String action() {
			return input + "Enhanced";
		}

	}

	@Rule
	public static class R2 {

		@TagValue(type = "Tag1", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Action(resultTag = "Tag2", resultQuantity = Action.Quantity.MULTIPLE)
		public String[] action() {
			return new String[] { input + "AgainEnhanced", input + "AgainEnhanced1", input + "AgainEnhanced2", input + "AgainEnhanced12" };
		}

	}

	@Rule(name = "ConditionalRule1")
	public static class R3 {

		@TagValue(type = "Tag2", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Condition(name = "containsOne", hint = "Also no problem")
		public boolean containsOne() {
			return input.contains("1");
		}

		@Condition(name = "containsTwo", hint = "No problem")
		public boolean containsTwo() {
			return input.contains("2");
		}

		@Action(resultTag = "Tag3")
		public String action() {
			return "EnhancedFor12";
		}
	}

	@Rule(name = "ConditionalRule2")
	public static class R4 {

		@TagValue(type = "Tag2", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private String input;

		@Condition(name = "containsOne", hint = "Also no problem")
		public boolean containsNoNumbers() {
			return !input.contains("1") && !input.contains("2");
		}

		@Action(resultTag = "Tag4", resultQuantity = Action.Quantity.MULTIPLE)
		public int[] action() {
			return new int[] { input.length(), input.length() + 1, input.length() + 2 };
		}
	}

	@Rule(name = "intRule")
	public static class R5 {

		@TagValue(type = "Tag4", injectionStrategy = TagValue.InjectionStrategy.BY_VALUE)
		private int input;

		@Action(resultTag = "Tag5")
		public int action() {
			return input * 2;
		}
	}
}