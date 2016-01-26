package info.novatec.inspectit.agent.analyzer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.analyzer.IClassHashHelper;
import info.novatec.inspectit.agent.analyzer.classes.TestClass;
import info.novatec.inspectit.agent.config.IConfigurationStorage;
import info.novatec.inspectit.agent.connection.IConnection;
import info.novatec.inspectit.agent.connection.ServerUnavailableException;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IHookDispatcherMapper;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.config.FunctionalInstrumentationType;
import info.novatec.inspectit.instrumentation.config.IMethodInstrumentationPoint;
import info.novatec.inspectit.instrumentation.config.impl.FunctionalInstrumentationPoint;
import info.novatec.inspectit.instrumentation.config.impl.InstrumentationResult;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.testbase.TestBase;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings({ "PMD", "unchecked", "rawtypes" })
public class ByteCodeAnalyzerTest extends TestBase {

	@InjectMocks
	protected ByteCodeAnalyzer byteCodeAnalyzer;

	@Mock
	protected Logger log;

	@Mock
	protected IIdManager idManager;

	@Mock
	protected IConnection connection;

	@Mock
	protected IHookDispatcherMapper hookDispatcherMapper;

	@Mock
	protected InstrumentationResult instrumentationResult;

	@Mock
	protected MethodInstrumentationConfig methodInstrumentationConfig;

	@Mock
	protected RegisteredSensorConfig registeredSensorConfig;

	@Mock
	protected IConfigurationStorage configurationStorage;

	@Mock
	protected IClassHashHelper classHashHelper;

	@Mock
	protected ICoreService coreService;

	@Mock
	protected ScheduledExecutorService executorService;

	@Mock
	protected MethodVisitor methodVisitor;

	protected final Long platformId = 10L;

