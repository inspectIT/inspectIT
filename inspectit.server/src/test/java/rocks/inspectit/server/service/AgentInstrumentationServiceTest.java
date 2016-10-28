package rocks.inspectit.server.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.server.messaging.AgentInstrumentationMessageGate;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AgentInstrumentationService} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AgentInstrumentationServiceTest extends TestBase {

	@InjectMocks
	AgentInstrumentationService instrumentationService;

	@Mock
	Logger log;

	@Mock
	AgentInstrumentationMessageGate messageGate;

	/**
	 * Tests the {@link AgentInstrumentationService#updateInstrumentation(java.util.Collection)}
	 * method.
	 */
	public static class UpdateInstrumentation extends AgentInstrumentationServiceTest {

		@Test
		public void successful() {
			instrumentationService.updateInstrumentation(Arrays.asList(10L, 20L));

			ArgumentCaptor<Long> platformIdentCaptor = ArgumentCaptor.forClass(Long.class);
			verify(messageGate, times(2)).flush(platformIdentCaptor.capture());
			verifyNoMoreInteractions(messageGate);
			assertThat(platformIdentCaptor.getAllValues(), contains(10L, 20L));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullParameterIdentCollection() {
			try {
				instrumentationService.updateInstrumentation(null);
			} finally {
				verifyZeroInteractions(messageGate);
			}
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		@SuppressWarnings("unchecked")
		public void emptyParameterIdentCollection() {
			try {
				instrumentationService.updateInstrumentation(Collections.EMPTY_LIST);
			} finally {
				verifyZeroInteractions(messageGate);
			}
		}
	}
}
