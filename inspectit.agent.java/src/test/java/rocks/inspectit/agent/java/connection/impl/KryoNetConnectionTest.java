package rocks.inspectit.agent.java.connection.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import com.esotericsoftware.kryonet.rmi.TimeoutException;

import rocks.inspectit.agent.java.connection.RetryStrategy;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.shared.all.cmr.service.IAgentService;
import rocks.inspectit.shared.all.cmr.service.IAgentStorageService;
import rocks.inspectit.shared.all.cmr.service.IKeepAliveService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.kryonet.Client;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings({ "PMD", "unchecked" })
public class KryoNetConnectionTest extends TestBase {

	@InjectMocks
	KryoNetConnection connection;

	@Mock
	Logger log;

	@Mock
	Client client;

	@Mock
	IAgentStorageService agentStorageService;

	@Mock
	IAgentService agentService;

	@Mock
	IKeepAliveService keepAliveService;

	public static class Connect extends KryoNetConnectionTest {

		@Test
		public void connect() throws IOException {
			when(client.isConnected()).thenReturn(false);
			String host = "host";
			int port = 22;

			connection.connect(host, port);

			verify(client).start();
			verify(client).connect(anyInt(), eq(host), eq(port));
			verify(client, atLeast(1)).isConnected();
			verifyNoMoreInteractions(client);
		}

		@Test
		public void alreadyConnected() throws ConnectException {
			when(client.isConnected()).thenReturn(true);

			connection.connect("host", 22);

			verify(client).isConnected();
			verifyNoMoreInteractions(client);
		}

		@Test(expectedExceptions = ConnectException.class)
		public void ioException() throws IOException {
			when(client.isConnected()).thenReturn(false);
			String host = "host";
			int port = 22;
			doThrow(IOException.class).when(client).connect(anyInt(), eq(host), eq(port));

			connection.connect(host, port);
		}
	}

	public static class Reconnect extends KryoNetConnectionTest {

		@Test
		public void reconnect() throws IOException {
			when(client.isConnected()).thenReturn(false);

			connection.reconnect();

			verify(client).reconnect();
			verify(client, atLeast(1)).isConnected();
			verifyNoMoreInteractions(client);
		}

		@Test
		public void alreadyConnected() throws ConnectException {
			when(client.isConnected()).thenReturn(true);

			connection.reconnect();

			verify(client).isConnected();
			verifyNoMoreInteractions(client);
		}

		@Test(expectedExceptions = ConnectException.class)
		public void ioException() throws IOException {
			when(client.isConnected()).thenReturn(false);
			doThrow(IOException.class).when(client).reconnect();

			connection.reconnect();
		}
	}

	public static class Disconnect extends KryoNetConnectionTest {

		@Test
		public void disconnect() {
			connection.disconnect();

			verify(client).stop();
		}
	}

	public static class KeepAlive extends KryoNetConnectionTest {

