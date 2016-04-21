package rocks.inspectit.server.diagnosis.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
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

	public static class Diagnose extends DiagnosisServiceTest {

		@InjectMocks
		DiagnosisService diagnosisService = new DiagnosisService(new ArrayList<String>() {
			/**
			 * Default Serial ID.
			 */
			private static final long serialVersionUID = 1L;

			{
				// the testrules should be replaced with the correct rules
				add("rocks.inspectit.server.diagnosis.engine.testrules");
			}
		}, 2, 50L);

		InvocationSequenceData invocationSequenceRoot;
		private final double BASELINE = 1000;
		private static final long METHOD_IDENT = 108L;
		private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());
		boolean canBeDiagnosed = false;
		int nbrResults = 0;

		@Mock
		Logger log;

		@Mock
		private IDiagnosisEngine<InvocationSequenceData> engine;

		Map<String, Double> sessionVariables = new HashMap<String, Double>();

		@Test
		public void init() {
			invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(5000d);
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstChildSequence.setDuration(200d);
			firstChildSequence.setId(2);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(DEF_DATE, 20, 20, METHOD_IDENT);
			secondChildSequence.setDuration(4000d);
			secondChildSequence.setId(3);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(DEF_DATE, 30, 30, METHOD_IDENT);
			thirdChildSequence.setDuration(500d);
			thirdChildSequence.setId(4);
			invocationSequenceRoot.getNestedSequences().add(firstChildSequence);
			invocationSequenceRoot.getNestedSequences().add(secondChildSequence);
			invocationSequenceRoot.getNestedSequences().add(thirdChildSequence);

			canBeDiagnosed = diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
			assertEquals(canBeDiagnosed, true);

			Pair<InvocationSequenceData, Double> pair = new Pair<InvocationSequenceData, Double>(invocationSequenceRoot, BASELINE);
			Set<Pair<InvocationSequenceData, Double>> invocationBaselinePair = new HashSet<Pair<InvocationSequenceData, Double>>();
			invocationBaselinePair.add(pair);
			nbrResults = diagnosisService.diagnose(invocationBaselinePair);
			assertEquals(nbrResults, 1);
		}

	}

	public static class Init extends DiagnosisServiceTest {

		@InjectMocks
		DiagnosisService diagnosisService = new DiagnosisService(new ArrayList<String>() {
			/**
			 * Default Serial ID.
			 */
			private static final long serialVersionUID = 1L;

			{
				// the testrules should be replaced with the correct rules
				add("rocks.inspectit.server.diagnosis.engine.testrules");
			}
		}, 2, 50L);

		@Mock
		Logger log;

		@Test
		public void initAndShutdown() {
			// currently no rules are defined. Therefore the initialization must be false.
			assertEquals(diagnosisService.init(), true);
			assertEquals(diagnosisService.isShutdown(), false);
			diagnosisService.shutdown(true);
			assertEquals(diagnosisService.isShutdown(), true);
		}

	}

	public static class InitFailure extends DiagnosisServiceTest {

		@InjectMocks
		DiagnosisService diagnosisService = new DiagnosisService(new ArrayList<String>() {
			/**
			 * Default Serial ID.
			 */
			private static final long serialVersionUID = 1L;

			{
				add("non.existing.package");
			}
		}, 2, 50L);

		@Mock
		Logger log;

		@Test
		public void init() {
			assertEquals(diagnosisService.init(), false);
		}

	}

	public static class StartStop extends DiagnosisServiceTest {

		@InjectMocks
		DiagnosisService diagnosisService = new DiagnosisService(new ArrayList<String>() {
			/**
			 * Default Serial ID.
			 */
			private static final long serialVersionUID = 1L;

			{
				// the testrules should be replaced with the correct rules
				add("rocks.inspectit.server.diagnosis.engine.testrules");
			}
		}, 2, 50L);

		InvocationSequenceData invocationSequenceRoot;
		private final double BASELINE = 1000;
		private static final long METHOD_IDENT = 108L;
		private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());
		boolean canBeDiagnosed = false;
		int nbrResults = 0;

		@Mock
		Logger log;

		@Mock
		private IDiagnosisEngine<InvocationSequenceData> engine;

		@Test
		public void startStopDiagnosisService() {

			invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(5000d);
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstChildSequence.setDuration(200d);
			firstChildSequence.setId(2);
			InvocationSequenceData secondChildSequence = new InvocationSequenceData(DEF_DATE, 20, 20, METHOD_IDENT);
			secondChildSequence.setDuration(4000d);
			secondChildSequence.setId(3);
			InvocationSequenceData thirdChildSequence = new InvocationSequenceData(DEF_DATE, 30, 30, METHOD_IDENT);
			thirdChildSequence.setDuration(500d);
			thirdChildSequence.setId(4);
			invocationSequenceRoot.getNestedSequences().add(firstChildSequence);
			invocationSequenceRoot.getNestedSequences().add(secondChildSequence);
			invocationSequenceRoot.getNestedSequences().add(thirdChildSequence);

			Map<String, Double> sessionVariables = new HashMap<String, Double>();
			sessionVariables.put(RuleConstants.VAR_BASELINE, BASELINE);

			try {
				assertEquals(diagnosisService.init(), true);
				verify(engine, times(0)).analyze(invocationSequenceRoot, sessionVariables);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				Thread.sleep(1000);
				verify(engine, times(1)).analyze(invocationSequenceRoot, sessionVariables);
				diagnosisService.setDiagnosisServiceStopped(true);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				Thread.sleep(1000);
				verify(engine, times(2)).analyze(invocationSequenceRoot, sessionVariables);
				diagnosisService.setDiagnosisServiceStopped(false);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				Thread.sleep(1000);
				verify(engine, times(6)).analyze(invocationSequenceRoot, sessionVariables);
				diagnosisService.shutdown(true);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				diagnosisService.diagnose(invocationSequenceRoot, BASELINE);
				Thread.sleep(1000);
				verify(engine, times(7)).analyze(invocationSequenceRoot, sessionVariables);
			} catch (DiagnosisEngineException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

}