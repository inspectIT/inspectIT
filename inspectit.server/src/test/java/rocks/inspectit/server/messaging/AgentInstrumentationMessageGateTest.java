package rocks.inspectit.server.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import rocks.inspectit.server.ci.event.ClassInstrumentationChangedEvent;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.InstrumentationStatus;
import rocks.inspectit.shared.all.communication.message.AbstractAgentMessage;
import rocks.inspectit.shared.all.communication.message.UpdatedInstrumentationMessage;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AgentInstrumentationMessageGate} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class AgentInstrumentationMessageGateTest extends TestBase {

	@InjectMocks
	AgentInstrumentationMessageGate messageGate;

	@Mock
	Logger log;

	@Mock
	AgentMessageProvider messageProvider;

	@Mock
	AgentStatusDataProvider agentStatusDataProvider;

	@SuppressWarnings("unchecked")
	protected Map<Long, Map<String, InstrumentationDefinition>> getDefinitionBuffer() throws Exception {
		Field field = AgentInstrumentationMessageGate.class.getDeclaredField("definitionBuffer");
		field.setAccessible(true);
		return (Map<Long, Map<String, InstrumentationDefinition>>) field.get(messageGate);
	}

	/**
	 * Tests the
	 * {@link AgentInstrumentationMessageGate#onApplicationEvent(ClassInstrumentationChangedEvent)}
	 * method.
	 */
	public static class OnApplicationEvent extends AgentInstrumentationMessageGateTest {

		@Mock
		InstrumentationDefinition definitionOne;

		@Mock
		InstrumentationDefinition definitionTwo;

		@Mock
		InstrumentationDefinition definitionThree;

		@Mock
		AgentStatusData statusData;

		@Test
		public void bufferInstrumentationDefinitions() throws Exception {
			when(definitionOne.getClassName()).thenReturn("class.one");
			when(definitionTwo.getClassName()).thenReturn("class.two");
			when(definitionThree.getClassName()).thenReturn("class.three");
			ClassInstrumentationChangedEvent eventOne = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definitionOne, definitionTwo));
			ClassInstrumentationChangedEvent eventTwo = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definitionThree));
			when(agentStatusDataProvider.getAgentStatusDataMap()).thenReturn(ImmutableMap.of(10L, statusData));
			when(statusData.getInstrumentationStatus()).thenReturn(InstrumentationStatus.UP_TO_DATE, InstrumentationStatus.PENDING);
			long currentTime = System.currentTimeMillis();

			messageGate.onApplicationEvent(eventOne);
			messageGate.onApplicationEvent(eventTwo);

			verify(definitionOne).getClassName();
			verify(definitionTwo).getClassName();
			verify(definitionThree).getClassName();
			verify(agentStatusDataProvider, times(2)).getAgentStatusDataMap();
			verify(statusData, times(2)).getInstrumentationStatus();
			verify(statusData).setInstrumentationStatus(InstrumentationStatus.PENDING);
			ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
			verify(statusData).setLastInstrumentationUpate(timeCaptor.capture());
			verifyNoMoreInteractions(definitionOne, definitionTwo, definitionThree, agentStatusDataProvider, statusData);
			verifyZeroInteractions(messageProvider);
			assertThat(timeCaptor.getValue(), greaterThanOrEqualTo(currentTime));
			assertThat(getDefinitionBuffer().entrySet(), hasSize(1));
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(3));
			assertThat(getDefinitionBuffer().get(10L), hasEntry("class.one", definitionOne));
			assertThat(getDefinitionBuffer().get(10L), hasEntry("class.two", definitionTwo));
			assertThat(getDefinitionBuffer().get(10L), hasEntry("class.three", definitionThree));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void replaceInstrumentationDefinition() throws Exception {
			when(definitionOne.getClassName()).thenReturn("class.one");
			when(definitionTwo.getClassName()).thenReturn("class.two");
			when(definitionThree.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent eventOne = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definitionOne, definitionTwo));
			ClassInstrumentationChangedEvent eventTwo = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definitionThree));
			when(agentStatusDataProvider.getAgentStatusDataMap()).thenReturn(Collections.EMPTY_MAP);
			when(statusData.getInstrumentationStatus()).thenReturn(InstrumentationStatus.UP_TO_DATE, InstrumentationStatus.PENDING);

			messageGate.onApplicationEvent(eventOne);
			messageGate.onApplicationEvent(eventTwo);

			assertThat(getDefinitionBuffer().entrySet(), hasSize(1));
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(2));
			assertThat(getDefinitionBuffer().get(10L), hasEntry("class.one", definitionThree));
			assertThat(getDefinitionBuffer().get(10L), hasEntry("class.two", definitionTwo));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void addInstrumentationDefinitionForDiffrentAgents() throws Exception {
			when(definitionOne.getClassName()).thenReturn("class.one");
			when(definitionTwo.getClassName()).thenReturn("class.two");
			when(definitionThree.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent eventOne = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definitionOne, definitionTwo));
			ClassInstrumentationChangedEvent eventTwo = new ClassInstrumentationChangedEvent(this, 20L, Arrays.asList(definitionThree));
			when(agentStatusDataProvider.getAgentStatusDataMap()).thenReturn(Collections.EMPTY_MAP);
			when(statusData.getInstrumentationStatus()).thenReturn(InstrumentationStatus.UP_TO_DATE, InstrumentationStatus.PENDING);

			messageGate.onApplicationEvent(eventOne);
			messageGate.onApplicationEvent(eventTwo);

			assertThat(getDefinitionBuffer().entrySet(), hasSize(2));
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(2));
			assertThat(getDefinitionBuffer().get(10L), hasEntry("class.one", definitionOne));
			assertThat(getDefinitionBuffer().get(10L), hasEntry("class.two", definitionTwo));
			assertThat(getDefinitionBuffer().get(20L).entrySet(), hasSize(1));
			assertThat(getDefinitionBuffer().get(20L), hasEntry("class.one", definitionThree));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void noAgentStatusData() throws Exception {
			when(definitionOne.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent eventOne = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definitionOne));
			when(agentStatusDataProvider.getAgentStatusDataMap()).thenReturn(Collections.EMPTY_MAP);

			messageGate.onApplicationEvent(eventOne);

			verify(agentStatusDataProvider).getAgentStatusDataMap();
			verifyNoMoreInteractions(agentStatusDataProvider);
			verifyZeroInteractions(statusData, messageProvider);
		}

		@Test
		public void nullEvent() throws Exception {
			messageGate.onApplicationEvent(null);

			verifyZeroInteractions(definitionOne, definitionTwo, definitionThree, agentStatusDataProvider, statusData, messageProvider);
		}
	}

	/**
	 * Tests the {@link AgentInstrumentationMessageGate#flush(PlatformIdent)} method.
	 */
	public static class Flush extends AgentInstrumentationMessageGateTest {

		@Mock
		InstrumentationDefinition definition;

		@Mock
		AgentStatusData statusData;

		@Test
		public void successful() throws Exception {
			when(definition.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent event = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definition));
			messageGate.onApplicationEvent(event);
			when(agentStatusDataProvider.getAgentStatusDataMap()).thenReturn(ImmutableMap.of(10L, statusData));
			when(statusData.getInstrumentationStatus()).thenReturn(InstrumentationStatus.PENDING);
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(1));

			messageGate.flush(10L);

			ArgumentCaptor<AbstractAgentMessage> messageCaptor = ArgumentCaptor.forClass(AbstractAgentMessage.class);
			verify(messageProvider).provideMessage(eq(10L), messageCaptor.capture());
			verify(statusData).setInstrumentationStatus(InstrumentationStatus.UP_TO_DATE);
			verifyNoMoreInteractions(statusData, messageProvider);
			assertThat(((UpdatedInstrumentationMessage) messageCaptor.getValue()).getUpdatedInstrumentationDefinitions(), contains(definition));
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(0));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void unknownPlatformId() throws Exception {
			when(definition.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent event = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definition));
			messageGate.onApplicationEvent(event);
			when(agentStatusDataProvider.getAgentStatusDataMap()).thenReturn(Collections.EMPTY_MAP);
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(1));

			messageGate.flush(20L);

			verifyZeroInteractions(messageProvider);
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(1));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void invalidPlatformId() {
			try {
				messageGate.flush(-10L);
			} finally {
				verifyZeroInteractions(messageProvider, agentStatusDataProvider);
			}
		}
	}

	/**
	 * Tests the {@link AgentInstrumentationMessageGate#clear(PlatformIdent)} method.
	 */
	public static class Clear extends AgentInstrumentationMessageGateTest {

		@Mock
		InstrumentationDefinition definition;

		@Test
		public void successful() throws Exception {
			when(definition.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent event = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definition));
			messageGate.onApplicationEvent(event);

			messageGate.clear(10L);

			verifyZeroInteractions(messageProvider);
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(0));
		}

		@Test
		public void unknownPlatformId() throws Exception {
			when(definition.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent event = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definition));
			messageGate.onApplicationEvent(event);

			messageGate.clear(20L);

			verifyZeroInteractions(messageProvider);
			assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(1));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void invalidPlatformId() throws Exception {
			when(definition.getClassName()).thenReturn("class.one");
			ClassInstrumentationChangedEvent event = new ClassInstrumentationChangedEvent(this, 10L, Arrays.asList(definition));
			messageGate.onApplicationEvent(event);

			try {
				messageGate.clear(-10L);
			} finally {
				verifyZeroInteractions(messageProvider);
				assertThat(getDefinitionBuffer().get(10L).entrySet(), hasSize(1));
			}
		}
	}
}