		@Test
		public void keepAlive() throws Exception {
			when(client.isConnected()).thenReturn(true);
			long id = 3L;

			connection.sendKeepAlive(id);

			verify(keepAliveService, times(1)).sendKeepAlive(id);
			verifyNoMoreInteractions(keepAliveService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(keepAliveService).sendKeepAlive(anyLong());
			long id = 3L;

			try {
				connection.sendKeepAlive(id);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(keepAliveService, times(1)).sendKeepAlive(id);
				verifyNoMoreInteractions(keepAliveService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(keepAliveService).sendKeepAlive(anyLong());
			long id = 3L;

			try {
				connection.sendKeepAlive(id);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// fail fast call, only one attempt
				verify(keepAliveService, times(1)).sendKeepAlive(id);
				verifyNoMoreInteractions(keepAliveService);
				verify(client).close();
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			long id = 3L;

			try {
				connection.sendKeepAlive(id);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(keepAliveService);
			}
		}
	}

	public static class SendData extends KryoNetConnectionTest {

		@Test
		public void sendData() throws Exception {
			when(client.isConnected()).thenReturn(true);
			List<DefaultData> measurements = new ArrayList<DefaultData>();
			measurements.add(new TimerData());

			connection.sendDataObjects(measurements);

			verify(agentStorageService, times(1)).addDataObjects(measurements);
			verifyNoMoreInteractions(agentStorageService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(agentStorageService).addDataObjects(Matchers.<List<? extends DefaultData>> any());
			List<DefaultData> measurements = new ArrayList<DefaultData>();
			measurements.add(new TimerData());

			try {
				connection.sendDataObjects(measurements);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(agentStorageService, times(1)).addDataObjects(measurements);
				verifyNoMoreInteractions(agentStorageService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(agentStorageService).addDataObjects(Matchers.<List<? extends DefaultData>> any());
			List<DefaultData> measurements = new ArrayList<DefaultData>();
			measurements.add(new TimerData());

			try {
				connection.sendDataObjects(measurements);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// call depends on the retry strategy
				verify(agentStorageService, times(RetryStrategy.DEFAULT_NUMBER_OF_RETRIES)).addDataObjects(measurements);
				verifyNoMoreInteractions(agentStorageService);
				verify(client).close();
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			List<DefaultData> measurements = new ArrayList<DefaultData>();
			measurements.add(new TimerData());

			try {
				connection.sendDataObjects(measurements);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(agentStorageService);
			}
		}
	}

	public static class Register extends KryoNetConnectionTest {

		@Test
		public void register() throws Exception {
			AgentConfig agentConfiguration = mock(AgentConfig.class);
			when(client.isConnected()).thenReturn(true);
			doReturn(agentConfiguration).when(agentService).register(Matchers.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			AgentConfig receivedAgentConfiguration = connection.register(agentName, version);
			assertThat(receivedAgentConfiguration, is(agentConfiguration));

			verify(agentService, times(1)).register(Matchers.<List<String>> any(), eq(agentName), eq(version));
			verifyNoMoreInteractions(agentService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(agentService).register(Matchers.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			try {
				connection.register(agentName, version);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(agentService, times(1)).register(Matchers.<List<String>> any(), eq(agentName), eq(version));
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(agentService).register(Matchers.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			try {
				connection.register(agentName, version);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// fail fast call, only one attempt
				verify(agentService, times(1)).register(Matchers.<List<String>> any(), eq(agentName), eq(version));
				verifyNoMoreInteractions(agentService);
				verify(client).close();
			}
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void businessException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(BusinessException.class).when(agentService).register(Matchers.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			try {
				connection.register(agentName, version);
			} finally {
				verify(agentService, times(1)).register(Matchers.<List<String>> any(), eq(agentName), eq(version));
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			String agentName = "agentName";
			String version = "version";

			try {
				connection.register(agentName, version);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(agentService);
			}
		}
	}

	public static class Unregister extends KryoNetConnectionTest {

		@Test
		public void unregister() throws Exception {
			when(client.isConnected()).thenReturn(true);
			long platformId = 10L;

			connection.unregister(platformId);

			verify(agentService, times(1)).unregister(platformId);
			verifyNoMoreInteractions(agentService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(agentService).unregister(anyLong());
			long platformId = 10L;

			try {
				connection.unregister(platformId);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(agentService, times(1)).unregister(platformId);
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(agentService).unregister(anyLong());
			long platformId = 10L;

			try {
				connection.unregister(platformId);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// fail fast call, only one attempt
				verify(agentService, times(1)).unregister(platformId);
				verifyNoMoreInteractions(agentService);
				verify(client).close();
			}
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void businessException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(BusinessException.class).when(agentService).unregister(anyLong());
			long platformId = 10L;

			try {
				connection.unregister(platformId);
			} finally {
				verify(agentService, times(1)).unregister(platformId);
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			long platformId = 10L;

			try {
				connection.unregister(platformId);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(agentService);
			}
		}
	}

	public static class Analyze extends KryoNetConnectionTest {

		@Test
		public void analyzeAndInstrument() throws Exception {
			InstrumentationDefinition instrumentationResult = mock(InstrumentationDefinition.class);
			when(client.isConnected()).thenReturn(true);
			doReturn(instrumentationResult).when(agentService).analyze(anyLong(), anyString(), Matchers.<Type> any());
			long id = 7;
			String hash = "hash";
			Type type = mock(Type.class);

			InstrumentationDefinition receivedResult = connection.analyze(id, hash, type);
			assertThat(receivedResult, is(instrumentationResult));

			verify(agentService, times(1)).analyze(id, hash, type);
			verifyNoMoreInteractions(agentService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(agentService).analyze(anyLong(), anyString(), Matchers.<Type> any());
			long id = 7;
			String hash = "hash";
			Type type = mock(Type.class);

			try {
				connection.analyze(id, hash, type);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(agentService, times(1)).analyze(id, hash, type);
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(agentService).analyze(anyLong(), anyString(), Matchers.<Type> any());
			long id = 7;
			String hash = "hash";
			Type type = mock(Type.class);

			try {
				connection.analyze(id, hash, type);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// fail fast call, only one attempt
				verify(agentService, times(1)).analyze(id, hash, type);
				verifyNoMoreInteractions(agentService);
				verify(client).close();
			}
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void businessException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(BusinessException.class).when(agentService).analyze(anyLong(), anyString(), Matchers.<Type> any());
			long id = 7;
			String hash = "hash";
			Type type = mock(Type.class);

			try {
				connection.analyze(id, hash, type);
			} finally {
				verify(agentService, times(1)).analyze(id, hash, type);
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			long id = 7;
			String hash = "hash";
			Type type = mock(Type.class);

			try {
				connection.analyze(id, hash, type);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(agentService);
			}
		}
	}

	public static class AnalyzeJmxAttributes extends KryoNetConnectionTest {

		@Test
		public void analyzeJmxAttributes() throws Exception {
			Collection<JmxAttributeDescriptor> result = mock(Collection.class);
			when(client.isConnected()).thenReturn(true);
			doReturn(result).when(agentService).analyzeJmxAttributes(anyLong(), Mockito.<Collection<JmxAttributeDescriptor>> any());
			long id = 7;
			Collection<JmxAttributeDescriptor> descriptors = Collections.emptyList();

			Collection<JmxAttributeDescriptor> receivedResult = connection.analyzeJmxAttributes(id, descriptors);
			assertThat(receivedResult, is(result));

			verify(agentService, times(1)).analyzeJmxAttributes(id, descriptors);
			verifyNoMoreInteractions(agentService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(agentService).analyzeJmxAttributes(anyLong(), Mockito.<Collection<JmxAttributeDescriptor>> any());
			long id = 7;
			Collection<JmxAttributeDescriptor> descriptors = Collections.emptyList();

			try {
				connection.analyzeJmxAttributes(id, descriptors);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(agentService, times(1)).analyzeJmxAttributes(id, descriptors);
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(agentService).analyzeJmxAttributes(anyLong(), Mockito.<Collection<JmxAttributeDescriptor>> any());
			long id = 7;
			Collection<JmxAttributeDescriptor> descriptors = Collections.emptyList();

			try {
				connection.analyzeJmxAttributes(id, descriptors);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// fail fast call, only one attempt
				verify(agentService, times(1)).analyzeJmxAttributes(id, descriptors);
				verifyNoMoreInteractions(agentService);
				verify(client).close();
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			long id = 7;
			Collection<JmxAttributeDescriptor> descriptors = Collections.emptyList();

			try {
				connection.analyzeJmxAttributes(id, descriptors);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(agentService);
			}
		}
	}

	public static class InstrumentationApplied extends KryoNetConnectionTest {

		@Test
		public void instrumentationApplied() throws Exception {
			when(client.isConnected()).thenReturn(true);
			Map<Long, long[]> methodToSensorMap = mock(Map.class);
			when(methodToSensorMap.isEmpty()).thenReturn(false);
			long id = 7;

			connection.instrumentationApplied(id, methodToSensorMap);

			verify(agentService, times(1)).instrumentationApplied(id, methodToSensorMap);
			verifyNoMoreInteractions(agentService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(agentService).instrumentationApplied(anyLong(), Matchers.<Map<Long, long[]>> any());
			Map<Long, long[]> methodToSensorMap = mock(Map.class);
			when(methodToSensorMap.isEmpty()).thenReturn(false);
			long id = 7;

			try {
				connection.instrumentationApplied(id, methodToSensorMap);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(agentService, times(1)).instrumentationApplied(id, methodToSensorMap);
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(agentService).instrumentationApplied(anyLong(), Matchers.<Map<Long, long[]>> any());
			Map<Long, long[]> methodToSensorMap = mock(Map.class);
			when(methodToSensorMap.isEmpty()).thenReturn(false);
			long id = 7;

			try {
				connection.instrumentationApplied(id, methodToSensorMap);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// call depends on the retry strategy
				verify(agentService, times(RetryStrategy.DEFAULT_NUMBER_OF_RETRIES)).instrumentationApplied(id, methodToSensorMap);
				verifyNoMoreInteractions(agentService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			Map<Long, long[]> methodToSensorMap = mock(Map.class);
			long id = 7;

			try {
				connection.instrumentationApplied(id, methodToSensorMap);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(agentService);
			}
		}
	}
}
