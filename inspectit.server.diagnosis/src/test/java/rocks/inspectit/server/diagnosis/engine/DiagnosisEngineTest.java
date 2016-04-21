package rocks.inspectit.server.diagnosis.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.store.DefaultRuleOutputStorage;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResult;
import rocks.inspectit.server.diagnosis.engine.session.DefaultSessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.ISessionCallback;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleA;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleB;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleC;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleD;
import rocks.inspectit.server.diagnosis.engine.testrules.RuleE;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Claudio Waldvogel
 */
@SuppressWarnings("all")
public class DiagnosisEngineTest extends TestBase {

	public class Analyze extends DiagnosisEngineTest {
		@Test
		public void testEngine() throws DiagnosisEngineException {

			final List<DefaultSessionResult<String>> results = new ArrayList<>();

			final List<Throwable> exceptions = new ArrayList<>();
			DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> configuration = new DiagnosisEngineConfiguration<String, DefaultSessionResult<String>>().setNumSessionWorkers(1)
					.addRuleClasses(RuleA.class, RuleB.class, RuleC.class, RuleE.class).setStorageClass(DefaultRuleOutputStorage.class).setResultCollector(new DefaultSessionResultCollector<String>())
					.addSessionCallback(new ISessionCallback<DefaultSessionResult<String>>() {
						@Override
						public void onSuccess(DefaultSessionResult<String> result) {
							results.add(result);
						}

						@Override
						public void onFailure(Throwable t) {
							exceptions.add(t);
						}

					});
			configuration.setShutdownTimeout(6000);
			DiagnosisEngine<String, DefaultSessionResult<String>> diagnosisEngine = new DiagnosisEngine<>(configuration);

			String input = "Trace";
			diagnosisEngine.analyze(input);

			diagnosisEngine.shutdown(true);

			assertThat(exceptions, empty());
			assertThat(results.get(0).getEndTags().get("C"), hasSize(1));
			assertThat(results.get(0).getEndTags().get("C").iterator().next().getValue(), equalTo((Object) ((input + input).length() + 2)));
			assertThat(results.get(0).getEndTags().get("E"), hasSize(1));
			assertThat(results.get(0).getEndTags().get("E").iterator().next().getValue(), equalTo((Object) (input + "E")));

		}

		@Test
		public void testwithError() throws DiagnosisEngineException {

			final List<DefaultSessionResult<String>> results = new ArrayList<>();

			final List<Throwable> exceptions = new ArrayList<>();

			DiagnosisEngineConfiguration<String, DefaultSessionResult<String>> configuration = new DiagnosisEngineConfiguration<String, DefaultSessionResult<String>>().setNumSessionWorkers(1)
					.addRuleClasses(RuleA.class, RuleB.class, RuleC.class, RuleE.class, RuleD.class).setStorageClass(DefaultRuleOutputStorage.class)
					.setResultCollector(new DefaultSessionResultCollector<String>()).addSessionCallback(new ISessionCallback<DefaultSessionResult<String>>() {
						@Override
						public void onSuccess(DefaultSessionResult<String> result) {
							results.add(result);
						}

						@Override
						public void onFailure(Throwable t) {
							exceptions.add(t);
						}

					});
			configuration.setShutdownTimeout(6000);
			DiagnosisEngine<String, DefaultSessionResult<String>> diagnosisEngine = new DiagnosisEngine<>(configuration);

			String input = "Trace";
			diagnosisEngine.analyze(input);

			diagnosisEngine.shutdown(true);

			assertThat(exceptions, hasSize(1));
		}
	}
}