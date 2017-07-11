package rocks.inspectit.server.diagnosis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineException;
import rocks.inspectit.server.diagnosis.engine.IDiagnosisEngine;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;

/**
 * @author Christian Voegele
 *
 */
@SuppressWarnings("PMD")
public class DiagnosisServiceTest extends TestBase {

	DiagnosisService diagnosisService;

	@Mock
	Consumer<ProblemOccurrence> problemOccurrenceConsumer;

	public static class Diagnose extends DiagnosisServiceTest {
		@Test
		public void canBeDiagnosed() {
			double baseline = 1000;
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			boolean canBeDiagnosed = diagnosisService.diagnose(invocationSequenceData, baseline);

			assertThat(canBeDiagnosed, equalTo(true));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void cannotBeDiagnosedWithInvocationSequenceDataNull() {
			double baseline = 1000;
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			diagnosisService.diagnose(null, baseline);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void cannotBeDiagnosedWithBaselineNegative() {
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			diagnosisService.diagnose(invocationSequenceData, -1);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void cannotBeDiagnosedWithNullAndNegativeBaseline() {
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			diagnosisService.diagnose(null, -1);
		}
	}

	public static class Init extends DiagnosisServiceTest {
		@Test
		public void initSuccessfully() {
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);

			assertThat(diagnosisService.init(), is(true));
		}

		@Test
		public void initFailureWithoutRules() {
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.shared.all");
				}
			}, 2, 50L, 2);
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			assertThat(diagnosisService.init(), is(false));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void initFailureWithNegativeNumberOfSessionWorkers() {
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, -2, 50L, 2);
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			assertThat(diagnosisService.init(), is(false));
		}

		@Test
		public void initFailureWithRulesInPackageWithMultipleActionTag() {
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.engine.testrules");
				}
			}, 2, 50L, 2);
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			assertThat(diagnosisService.init(), is(false));
		}

	}

	public static class Run extends DiagnosisServiceTest {
		@Mock
		IDiagnosisEngine<InvocationSequenceData> engine;

		@Mock
		ExecutorService diagnosisServiceExecutor;

		@BeforeMethod
		private void init() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 3);

			Field fieldEngine;
			fieldEngine = DiagnosisService.class.getDeclaredField("engine");
			fieldEngine.setAccessible(true);
			fieldEngine.set(diagnosisService, engine);

			Field fieldDiagnosisServiceExecutor = DiagnosisService.class.getDeclaredField("diagnosisServiceExecutor");
			fieldDiagnosisServiceExecutor.setAccessible(true);
			fieldDiagnosisServiceExecutor.set(diagnosisService, diagnosisServiceExecutor);
		}

		@Test
		public void runDiagnosis() throws DiagnosisEngineException {
			double baseline = 1000;
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);
			verify(engine, times(0)).analyze(invocationSequenceData, Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, baseline));
			verify(diagnosisServiceExecutor, times(0)).execute(diagnosisService);
			diagnosisService.diagnose(invocationSequenceData, baseline);
			diagnosisService.diagnose(invocationSequenceData, baseline);
			diagnosisService.diagnose(invocationSequenceData, baseline);

			diagnosisService.run();
			diagnosisService.run();
			diagnosisService.run();

			verify(engine, times(3)).analyze(invocationSequenceData, Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, baseline));
			verify(diagnosisServiceExecutor, times(3)).execute(diagnosisService);
		}

		@Test
		public void stopDiagnosis() throws DiagnosisEngineException {
			double baseline = 1000;
			InvocationSequenceData invocationSequenceData = new InvocationSequenceData();
			when(diagnosisServiceExecutor.isShutdown()).thenReturn(true);
			diagnosisService.diagnose(invocationSequenceData, baseline);

			diagnosisService.run();

			verify(engine, times(1)).analyze(invocationSequenceData, Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, baseline));
			verify(diagnosisServiceExecutor, times(0)).execute(diagnosisService);
		}
	}

	public static class Shutdown extends DiagnosisServiceTest {
		@Test
		public void initAndShutDown() {
			diagnosisService = new DiagnosisService(problemOccurrenceConsumer, new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			diagnosisService.init();

			diagnosisService.shutdown(true);

			assertThat(diagnosisService.isShutdown(), is(true));
		}
	}
}