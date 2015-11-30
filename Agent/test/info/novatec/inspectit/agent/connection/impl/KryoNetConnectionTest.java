package info.novatec.inspectit.agent.connection.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.config.impl.MethodSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.PlatformSensorTypeConfig;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.connection.RegistrationException;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.cmr.service.IAgentStorageService;
import info.novatec.inspectit.cmr.service.IKeepAliveService;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.kryonet.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryonet.rmi.TimeoutException;

@SuppressWarnings("PMD")
public class KryoNetConnectionTest {

	@InjectMocks
	protected KryoNetConnection connection;

	@Mock
	private Logger log;

	@Mock
	protected Client client;

	@Mock
	protected IAgentStorageService agentStorageService;

	@Mock
	protected IRegistrationService registrationService;

	@Mock
	protected IKeepAliveService keepAliveService;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
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
			doThrow(TimeoutException.class).when(agentStorageService).addDataObjects(Mockito.<List<? extends DefaultData>> any());
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
			doThrow(RuntimeException.class).when(agentStorageService).addDataObjects(Mockito.<List<? extends DefaultData>> any());
			List<DefaultData> measurements = new ArrayList<DefaultData>();
			measurements.add(new TimerData());

			try {
				connection.sendDataObjects(measurements);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// call depends on the retry strategy
				verify(agentStorageService, times(AdditiveWaitRetryStrategy.DEFAULT_NUMBER_OF_RETRIES)).addDataObjects(measurements);
				verifyNoMoreInteractions(agentStorageService);
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

	public static class RegisterPlatform extends KryoNetConnectionTest {

		@Test
		public void register() throws Exception {
			long id = 3L;
			when(client.isConnected()).thenReturn(true);
			doReturn(id).when(registrationService).registerPlatformIdent(Mockito.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			long registeredId = connection.registerPlatform(agentName, version);
			assertThat(registeredId, is(id));

			verify(registrationService, times(1)).registerPlatformIdent(Mockito.<List<String>> any(), eq(agentName), eq(version));
			verifyNoMoreInteractions(registrationService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(registrationService).registerPlatformIdent(Mockito.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			try {
				connection.registerPlatform(agentName, version);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(registrationService, times(1)).registerPlatformIdent(Mockito.<List<String>> any(), eq(agentName), eq(version));
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(registrationService).registerPlatformIdent(Mockito.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			try {
				connection.registerPlatform(agentName, version);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// fail fast call, only one attempt
				verify(registrationService, times(1)).registerPlatformIdent(Mockito.<List<String>> any(), eq(agentName), eq(version));
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { RegistrationException.class })
		public void businessException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(BusinessException.class).when(registrationService).registerPlatformIdent(Mockito.<List<String>> any(), anyString(), anyString());
			String agentName = "agentName";
			String version = "version";

			try {
				connection.registerPlatform(agentName, version);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verify(registrationService, times(1)).registerPlatformIdent(Mockito.<List<String>> any(), eq(agentName), eq(version));
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			String agentName = "agentName";
			String version = "version";

			try {
				connection.registerPlatform(agentName, version);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(registrationService);
			}
		}
	}

	public static class UnregisterPlatform extends KryoNetConnectionTest {

		@Test
		public void unregister() throws Exception {
			when(client.isConnected()).thenReturn(true);
			String agentName = "agentName";

			connection.unregisterPlatform(agentName);

			verify(registrationService, times(1)).unregisterPlatformIdent(Mockito.<List<String>> any(), eq(agentName));
			verifyNoMoreInteractions(registrationService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(registrationService).unregisterPlatformIdent(Mockito.<List<String>> any(), anyString());
			String agentName = "agentName";

			try {
				connection.unregisterPlatform(agentName);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(registrationService, times(1)).unregisterPlatformIdent(Mockito.<List<String>> any(), eq(agentName));
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(registrationService).unregisterPlatformIdent(Mockito.<List<String>> any(), anyString());
			String agentName = "agentName";

			try {
				connection.unregisterPlatform(agentName);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				// fail fast call, only one attempt
				verify(registrationService, times(1)).unregisterPlatformIdent(Mockito.<List<String>> any(), eq(agentName));
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { RegistrationException.class })
		public void businessException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(BusinessException.class).when(registrationService).unregisterPlatformIdent(Mockito.<List<String>> any(), anyString());
			String agentName = "agentName";

			try {
				connection.unregisterPlatform(agentName);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verify(registrationService, times(1)).unregisterPlatformIdent(Mockito.<List<String>> any(), eq(agentName));
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			String agentName = "agentName";

			try {
				connection.unregisterPlatform(agentName);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(registrationService);
			}
		}
	}

	public static class RegisterMethod extends KryoNetConnectionTest {

		@Test
		public void registerMethod() throws Exception {
			long id = 3L;
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doReturn(id).when(registrationService).registerMethodIdent(anyLong(), anyString(), anyString(), anyString(), Mockito.<List<String>> any(), anyString(), anyInt());

			String packageName = "p";
			String className = "c";
			String methodName = "m";
			List<String> parameterTypes = Collections.emptyList();
			String returnType = "r";
			int modifiers = 54321;
			RegisteredSensorConfig sensorConfig = mock(RegisteredSensorConfig.class);
			when(sensorConfig.getTargetPackageName()).thenReturn(packageName);
			when(sensorConfig.getTargetClassName()).thenReturn(className);
			when(sensorConfig.getTargetMethodName()).thenReturn(methodName);
			when(sensorConfig.getParameterTypes()).thenReturn(parameterTypes);
			when(sensorConfig.getReturnType()).thenReturn(returnType);
			when(sensorConfig.getModifiers()).thenReturn(modifiers);

			long registeredId = connection.registerMethod(platformIdent, sensorConfig);
			assertThat(registeredId, is(id));

			verify(registrationService, times(1)).registerMethodIdent(platformIdent, packageName, className, methodName, parameterTypes, returnType, modifiers);
			verifyNoMoreInteractions(registrationService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws ServerUnavailableException, RegistrationException {
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(registrationService).registerMethodIdent(anyLong(), anyString(), anyString(), anyString(), Mockito.<List<String>> any(), anyString(), anyInt());

			String packageName = "p";
			String className = "c";
			String methodName = "m";
			List<String> parameterTypes = Collections.emptyList();
			String returnType = "r";
			int modifiers = 54321;
			RegisteredSensorConfig sensorConfig = mock(RegisteredSensorConfig.class);
			when(sensorConfig.getTargetPackageName()).thenReturn(packageName);
			when(sensorConfig.getTargetClassName()).thenReturn(className);
			when(sensorConfig.getTargetMethodName()).thenReturn(methodName);
			when(sensorConfig.getParameterTypes()).thenReturn(parameterTypes);
			when(sensorConfig.getReturnType()).thenReturn(returnType);
			when(sensorConfig.getModifiers()).thenReturn(modifiers);

			try {
				connection.registerMethod(platformIdent, sensorConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(registrationService, times(1)).registerMethodIdent(platformIdent, packageName, className, methodName, parameterTypes, returnType, modifiers);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws ServerUnavailableException, RegistrationException {
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(registrationService).registerMethodIdent(anyLong(), anyString(), anyString(), anyString(), Mockito.<List<String>> any(), anyString(), anyInt());

			String packageName = "p";
			String className = "c";
			String methodName = "m";
			List<String> parameterTypes = Collections.emptyList();
			String returnType = "r";
			int modifiers = 54321;
			RegisteredSensorConfig sensorConfig = mock(RegisteredSensorConfig.class);
			when(sensorConfig.getTargetPackageName()).thenReturn(packageName);
			when(sensorConfig.getTargetClassName()).thenReturn(className);
			when(sensorConfig.getTargetMethodName()).thenReturn(methodName);
			when(sensorConfig.getParameterTypes()).thenReturn(parameterTypes);
			when(sensorConfig.getReturnType()).thenReturn(returnType);
			when(sensorConfig.getModifiers()).thenReturn(modifiers);

			try {
				connection.registerMethod(platformIdent, sensorConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verify(registrationService, times(AdditiveWaitRetryStrategy.DEFAULT_NUMBER_OF_RETRIES)).registerMethodIdent(platformIdent, packageName, className, methodName, parameterTypes,
						returnType, modifiers);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws ServerUnavailableException, RegistrationException {
			when(client.isConnected()).thenReturn(false);
			long platformIdent = 7L;
			RegisteredSensorConfig sensorConfig = mock(RegisteredSensorConfig.class);

			try {
				connection.registerMethod(platformIdent, sensorConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(registrationService);
			}
		}
	}

	public static class RegisterMethodSensor extends KryoNetConnectionTest {

		@Test
		public void registerMethodSensor() throws ServerUnavailableException, RegistrationException {
			long id = 3L;
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doReturn(id).when(registrationService).registerMethodSensorTypeIdent(anyLong(), anyString(), Mockito.<Map<String, Object>> any());

			String className = "c";
			Map<String, Object> parameters = Collections.emptyMap();
			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensorTypeConfig.getClassName()).thenReturn(className);
			when(methodSensorTypeConfig.getParameters()).thenReturn(parameters);

			long registeredId = connection.registerMethodSensorType(platformIdent, methodSensorTypeConfig);
			assertThat(registeredId, is(id));

			verify(registrationService, times(1)).registerMethodSensorTypeIdent(platformIdent, className, parameters);
			verifyNoMoreInteractions(registrationService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws ServerUnavailableException, RegistrationException {
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(registrationService).registerMethodSensorTypeIdent(anyLong(), anyString(), Mockito.<Map<String, Object>> any());

			String className = "c";
			Map<String, Object> parameters = Collections.emptyMap();
			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensorTypeConfig.getClassName()).thenReturn(className);
			when(methodSensorTypeConfig.getParameters()).thenReturn(parameters);

			try {
				connection.registerMethodSensorType(platformIdent, methodSensorTypeConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(registrationService, times(1)).registerMethodSensorTypeIdent(platformIdent, className, parameters);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(registrationService).registerMethodSensorTypeIdent(anyLong(), anyString(), Mockito.<Map<String, Object>> any());

			String className = "c";
			Map<String, Object> parameters = Collections.emptyMap();
			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);
			when(methodSensorTypeConfig.getClassName()).thenReturn(className);
			when(methodSensorTypeConfig.getParameters()).thenReturn(parameters);

			try {
				connection.registerMethodSensorType(platformIdent, methodSensorTypeConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verify(registrationService, times(AdditiveWaitRetryStrategy.DEFAULT_NUMBER_OF_RETRIES)).registerMethodSensorTypeIdent(platformIdent, className, parameters);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			long platformIdent = 7L;
			MethodSensorTypeConfig methodSensorTypeConfig = mock(MethodSensorTypeConfig.class);

			try {
				connection.registerMethodSensorType(platformIdent, methodSensorTypeConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(registrationService);
			}
		}
	}

	public static class RegisterPlatformSensor extends KryoNetConnectionTest {

		@Test
		public void registerPlatformSensor() throws Exception {
			long id = 3L;
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doReturn(id).when(registrationService).registerPlatformSensorTypeIdent(anyLong(), anyString());

			String className = "c";
			PlatformSensorTypeConfig platformSensorTypeConfig = mock(PlatformSensorTypeConfig.class);
			when(platformSensorTypeConfig.getClassName()).thenReturn(className);

			long registeredId = connection.registerPlatformSensorType(platformIdent, platformSensorTypeConfig);
			assertThat(registeredId, is(id));

			verify(registrationService, times(1)).registerPlatformSensorTypeIdent(platformIdent, className);
			verifyNoMoreInteractions(registrationService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(registrationService).registerPlatformSensorTypeIdent(anyLong(), anyString());

			String className = "c";
			PlatformSensorTypeConfig platformSensorTypeConfig = mock(PlatformSensorTypeConfig.class);
			when(platformSensorTypeConfig.getClassName()).thenReturn(className);

			try {
				connection.registerPlatformSensorType(platformIdent, platformSensorTypeConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(registrationService, times(1)).registerPlatformSensorTypeIdent(platformIdent, className);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			long platformIdent = 7L;
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(registrationService).registerPlatformSensorTypeIdent(anyLong(), anyString());

			String className = "c";
			PlatformSensorTypeConfig platformSensorTypeConfig = mock(PlatformSensorTypeConfig.class);
			when(platformSensorTypeConfig.getClassName()).thenReturn(className);

			try {
				connection.registerPlatformSensorType(platformIdent, platformSensorTypeConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verify(registrationService, times(AdditiveWaitRetryStrategy.DEFAULT_NUMBER_OF_RETRIES)).registerPlatformSensorTypeIdent(platformIdent, className);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			long platformIdent = 7L;
			PlatformSensorTypeConfig platformSensorTypeConfig = mock(PlatformSensorTypeConfig.class);

			try {
				connection.registerPlatformSensorType(platformIdent, platformSensorTypeConfig);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(registrationService);
			}
		}
	}

	public static class AddSensorToMethod extends KryoNetConnectionTest {

		@Test
		public void addSensorToMethod() throws Exception {
			when(client.isConnected()).thenReturn(true);
			long methodId = 3L;
			long sensorId = 7L;

			connection.addSensorTypeToMethod(sensorId, methodId);

			verify(registrationService, times(1)).addSensorTypeToMethod(sensorId, methodId);
			verifyNoMoreInteractions(registrationService);
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void timeout() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(TimeoutException.class).when(registrationService).addSensorTypeToMethod(anyLong(), anyLong());
			long methodId = 3L;
			long sensorId = 7L;

			try {
				connection.addSensorTypeToMethod(sensorId, methodId);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(true));
				throw e;
			} finally {
				verify(registrationService, times(1)).addSensorTypeToMethod(sensorId, methodId);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void remoteException() throws Exception {
			when(client.isConnected()).thenReturn(true);
			doThrow(RuntimeException.class).when(registrationService).addSensorTypeToMethod(anyLong(), anyLong());
			long methodId = 3L;
			long sensorId = 7L;

			try {
				connection.addSensorTypeToMethod(sensorId, methodId);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verify(registrationService, times(AdditiveWaitRetryStrategy.DEFAULT_NUMBER_OF_RETRIES)).addSensorTypeToMethod(sensorId, methodId);
				verifyNoMoreInteractions(registrationService);
			}
		}

		@Test(expectedExceptions = { ServerUnavailableException.class })
		public void notConnected() throws Exception {
			when(client.isConnected()).thenReturn(false);
			long methodId = 3L;
			long sensorId = 7L;

			try {
				connection.addSensorTypeToMethod(sensorId, methodId);
			} catch (ServerUnavailableException e) {
				assertThat(e.isServerTimeout(), is(false));
				throw e;
			} finally {
				verifyZeroInteractions(registrationService);
			}
		}
	}
}
