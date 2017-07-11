package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.persistence.EntityManager;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.service.DiagnosisService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;


/**
 * @author Christian Voegele, Isabel Vico Peinado
 *
 */
@SuppressWarnings("PMD")
public class DiagnosisCmrProcessorTest extends TestBase {

	@InjectMocks
	DiagnosisCmrProcessor cmrProcessor;

	public static class ProcessData extends DiagnosisCmrProcessorTest {

		@Mock
		EntityManager entityManager;

		@Mock
		DiagnosisService diagnosisService;

		@Test
		public void processData() {
			cmrProcessor.setBaseline(1000);
			InvocationSequenceData invocationSequenceRoot = new InvocationSequenceData();

			cmrProcessor.processData(invocationSequenceRoot, entityManager);

			verifyZeroInteractions(entityManager);
			verify(diagnosisService).diagnose(invocationSequenceRoot, 1000);
		}
	}

	public static class CanBeProcessed extends DiagnosisCmrProcessorTest {

		@Mock
		EntityManager entityManager;

		@Mock
		DiagnosisService diagnosisService;

		@Test
		public void dataIsNotProcessedWhenInfluxIsNotActive() {
			DefaultData invocationSequenceRoot = mock(DefaultData.class);
			cmrProcessor.setBaseline(1000);
			cmrProcessor.setDiagnosisEnabled(true);
			cmrProcessor.setInfluxActive(false);

			boolean canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);

			assertThat("Data cannot be processed if influx is not active.", canBeProcessed, is(false));
		}

		@Test
		public void dataIsNotProcessedWhenDiagnosisIsNotEnabled() {
			DefaultData invocationSequenceRoot = mock(DefaultData.class);
			cmrProcessor.setBaseline(1000);
			cmrProcessor.setDiagnosisEnabled(false);
			cmrProcessor.setInfluxActive(true);

			boolean canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);

			assertThat("Data cannot be processed if diagnosis is not enabled.", canBeProcessed, is(false));
		}

		@Test
		public void dataIsNotProcessedWhenIsNotAnInvocationSequenceDataInstance() {
			DefaultData invocationSequenceRoot = mock(DefaultData.class);
			cmrProcessor.setDiagnosisEnabled(true);
			cmrProcessor.setInfluxActive(true);

			boolean canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);

			assertThat("Data cannot be processed if ther is not a InvocationSequenceData.", canBeProcessed, is(false));
		}

		@Test
		public void dataIsNotProcessedWhenTheTimeLowerThanTheBaseline() {
			InvocationSequenceData invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(500d);
			cmrProcessor.setBaseline(1000);
			cmrProcessor.setDiagnosisEnabled(true);
			cmrProcessor.setInfluxActive(true);

			boolean canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);

			assertThat("Data cannot be processed when time is lower than the set in the baseline.", canBeProcessed, is(false));
		}

		@Test
		public void dataIsProcessedWhenAllTheConditionsAreFulfilled() {
			InvocationSequenceData invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(1500d);
			cmrProcessor.setBaseline(1000);
			cmrProcessor.setDiagnosisEnabled(true);
			cmrProcessor.setInfluxActive(true);

			boolean canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);

			assertThat("Data must be processed when all the conditions are fulfilled. ", canBeProcessed, is(true));
		}
	}
}
