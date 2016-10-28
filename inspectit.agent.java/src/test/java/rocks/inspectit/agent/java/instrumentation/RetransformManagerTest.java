package rocks.inspectit.agent.java.instrumentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.analyzer.impl.ClassHashHelper;
import rocks.inspectit.agent.java.event.AgentMessagesReceivedEvent;
import rocks.inspectit.shared.all.communication.message.AbstractAgentMessage;
import rocks.inspectit.shared.all.communication.message.UpdatedInstrumentationMessage;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test the {@link RetransformManager} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class RetransformManagerTest extends TestBase {

	@InjectMocks
	RetransformManager retransformManager;

	@Mock
	Logger log;

	@Mock
	Instrumentation instrumentation;

	@Mock
	ClassHashHelper classHashHelper;

	/**
	 * Tests the {@link RetransformManager#onApplicationEvent(AgentMessagesReceivedEvent)} method.
	 *
	 */
	public static class OnApplicationEvent extends RetransformManagerTest {

		@Mock
		Object eventSource;

		@Test
		@SuppressWarnings("rawtypes")
		public void successful() throws UnmodifiableClassException {
			InstrumentationDefinition iDefinition = mock(InstrumentationDefinition.class);
			when(iDefinition.getClassName()).thenReturn("java.lang.Object");
			AbstractAgentMessage message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getUpdatedInstrumentationDefinitions().add(iDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(event);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<InstrumentationDefinition> definitionCaptor = ArgumentCaptor.forClass(InstrumentationDefinition.class);
			ArgumentCaptor<Class> classCaptor = ArgumentCaptor.forClass(Class.class);
			verify(classHashHelper).registerInstrumentationDefinition(fqnCaptor.capture(), definitionCaptor.capture());
			verify(instrumentation).getAllLoadedClasses();
			verify(instrumentation).retransformClasses(classCaptor.capture());
			verifyNoMoreInteractions(instrumentation, classHashHelper);
			assertThat(fqnCaptor.getValue(), is(equalTo("java.lang.Object")));
			assertThat(definitionCaptor.getValue(), is(equalTo(iDefinition)));
			assertThat(classCaptor.getAllValues(), contains((Class) Object.class));
		}

		@Test
		public void unknownInstrumentationClass() throws UnmodifiableClassException {
			InstrumentationDefinition iDefinition = mock(InstrumentationDefinition.class);
			when(iDefinition.getClassName()).thenReturn("unknown.Class");
			AbstractAgentMessage message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getUpdatedInstrumentationDefinitions().add(iDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(event);

			verify(instrumentation).getAllLoadedClasses();
			verifyNoMoreInteractions(instrumentation);
			verifyZeroInteractions(classHashHelper);
		}

		@Test
		public void noInstrumentationDefinitions() throws UnmodifiableClassException {
			AbstractAgentMessage message = new UpdatedInstrumentationMessage();
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(event);

			verifyZeroInteractions(instrumentation, classHashHelper);
		}

		@Test
		public void retransformationThrowsException() throws UnmodifiableClassException {
			InstrumentationDefinition iDefinition = mock(InstrumentationDefinition.class);
			when(iDefinition.getClassName()).thenReturn("java.lang.Object");
			AbstractAgentMessage message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getUpdatedInstrumentationDefinitions().add(iDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });
			doThrow(Exception.class).when(instrumentation).retransformClasses(any(Class.class));

			retransformManager.onApplicationEvent(event);

			verify(classHashHelper).registerInstrumentationDefinition(any(String.class), any(InstrumentationDefinition.class));
			verify(instrumentation).getAllLoadedClasses();
			verify(instrumentation).retransformClasses(any(Class.class));
			verifyNoMoreInteractions(instrumentation, classHashHelper);
		}

		@Test
		public void nullEvent() throws UnmodifiableClassException {
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(null);

			verifyZeroInteractions(instrumentation, classHashHelper);
		}

		@Test
		public void unknownMessageClass() throws UnmodifiableClassException {
			AbstractAgentMessage message = new AbstractAgentMessage() {
			};
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(event);

			verifyZeroInteractions(instrumentation, classHashHelper);
		}
	}

}
