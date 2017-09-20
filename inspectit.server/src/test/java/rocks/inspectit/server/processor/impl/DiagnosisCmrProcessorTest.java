package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.sql.Timestamp;
import java.util.Date;

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

	private static final long METHOD_IDENT = 108L;
	private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());

	public static class Process extends DiagnosisCmrProcessorTest {

		@Mock
		EntityManager entityManager;

		@Mock
		DiagnosisService diagnosisService;

		@Test
		public void processData() {
			cmrProcessor.setBaseline(1000);
			cmrProcessor.setDiagnosisEnabled(true);
			cmrProcessor.setInfluxActive(true);
			InvocationSequenceData invocationSequenceRoot = new InvocationSequenceData();
			invocationSequenceRoot.setId(1);
			invocationSequenceRoot.setDuration(5000d);
			InvocationSequenceData firstChildSequence = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstChildSequence.setDuration(200d);
			firstChildSequence.setId(2);

			invocationSequenceRoot.getNestedSequences().add(firstChildSequence);

			cmrProcessor.processData(invocationSequenceRoot, entityManager);

			verifyZeroInteractions(entityManager);
			verify(diagnosisService).diagnose(invocationSequenceRoot, cmrProcessor.getBaseline());
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

			assertThat("Data cannot be processed if influx is not active", canBeProcessed, is(false));
		}

		@Test
		public void dataIsNotProcessedWhenDiagnosisIsNotEnabled() {
			DefaultData invocationSequenceRoot = mock(DefaultData.class);
			cmrProcessor.setBaseline(1000);
			cmrProcessor.setDiagnosisEnabled(false);
			cmrProcessor.setInfluxActive(true);

			boolean canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);

			assertThat("Data cannot be processed if influx is not active", canBeProcessed, is(false));
		}

		@Test
		public void dataIsNotProcessedWhenIsNotAnInvocationSequenceDataInstance() {
			DefaultData invocationSequenceRoot = mock(DefaultData.class);
			cmrProcessor.setDiagnosisEnabled(true);
			cmrProcessor.setInfluxActive(true);

			boolean canBeProcessed = cmrProcessor.canBeProcessed(invocationSequenceRoot);

			assertThat("Data cannot be processed if influx is not active", canBeProcessed, is(false));
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

			assertThat("Data cannot be processed if influx is not active", canBeProcessed, is(false));
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

			assertThat("Data cannot be processed if influx is not active", canBeProcessed, is(true));
		}
	}
}
