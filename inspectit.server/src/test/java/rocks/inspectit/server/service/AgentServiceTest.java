package rocks.inspectit.server.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.instrumentation.NextGenInstrumentationManager;
import rocks.inspectit.server.messaging.AgentMessageProvider;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link AgentService} class.
 *
 * @author Marius Oehler
 *
 */
public class AgentServiceTest extends TestBase {

	@InjectMocks
	AgentService agentService;

	@Mock
	NextGenInstrumentationManager instrumentationManager;

	@Mock
	AgentMessageProvider messageProvider;

	/**
	 * Tests the {@link AgentService#analyze(long, String, Type)} method.
	 */
	public static class Analyze extends AgentServiceTest {

		@Test
		public void successful() throws Exception {
			Type type = mock(Type.class);

			agentService.analyze(10, "hash", type);

			verify(instrumentationManager).analyze(10L, "hash", type);
			verifyNoMoreInteractions(instrumentationManager);
			verifyZeroInteractions(messageProvider);
		}
	}

	/**
	 * Tests the {@link AgentService#analyzeJmxAttributes(long, Collection)} method.
	 */
	public static class AnalyzeJmxAttributes extends AgentServiceTest {

		@Test
		@SuppressWarnings("unchecked")
		public void successful() throws Exception {
			List<JmxAttributeDescriptor> attributeDescriptors = mock(List.class);

			agentService.analyzeJmxAttributes(10L, attributeDescriptors);

			verify(instrumentationManager).analyzeJmxAttributes(10L, attributeDescriptors);
			verifyNoMoreInteractions(instrumentationManager);
			verifyZeroInteractions(messageProvider, attributeDescriptors);
		}
	}

	/**
	 * Tests the {@link AgentService#fetchAgentMessages(long)} method.
	 */
	public static class FetchAgentMessages extends AgentServiceTest {

		@Test
		public void successful() throws Exception {
			agentService.fetchAgentMessages(10L);

			verify(messageProvider).fetchMessages(10L);
			verifyNoMoreInteractions(messageProvider);
			verifyZeroInteractions(instrumentationManager);
		}
	}

	/**
	 * Tests the {@link AgentService#instrumentationApplied(long, java.util.Map)} method.
	 */
	public static class InstrumentationApplied extends AgentServiceTest {

		@Test
		@SuppressWarnings("unchecked")
		public void successful() throws Exception {
			Map<Long, long[]> methodSensorMap = mock(Map.class);

			agentService.instrumentationApplied(10L, methodSensorMap);

			verify(instrumentationManager).instrumentationApplied(10L, methodSensorMap);
			verifyNoMoreInteractions(instrumentationManager);
			verifyZeroInteractions(messageProvider, methodSensorMap);
		}
	}

	/**
	 * Tests the {@link AgentService#register(List, String, String)} method.
	 */
	public static class Register extends AgentServiceTest {

		@Mock
		AgentConfig agentConfig;

		@Test
		@SuppressWarnings("unchecked")
		public void successful() throws Exception {
			List<String> definedIPs = mock(List.class);
			when(instrumentationManager.register(any(List.class), any(String.class), any(String.class))).thenReturn(agentConfig);
			when(agentConfig.getPlatformId()).thenReturn(10L);

			agentService.register(definedIPs, "name", "version");

			verify(instrumentationManager).register(definedIPs, "name", "version");
			verifyNoMoreInteractions(instrumentationManager, messageProvider);
			verifyZeroInteractions(messageProvider, definedIPs);
		}
	}

	/**
	 * Tests the {@link AgentService#unregister(long)} method.
	 */
	public static class Unregister extends AgentServiceTest {

		@Test
		public void successful() throws Exception {
			agentService.unregister(10L);

			verify(instrumentationManager).unregister(10L);
			verifyNoMoreInteractions(instrumentationManager);
			verifyZeroInteractions(messageProvider);
		}
	}
}
