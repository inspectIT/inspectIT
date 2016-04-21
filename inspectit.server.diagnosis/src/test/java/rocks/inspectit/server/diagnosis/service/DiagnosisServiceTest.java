package rocks.inspectit.server.diagnosis.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.DiagnosisEngineException;
import rocks.inspectit.server.diagnosis.engine.IDiagnosisEngine;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Christian Voegele
 *
 */
@SuppressWarnings("PMD")
public class DiagnosisServiceTest extends TestBase {

	InvocationSequenceData invocationSequenceData;
	double BASELINE = 1000;
	long METHOD_IDENT = 108L;
	Timestamp DEF_DATE = new Timestamp(new Date().getTime());
	DiagnosisService diagnosisService;

	public static class Diagnose extends DiagnosisServiceTest {
		@Test
		public void canBeDiagnosed() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			boolean canBeDiagnosed = diagnosisService.diagnose(invocationSequenceData, BASELINE);

			assertThat(canBeDiagnosed, equalTo(true));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void cannotBeDiagnosedWithInvocationSequenceDataNull() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			diagnosisService.diagnose(null, BASELINE);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void cannotBeDiagnosedWithBaselineNegative() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			diagnosisService.diagnose(invocationSequenceData, -1);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void cannotBeDiagnosedWithNullAndNegativeBaseline() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
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
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);

			assertEquals(diagnosisService.init(), true);
		}

		@Test
		public void initFailureWithoutRules() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.shared.all");
				}
			}, 2, 50L, 2);
			invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			assertEquals(diagnosisService.init(), false);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void initFailureWithNegativeNumberOfSessionWorkers() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, -2, 50L, 2);
			invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			assertEquals(diagnosisService.init(), false);
		}

		@Test
		public void initFailureWithRulesInPackageWithMultipleActionTag() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.engine.testrules");
				}
			}, 2, 50L, 2);
			invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);

			assertEquals(diagnosisService.init(), false);
		}

	}

	public static class Run extends DiagnosisServiceTest {
		@Mock
		IDiagnosisEngine<InvocationSequenceData> engine;

		@Mock
		ExecutorService diagnosisServiceExecutor;

		@Test
		public void runDiagnosis() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 3);
			invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);
			try {
				Field fieldEngine = DiagnosisService.class.getDeclaredField("engine");
				fieldEngine.setAccessible(true);
				fieldEngine.set(diagnosisService, engine);
				Field fieldDiagnosisServiceExecutor = DiagnosisService.class.getDeclaredField("diagnosisServiceExecutor");
				fieldDiagnosisServiceExecutor.setAccessible(true);
				fieldDiagnosisServiceExecutor.set(diagnosisService, diagnosisServiceExecutor);

				verify(engine, times(0)).analyze(invocationSequenceData, Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, BASELINE));
				verify(diagnosisServiceExecutor, times(0)).execute(diagnosisService);

				diagnosisService.diagnose(invocationSequenceData, BASELINE);
				diagnosisService.diagnose(invocationSequenceData, BASELINE);
				diagnosisService.diagnose(invocationSequenceData, BASELINE);

				diagnosisService.run();
				diagnosisService.run();
				diagnosisService.run();

				verify(engine, times(3)).analyze(invocationSequenceData, Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, BASELINE));
				verify(diagnosisServiceExecutor, times(3)).execute(diagnosisService);
			} catch (DiagnosisEngineException | NoSuchFieldException | IllegalArgumentException | SecurityException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		@Test
		public void stopDiagnosis() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 3);
			invocationSequenceData = new InvocationSequenceData();
			invocationSequenceData.setDuration(5000d);
			try {
				Field fieldEngine = DiagnosisService.class.getDeclaredField("engine");
				fieldEngine.setAccessible(true);
				fieldEngine.set(diagnosisService, engine);

				Field fieldDiagnosisServiceExecutor = DiagnosisService.class.getDeclaredField("diagnosisServiceExecutor");
				fieldDiagnosisServiceExecutor.setAccessible(true);
				fieldDiagnosisServiceExecutor.set(diagnosisService, diagnosisServiceExecutor);

				when(diagnosisServiceExecutor.isShutdown()).thenReturn(true);

				diagnosisService.diagnose(invocationSequenceData, BASELINE);

				diagnosisService.run();

				verify(engine, times(1)).analyze(invocationSequenceData, Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, BASELINE));
				verify(diagnosisServiceExecutor, times(0)).execute(diagnosisService);
			} catch (DiagnosisEngineException | NoSuchFieldException | IllegalArgumentException | SecurityException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static class Shutdown extends DiagnosisServiceTest {
		@Test
		public void initAndShutDown() {
			diagnosisService = new DiagnosisService(new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add("rocks.inspectit.server.diagnosis.service.rules.testrules");
				}
			}, 2, 50L, 2);
			diagnosisService.init();

			diagnosisService.shutdown(true);

			assertEquals(diagnosisService.isShutdown(), true);
		}

	}

}