	@BeforeMethod
	public void setup() throws IdNotAvailableException, ServerUnavailableException {
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(coreService.getExecutorService()).thenReturn(executorService);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				((Runnable) invocation.getArguments()[0]).run();
				return null;
			}
		}).when(executorService).submit(Mockito.<Runnable> any());
	}

	protected byte[] getByteCode(String className) throws IOException {
		// get byte-code via ASM
		ClassReader reader = new ClassReader(className);
		ClassWriter writer = new ClassWriter(reader, 0);
		reader.accept(writer, 0);
		return writer.toByteArray();
	}

	public class AnalyzeAndInstrument extends ByteCodeAnalyzerTest {

		@Test
		public void instrumentation() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			// make registered sensor config always instrument toString
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getRegisteredSensorConfig()).thenReturn(registeredSensorConfig);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(registeredSensorConfig));
			when(registeredSensorConfig.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString())).thenReturn(methodVisitor);

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(false);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeAndInstrument(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			long[] sensorIds = { 17L };
			when(registeredSensorConfig.getId()).thenReturn(rscId);
			when(registeredSensorConfig.getSensorIds()).thenReturn(sensorIds);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as instrumentation happened, we get a not null object
			assertThat(instrumentedByteCode, is(not(nullValue())));

			verify(connection, times(2)).isConnected();
			verify(connection, times(1)).analyzeAndInstrument(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
			verify(connection, times(1)).instrumentationApplied(captor.capture());
			assertThat(captor.getValue().size(), is(1));
			assertThat((Map<Long, long[]>) captor.getValue(), hasEntry(rscId, sensorIds));
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(hashCaptor.getValue(), instrumentationResult);
			verify(hookDispatcherMapper, times(1)).addMapping(rscId, registeredSensorConfig);
			verify(coreService, times(1)).getExecutorService();
			verifyNoMoreInteractions(hookDispatcherMapper, connection, classHashHelper, coreService);
		}

		@Test
		public void nullByteCodeAndClassLoaderInstrumentation() throws Exception {
			String className = String.class.getName();

			// make registered sensor config always instrument toString
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getRegisteredSensorConfig()).thenReturn(registeredSensorConfig);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(registeredSensorConfig));
			when(registeredSensorConfig.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString())).thenReturn(methodVisitor);

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(false);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeAndInstrument(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			long[] sensorIds = { 17L };
			when(registeredSensorConfig.getId()).thenReturn(rscId);
			when(registeredSensorConfig.getSensorIds()).thenReturn(sensorIds);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(null, className, null);

			// as instrumentation happened, we get a not null object
			assertThat(instrumentedByteCode, is(not(nullValue())));

			verify(connection, times(2)).isConnected();
			verify(connection, times(1)).analyzeAndInstrument(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
			verify(connection, times(1)).instrumentationApplied(captor.capture());
			assertThat(captor.getValue().size(), is(1));
			assertThat((Map<Long, long[]>) captor.getValue(), hasEntry(rscId, sensorIds));
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(hashCaptor.getValue(), instrumentationResult);
			verify(hookDispatcherMapper, times(1)).addMapping(rscId, registeredSensorConfig);
			verify(coreService, times(1)).getExecutorService();
			verifyNoMoreInteractions(hookDispatcherMapper, connection, classHashHelper, coreService);
		}

		@Test
		public void functionalInstrumentation() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			// need to fake the method
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getAllInstrumentationPoints())
			.thenReturn(Collections.<IMethodInstrumentationPoint> singleton(new FunctionalInstrumentationPoint(FunctionalInstrumentationType.CLASS_LOADING_DELEGATION)));

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(false);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeAndInstrument(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as instrumentation happened, we get a not null object
			assertThat(instrumentedByteCode, is(not(nullValue())));

			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).analyzeAndInstrument(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(hashCaptor.getValue(), instrumentationResult);
			verifyNoMoreInteractions(connection, classHashHelper);
			verifyZeroInteractions(hookDispatcherMapper, coreService);
		}

		@Test
		public void notToBeSentNoInstrumentation() throws IOException {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(true);
			when(classHashHelper.getInstrumentationResult(hashCaptor.capture())).thenReturn(null);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// we did not send the class type
			assertThat(instrumentedByteCode, is(nullValue()));
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			verifyZeroInteractions(idManager, connection, hookDispatcherMapper, coreService);
			// but we asked for the instrumentation result
			verify(classHashHelper, times(1)).getInstrumentationResult(hashCaptor.getValue());
			verifyNoMoreInteractions(classHashHelper);
		}

		@Test
		public void notToBeSentCachedInstrumentation() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			// make registered sensor config always instrument toString
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getRegisteredSensorConfig()).thenReturn(registeredSensorConfig);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(registeredSensorConfig));
			when(registeredSensorConfig.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString())).thenReturn(methodVisitor);

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(true);
			when(classHashHelper.getInstrumentationResult(hashCaptor.capture())).thenReturn(instrumentationResult);

			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			long[] sensorIds = { 17L };
			when(registeredSensorConfig.getId()).thenReturn(rscId);
			when(registeredSensorConfig.getSensorIds()).thenReturn(sensorIds);
			when(connection.isConnected()).thenReturn(true);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// we did not send the class type
			assertThat(instrumentedByteCode, is(not(nullValue())));
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			// but we asked for the instrumentation result and instrumented
			verify(classHashHelper, times(1)).getInstrumentationResult(hashCaptor.getValue());
			verify(hookDispatcherMapper, times(1)).addMapping(rscId, registeredSensorConfig);
			ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).instrumentationApplied(captor.capture());
			assertThat(captor.getValue().size(), is(1));
			assertThat((Map<Long, long[]>) captor.getValue(), hasEntry(rscId, sensorIds));
			verify(coreService, times(1)).getExecutorService();
			verifyNoMoreInteractions(hookDispatcherMapper, connection, classHashHelper, idManager, coreService);
		}

		@Test
		public void noInstrumentationResult() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(false);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeAndInstrument(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(null);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as no instrumentation happened, we get a null object
			assertThat(instrumentedByteCode, is(nullValue()));

			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).analyzeAndInstrument(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(hashCaptor.getValue(), null);
			verifyZeroInteractions(hookDispatcherMapper, coreService);
			verifyNoMoreInteractions(connection, classHashHelper);
		}

		@Test
		public void noInstrumentationConnectionOffline() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(false);
			when(connection.isConnected()).thenReturn(false);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as no instrumentation happened, we get a null object
			assertThat(instrumentedByteCode, is(nullValue()));

			verify(connection, times(1)).isConnected();
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			verifyZeroInteractions(hookDispatcherMapper, coreService);
			verifyNoMoreInteractions(connection, classHashHelper);
		}

		@Test
		public void noInstrumentationMissingMethod() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			// make registered sensor config always instrument toString
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("someOtherNonExistingMethod");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getRegisteredSensorConfig()).thenReturn(registeredSensorConfig);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(registeredSensorConfig));
			when(registeredSensorConfig.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString())).thenReturn(methodVisitor);

			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(hashCaptor.capture())).thenReturn(false);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyzeAndInstrument(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			when(registeredSensorConfig.getId()).thenReturn(rscId);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as no instrumentation happened, we get a null object
			assertThat(instrumentedByteCode, is(nullValue()));

			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).analyzeAndInstrument(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			verify(classHashHelper, times(1)).isSent(hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(hashCaptor.getValue(), instrumentationResult);
			verifyNoMoreInteractions(connection, classHashHelper);
			verifyZeroInteractions(hookDispatcherMapper, coreService);
		}

		@Test
		public void noInstrumentationMissingClass() throws Exception {
			String className = "someCrazyClass";

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(null, className, null);

			assertThat(instrumentedByteCode, is(nullValue()));

			verifyZeroInteractions(hookDispatcherMapper, coreService, connection, classHashHelper, configurationStorage, idManager);
		}

	}
}
