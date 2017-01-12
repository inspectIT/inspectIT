package rocks.inspectit.agent.java.instrumentation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;

import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.IThreadTransformHelper;
import rocks.inspectit.agent.java.analyzer.impl.ClassHashHelper;
import rocks.inspectit.agent.java.event.AgentMessagesReceivedEvent;
import rocks.inspectit.shared.all.communication.message.IAgentMessage;
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

	@Mock
	IThreadTransformHelper threadTransformHelper;

	/**
	 * Tests the {@link RetransformManager#onApplicationEvent(AgentMessagesReceivedEvent)} method.
	 *
	 */
	public static class OnApplicationEvent extends RetransformManagerTest {

		@Mock
		Object eventSource;

		@Test
		public void successful() throws UnmodifiableClassException {
			InstrumentationDefinition objectDefinition = mock(InstrumentationDefinition.class);
			when(objectDefinition.getClassName()).thenReturn("java.lang.Object");
			InstrumentationDefinition stringDefinition = mock(InstrumentationDefinition.class);
			when(stringDefinition.getClassName()).thenReturn("java.lang.String");
			IAgentMessage<?> message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getMessageContent().add(objectDefinition);
			((UpdatedInstrumentationMessage) message).getMessageContent().add(stringDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.<IAgentMessage<?>> asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });
			when(instrumentation.isModifiableClass(any(Class.class))).thenReturn(true);

			retransformManager.onApplicationEvent(event);

			verify(classHashHelper).registerInstrumentationDefinition(eq("java.lang.Object"), eq(objectDefinition));
			verify(classHashHelper).registerInstrumentationDefinition(eq("java.lang.String"), eq(stringDefinition));
			verify(instrumentation).getAllLoadedClasses();
			verify(instrumentation).retransformClasses(eq(Object.class), eq(String.class));
			verify(instrumentation, times(2)).isModifiableClass(any(Class.class));
			InOrder inOrder = inOrder(threadTransformHelper);
			inOrder.verify(threadTransformHelper).setThreadTransformDisabled(false);
			inOrder.verify(threadTransformHelper).setThreadTransformDisabled(true);
			verifyNoMoreInteractions(instrumentation, classHashHelper, threadTransformHelper);
		}

		@Test
		public void unmodifiableClass() throws UnmodifiableClassException {
			InstrumentationDefinition iDefinition = mock(InstrumentationDefinition.class);
			when(iDefinition.getClassName()).thenReturn("java.lang.Object");
			IAgentMessage<?> message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getMessageContent().add(iDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.<IAgentMessage<?>> asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });
			when(instrumentation.isModifiableClass(eq(Object.class))).thenReturn(false);

			retransformManager.onApplicationEvent(event);

			verify(classHashHelper).registerInstrumentationDefinition(eq("java.lang.Object"), eq(iDefinition));
			verify(instrumentation).getAllLoadedClasses();
			verify(instrumentation, times(2)).isModifiableClass(any(Class.class));
			verifyNoMoreInteractions(instrumentation, classHashHelper);
			verifyZeroInteractions(threadTransformHelper);
		}

		@Test
		@SuppressWarnings("unchecked")
		public void instrumentationThrowsOneException() throws UnmodifiableClassException {
			InstrumentationDefinition iDefinition = mock(InstrumentationDefinition.class);
			when(iDefinition.getClassName()).thenReturn("java.lang.Object");
			IAgentMessage<?> message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getMessageContent().add(iDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.<IAgentMessage<?>> asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });
			when(instrumentation.isModifiableClass(any(Class.class))).thenReturn(true).thenThrow(RuntimeException.class);

			retransformManager.onApplicationEvent(event);

			verify(classHashHelper).registerInstrumentationDefinition(eq("java.lang.Object"), eq(iDefinition));
			verify(instrumentation).getAllLoadedClasses();
			verify(instrumentation).retransformClasses(eq(Object.class));
			verify(instrumentation, times(2)).isModifiableClass(any(Class.class));
			InOrder inOrder = inOrder(threadTransformHelper);
			inOrder.verify(threadTransformHelper).setThreadTransformDisabled(false);
			inOrder.verify(threadTransformHelper).setThreadTransformDisabled(true);
			verifyNoMoreInteractions(instrumentation, classHashHelper, threadTransformHelper);
		}

		@Test
		public void unknownInstrumentationClass() throws UnmodifiableClassException {
			InstrumentationDefinition iDefinition = mock(InstrumentationDefinition.class);
			when(iDefinition.getClassName()).thenReturn("unknown.Class");
			IAgentMessage<?> message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getMessageContent().add(iDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.<IAgentMessage<?>> asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(event);

			verify(classHashHelper).registerInstrumentationDefinition(eq("unknown.Class"), eq(iDefinition));
			verify(instrumentation).getAllLoadedClasses();
			verify(instrumentation, times(2)).isModifiableClass(any(Class.class));
			verifyNoMoreInteractions(instrumentation, classHashHelper);
			verifyZeroInteractions(threadTransformHelper);
		}

		@Test
		public void noInstrumentationDefinitions() throws UnmodifiableClassException {
			IAgentMessage<?> message = new UpdatedInstrumentationMessage();
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.<IAgentMessage<?>> asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(event);

			verifyZeroInteractions(instrumentation, classHashHelper, threadTransformHelper);
		}

		@Test
		public void retransformationThrowsException() throws UnmodifiableClassException {
			InstrumentationDefinition iDefinition = mock(InstrumentationDefinition.class);
			when(iDefinition.getClassName()).thenReturn("java.lang.Object");
			IAgentMessage<?> message = new UpdatedInstrumentationMessage();
			((UpdatedInstrumentationMessage) message).getMessageContent().add(iDefinition);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.<IAgentMessage<?>> asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });
			doThrow(Exception.class).when(instrumentation).retransformClasses(any(Class.class));
			when(instrumentation.isModifiableClass(Matchers.<Class<?>> any())).thenReturn(true);

			retransformManager.onApplicationEvent(event);

			verify(classHashHelper).registerInstrumentationDefinition(eq("java.lang.Object"), eq(iDefinition));
			verify(instrumentation).getAllLoadedClasses();
			verify(instrumentation).retransformClasses(any(Class.class));
			verify(instrumentation, times(2)).isModifiableClass(any(Class.class));
			InOrder inOrder = inOrder(threadTransformHelper);
			inOrder.verify(threadTransformHelper).setThreadTransformDisabled(false);
			inOrder.verify(threadTransformHelper).setThreadTransformDisabled(true);
			verifyNoMoreInteractions(instrumentation, classHashHelper);
			verifyZeroInteractions(threadTransformHelper);
		}

		@Test
		public void nullEvent() throws UnmodifiableClassException {
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(null);

			verifyZeroInteractions(instrumentation, classHashHelper, threadTransformHelper);
		}

		@Test
		public void unknownMessageClass() throws UnmodifiableClassException {
			IAgentMessage<?> message = mock(IAgentMessage.class);
			AgentMessagesReceivedEvent event = new AgentMessagesReceivedEvent(eventSource, Arrays.<IAgentMessage<?>> asList(message));
			when(instrumentation.getAllLoadedClasses()).thenReturn(new Class[] { Object.class, String.class });

			retransformManager.onApplicationEvent(event);

			verifyZeroInteractions(instrumentation, classHashHelper, threadTransformHelper);
		}
	}

}
