package rocks.inspectit.agent.java.analyzer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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

import rocks.inspectit.agent.java.analyzer.classes.AbstractSubTest;
import rocks.inspectit.agent.java.analyzer.classes.TestClass;
import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.connection.IConnection;
import rocks.inspectit.agent.java.connection.ServerUnavailableException;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.hooking.IHookDispatcherMapper;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.SpecialInstrumentationType;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings({ "PMD", "unchecked", "rawtypes" })
public class ByteCodeAnalyzerTest extends TestBase {

	@InjectMocks
	ByteCodeAnalyzer byteCodeAnalyzer;

	@Mock
	Logger log;

	@Mock
	IPlatformManager platformManager;

	@Mock
	IConnection connection;

	@Mock
	IHookDispatcherMapper hookDispatcherMapper;

	@Mock
	InstrumentationDefinition instrumentationResult;

	@Mock
	MethodInstrumentationConfig methodInstrumentationConfig;

	@Mock
	SensorInstrumentationPoint sensorInstrumentationPoint;

	@Mock
	IConfigurationStorage configurationStorage;

	@Mock
	ClassHashHelper classHashHelper;

	@Mock
	ICoreService coreService;

	@Mock
	ScheduledExecutorService executorService;

	@Mock
	MethodVisitor methodVisitor;

	@Mock
	MethodSensorTypeConfig methodSensorTypeConfig;

	@Mock
	IMethodSensor methodSensor;

	@Mock
	List<IMethodSensor> methodSensors;

	final Long platformId = 10L;

	@BeforeMethod
	public void setup() throws IdNotAvailableException, ServerUnavailableException {
		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(coreService.getExecutorService()).thenReturn(executorService);
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				((Runnable) invocation.getArguments()[0]).run();
				return null;
			}
		}).when(executorService).submit(Mockito.<Runnable> any());

		// method sensor and config
		when(methodSensor.getSensorTypeConfig()).thenReturn(methodSensorTypeConfig);
		// method sensors iterator
		Iterator<IMethodSensor> it = Mockito.mock(Iterator.class);
		when(it.hasNext()).thenReturn(true, false);
		when(it.next()).thenReturn(methodSensor);
		when(methodSensors.iterator()).thenReturn(it);
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
			when(methodInstrumentationConfig.getTargetClassFqn()).thenReturn(className);
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getParameterTypes()).thenReturn(Collections.<String> emptyList());
			when(methodInstrumentationConfig.getSensorInstrumentationPoint()).thenReturn(sensorInstrumentationPoint);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sensorInstrumentationPoint));
			when(sensorInstrumentationPoint.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean())).thenReturn(methodVisitor);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(false);
			when(classHashHelper.isAnalyzed(anyString())).thenReturn(true);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyze(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			long[] sensorIds = { 17L };
			when(sensorInstrumentationPoint.getId()).thenReturn(rscId);
			when(sensorInstrumentationPoint.getSensorIds()).thenReturn(sensorIds);
			when(sensorInstrumentationPoint.isStartsInvocation()).thenReturn(false);
			when(sensorInstrumentationPoint.getSettings()).thenReturn(Collections.<String, Object> singletonMap("key", "value"));
			when(sensorInstrumentationPoint.getPropertyAccessorList()).thenReturn(Collections.<PropertyPathStart> emptyList());
			when(methodSensorTypeConfig.getId()).thenReturn(sensorIds[0]);

			byteCodeAnalyzer.afterPropertiesSet();
			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as instrumentation happened, we get a not null object
			assertThat(instrumentedByteCode, is(not(nullValue())));

			verify(connection, times(2)).isConnected();
			verify(connection, times(1)).analyze(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
			verify(connection, times(1)).instrumentationApplied(captor.capture());
			assertThat(captor.getValue().size(), is(1));
			assertThat((Map<Long, long[]>) captor.getValue(), hasEntry(rscId, sensorIds));
			verify(classHashHelper, atLeastOnce()).isAnalyzed(anyString());
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerInstrumentationDefinition(fqnCaptor.getValue(), instrumentationResult);
			ArgumentCaptor<RegisteredSensorConfig> rscCaptor = ArgumentCaptor.forClass(RegisteredSensorConfig.class);
			verify(hookDispatcherMapper, times(1)).addMapping(eq(rscId), rscCaptor.capture());
			assertThat(rscCaptor.getValue().getId(), is(rscId));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasSize(1));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasItem(methodSensor));
			assertThat(rscCaptor.getValue().getTargetClassFqn(), is(methodInstrumentationConfig.getTargetClassFqn()));
			assertThat(rscCaptor.getValue().getTargetMethodName(), is(methodInstrumentationConfig.getTargetMethodName()));
			assertThat(rscCaptor.getValue().getReturnType(), is(methodInstrumentationConfig.getReturnType()));
			assertThat(rscCaptor.getValue().isStartsInvocation(), is(sensorInstrumentationPoint.isStartsInvocation()));
			assertThat(rscCaptor.getValue().getSettings(), is(sensorInstrumentationPoint.getSettings()));
			assertThat(rscCaptor.getValue().getPropertyAccessorList(), is(sensorInstrumentationPoint.getPropertyAccessorList()));
			verify(coreService, times(1)).getExecutorService();
			verifyNoMoreInteractions(hookDispatcherMapper, connection, classHashHelper, coreService);
		}

		@Test
		public void nullByteCodeAndClassLoaderInstrumentation() throws Exception {
			String className = String.class.getName();

			// make registered sensor config always instrument toString
			when(methodInstrumentationConfig.getTargetClassFqn()).thenReturn(className);
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getParameterTypes()).thenReturn(Collections.<String> emptyList());
			when(methodInstrumentationConfig.getSensorInstrumentationPoint()).thenReturn(sensorInstrumentationPoint);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sensorInstrumentationPoint));
			when(sensorInstrumentationPoint.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean())).thenReturn(methodVisitor);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(false);
			when(classHashHelper.isAnalyzed(anyString())).thenReturn(true);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyze(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			long[] sensorIds = { 17L };
			when(sensorInstrumentationPoint.getId()).thenReturn(rscId);
			when(sensorInstrumentationPoint.getSensorIds()).thenReturn(sensorIds);
			when(sensorInstrumentationPoint.isStartsInvocation()).thenReturn(false);
			when(sensorInstrumentationPoint.getSettings()).thenReturn(Collections.<String, Object> singletonMap("key", "value"));
			when(sensorInstrumentationPoint.getPropertyAccessorList()).thenReturn(Collections.<PropertyPathStart> emptyList());
			when(methodSensorTypeConfig.getId()).thenReturn(sensorIds[0]);

			byteCodeAnalyzer.afterPropertiesSet();
			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(null, className, null);

			// as instrumentation happened, we get a not null object
			assertThat(instrumentedByteCode, is(not(nullValue())));

			verify(connection, times(2)).isConnected();
			verify(connection, times(1)).analyze(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
			verify(connection, times(1)).instrumentationApplied(captor.capture());
			assertThat(captor.getValue().size(), is(1));
			assertThat((Map<Long, long[]>) captor.getValue(), hasEntry(rscId, sensorIds));
			verify(classHashHelper, atLeastOnce()).isAnalyzed(anyString());
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerInstrumentationDefinition(fqnCaptor.getValue(), instrumentationResult);
			ArgumentCaptor<RegisteredSensorConfig> rscCaptor = ArgumentCaptor.forClass(RegisteredSensorConfig.class);
			verify(hookDispatcherMapper, times(1)).addMapping(eq(rscId), rscCaptor.capture());
			assertThat(rscCaptor.getValue().getId(), is(rscId));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasSize(1));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasItem(methodSensor));
			assertThat(rscCaptor.getValue().getTargetClassFqn(), is(methodInstrumentationConfig.getTargetClassFqn()));
			assertThat(rscCaptor.getValue().getTargetMethodName(), is(methodInstrumentationConfig.getTargetMethodName()));
			assertThat(rscCaptor.getValue().getReturnType(), is(methodInstrumentationConfig.getReturnType()));
			assertThat(rscCaptor.getValue().isStartsInvocation(), is(sensorInstrumentationPoint.isStartsInvocation()));
			assertThat(rscCaptor.getValue().getSettings(), is(sensorInstrumentationPoint.getSettings()));
			assertThat(rscCaptor.getValue().getPropertyAccessorList(), is(sensorInstrumentationPoint.getPropertyAccessorList()));
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
			.thenReturn(Collections.<IMethodInstrumentationPoint> singleton(new SpecialInstrumentationPoint(SpecialInstrumentationType.CLASS_LOADING_DELEGATION)));

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(false);
			when(classHashHelper.isAnalyzed(anyString())).thenReturn(true);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyze(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as instrumentation happened, we get a not null object
			assertThat(instrumentedByteCode, is(not(nullValue())));

			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).analyze(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			verify(classHashHelper, atLeastOnce()).isAnalyzed(anyString());
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerInstrumentationDefinition(fqnCaptor.getValue(), instrumentationResult);
			verifyNoMoreInteractions(connection, classHashHelper);
			verifyZeroInteractions(hookDispatcherMapper, coreService);
		}

		@Test
		public void notToBeSentNoInstrumentation() throws IOException {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(true);
			when(classHashHelper.getInstrumentationDefinition(fqnCaptor.capture())).thenReturn(null);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// we did not send the class type
			assertThat(instrumentedByteCode, is(nullValue()));
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verifyZeroInteractions(platformManager, connection, hookDispatcherMapper, coreService);
			// but we asked for the instrumentation result
			verify(classHashHelper, times(1)).getInstrumentationDefinition(fqnCaptor.getValue());
			verifyNoMoreInteractions(classHashHelper);
		}

		@Test
		public void notToBeSentCachedInstrumentation() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			// make registered sensor config always instrument toString
			when(methodInstrumentationConfig.getTargetClassFqn()).thenReturn(className);
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getParameterTypes()).thenReturn(Collections.<String> emptyList());
			when(methodInstrumentationConfig.getSensorInstrumentationPoint()).thenReturn(sensorInstrumentationPoint);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sensorInstrumentationPoint));
			when(sensorInstrumentationPoint.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean())).thenReturn(methodVisitor);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(true);
			when(classHashHelper.getInstrumentationDefinition(fqnCaptor.capture())).thenReturn(instrumentationResult);

			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			long[] sensorIds = { 17L };
			when(sensorInstrumentationPoint.getId()).thenReturn(rscId);
			when(sensorInstrumentationPoint.getSensorIds()).thenReturn(sensorIds);
			when(sensorInstrumentationPoint.isStartsInvocation()).thenReturn(false);
			when(sensorInstrumentationPoint.getSettings()).thenReturn(Collections.<String, Object> singletonMap("key", "value"));
			when(sensorInstrumentationPoint.getPropertyAccessorList()).thenReturn(Collections.<PropertyPathStart> emptyList());
			when(methodSensorTypeConfig.getId()).thenReturn(sensorIds[0]);
			when(connection.isConnected()).thenReturn(true);

			byteCodeAnalyzer.afterPropertiesSet();
			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// we did not send the class type
			assertThat(instrumentedByteCode, is(not(nullValue())));
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			// but we asked for the instrumentation result and instrumented
			verify(classHashHelper, times(1)).getInstrumentationDefinition(fqnCaptor.getValue());
			ArgumentCaptor<RegisteredSensorConfig> rscCaptor = ArgumentCaptor.forClass(RegisteredSensorConfig.class);
			verify(hookDispatcherMapper, times(1)).addMapping(eq(rscId), rscCaptor.capture());
			assertThat(rscCaptor.getValue().getId(), is(rscId));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasSize(1));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasItem(methodSensor));
			assertThat(rscCaptor.getValue().getTargetClassFqn(), is(methodInstrumentationConfig.getTargetClassFqn()));
			assertThat(rscCaptor.getValue().getTargetMethodName(), is(methodInstrumentationConfig.getTargetMethodName()));
			assertThat(rscCaptor.getValue().getReturnType(), is(methodInstrumentationConfig.getReturnType()));
			assertThat(rscCaptor.getValue().isStartsInvocation(), is(sensorInstrumentationPoint.isStartsInvocation()));
			assertThat(rscCaptor.getValue().getSettings(), is(sensorInstrumentationPoint.getSettings()));
			assertThat(rscCaptor.getValue().getPropertyAccessorList(), is(sensorInstrumentationPoint.getPropertyAccessorList()));
			ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).instrumentationApplied(captor.capture());
			assertThat(captor.getValue().size(), is(1));
			assertThat((Map<Long, long[]>) captor.getValue(), hasEntry(rscId, sensorIds));
			verify(coreService, times(1)).getExecutorService();
			verifyNoMoreInteractions(hookDispatcherMapper, connection, classHashHelper, platformManager, coreService);
		}

		@Test
		public void noInstrumentationResult() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(false);
			when(classHashHelper.isAnalyzed(anyString())).thenReturn(true);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyze(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(null);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as no instrumentation happened, we get a null object
			assertThat(instrumentedByteCode, is(nullValue()));

			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).analyze(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			verify(classHashHelper, atLeastOnce()).isAnalyzed(anyString());
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerInstrumentationDefinition(fqnCaptor.getValue(), null);
			verifyZeroInteractions(hookDispatcherMapper, coreService);
			verifyNoMoreInteractions(connection, classHashHelper);
		}

		@Test
		public void noInstrumentationConnectionOffline() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(false);
			when(connection.isConnected()).thenReturn(false);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as no instrumentation happened, we get a null object
			assertThat(instrumentedByteCode, is(nullValue()));

			verify(connection, times(1)).isConnected();
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
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
			when(methodInstrumentationConfig.getSensorInstrumentationPoint()).thenReturn(sensorInstrumentationPoint);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sensorInstrumentationPoint));
			when(sensorInstrumentationPoint.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean())).thenReturn(methodVisitor);

			ArgumentCaptor<String> fqnCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(fqnCaptor.capture(), hashCaptor.capture())).thenReturn(false);
			when(classHashHelper.isAnalyzed(anyString())).thenReturn(true);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyze(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			when(sensorInstrumentationPoint.getId()).thenReturn(rscId);

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as no instrumentation happened, we get a null object
			assertThat(instrumentedByteCode, is(nullValue()));

			verify(connection, times(1)).isConnected();
			verify(connection, times(1)).analyze(platformId.longValue(), hashCaptor.getValue(), classCaptor.getValue());
			verify(classHashHelper, atLeastOnce()).isAnalyzed(anyString());
			verify(classHashHelper, times(1)).isSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerAnalyzed(fqnCaptor.getValue());
			verify(classHashHelper, times(1)).registerSent(fqnCaptor.getValue(), hashCaptor.getValue());
			verify(classHashHelper, times(1)).registerInstrumentationDefinition(fqnCaptor.getValue(), instrumentationResult);
			verifyNoMoreInteractions(connection, classHashHelper);
			verifyZeroInteractions(hookDispatcherMapper, coreService);
		}

		@Test
		public void noInstrumentationMissingClass() throws Exception {
			String className = "someCrazyClass";

			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(null, className, null);

			assertThat(instrumentedByteCode, is(nullValue()));

			verifyZeroInteractions(hookDispatcherMapper, coreService, connection, classHashHelper, configurationStorage, platformManager);
		}

		@Test
		public void instrumentationAnalyzeDependingClasses() throws Exception {
			String className = TestClass.class.getName();
			ClassLoader classLoader = TestClass.class.getClassLoader();
			byte[] byteCode = getByteCode(className);

			// make registered sensor config always instrument toString
			when(methodInstrumentationConfig.getTargetClassFqn()).thenReturn(className);
			when(methodInstrumentationConfig.getTargetMethodName()).thenReturn("<init>");
			when(methodInstrumentationConfig.getReturnType()).thenReturn("void");
			when(methodInstrumentationConfig.getParameterTypes()).thenReturn(Collections.<String> emptyList());
			when(methodInstrumentationConfig.getSensorInstrumentationPoint()).thenReturn(sensorInstrumentationPoint);
			when(methodInstrumentationConfig.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sensorInstrumentationPoint));
			when(sensorInstrumentationPoint.getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean())).thenReturn(methodVisitor);

			ArgumentCaptor<ClassType> classCaptor = ArgumentCaptor.forClass(ClassType.class);
			when(classHashHelper.isSent(anyString(), anyString())).thenReturn(false);
			when(classHashHelper.isAnalyzed(anyString())).thenReturn(true);
			when(classHashHelper.isAnalyzed(AbstractSubTest.class.getName())).thenReturn(false);
			when(connection.isConnected()).thenReturn(true);
			when(connection.analyze(eq(platformId.longValue()), anyString(), classCaptor.capture())).thenReturn(instrumentationResult);
			when(instrumentationResult.getMethodInstrumentationConfigs()).thenReturn(Collections.singleton(methodInstrumentationConfig));
			long rscId = 13L;
			long[] sensorIds = { 17L };
			when(sensorInstrumentationPoint.getId()).thenReturn(rscId);
			when(sensorInstrumentationPoint.getSensorIds()).thenReturn(sensorIds);
			when(sensorInstrumentationPoint.isStartsInvocation()).thenReturn(false);
			when(sensorInstrumentationPoint.getSettings()).thenReturn(Collections.<String, Object> singletonMap("key", "value"));
			when(sensorInstrumentationPoint.getPropertyAccessorList()).thenReturn(Collections.<PropertyPathStart> emptyList());
			when(methodSensorTypeConfig.getId()).thenReturn(sensorIds[0]);

			byteCodeAnalyzer.afterPropertiesSet();
			byte[] instrumentedByteCode = byteCodeAnalyzer.analyzeAndInstrument(byteCode, className, classLoader);

			// as instrumentation happened, we get a not null object
			assertThat(instrumentedByteCode, is(not(nullValue())));

			verify(connection, times(3)).isConnected();
			verify(connection, times(1)).analyze(eq(platformId.longValue()), anyString(), eq(classCaptor.getAllValues().get(0)));
			verify(connection, times(1)).analyze(eq(platformId.longValue()), anyString(), eq(classCaptor.getAllValues().get(1)));
			ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
			verify(connection, times(1)).instrumentationApplied(captor.capture());
			assertThat(captor.getValue().size(), is(1));
			assertThat((Map<Long, long[]>) captor.getValue(), hasEntry(rscId, sensorIds));

			// assert sent classes order
			assertThat(classCaptor.getAllValues().get(0).getFQN(), is(AbstractSubTest.class.getName()));
			assertThat(classCaptor.getAllValues().get(1).getFQN(), is(TestClass.class.getName()));

			// class hash verfications
			verify(classHashHelper, atLeastOnce()).isAnalyzed(anyString());
			verify(classHashHelper, times(1)).isSent(eq(TestClass.class.getName()), anyString());
			verify(classHashHelper, times(1)).isSent(eq(AbstractSubTest.class.getName()), anyString());
			verify(classHashHelper, times(1)).registerAnalyzed(TestClass.class.getName());
			verify(classHashHelper, times(1)).registerAnalyzed(AbstractSubTest.class.getName());
			verify(classHashHelper, times(1)).registerSent(eq(TestClass.class.getName()), anyString());
			verify(classHashHelper, times(1)).registerSent(eq(AbstractSubTest.class.getName()), anyString());
			verify(classHashHelper, times(1)).registerInstrumentationDefinition(TestClass.class.getName(), instrumentationResult);
			verify(classHashHelper, times(1)).registerInstrumentationDefinition(AbstractSubTest.class.getName(), instrumentationResult);

			ArgumentCaptor<RegisteredSensorConfig> rscCaptor = ArgumentCaptor.forClass(RegisteredSensorConfig.class);
			verify(hookDispatcherMapper, times(1)).addMapping(eq(rscId), rscCaptor.capture());
			assertThat(rscCaptor.getValue().getId(), is(rscId));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasSize(1));
			assertThat(rscCaptor.getValue().getMethodSensors(), hasItem(methodSensor));
			assertThat(rscCaptor.getValue().getTargetClassFqn(), is(methodInstrumentationConfig.getTargetClassFqn()));
			assertThat(rscCaptor.getValue().getTargetMethodName(), is(methodInstrumentationConfig.getTargetMethodName()));
			assertThat(rscCaptor.getValue().getReturnType(), is(methodInstrumentationConfig.getReturnType()));
			assertThat(rscCaptor.getValue().isStartsInvocation(), is(sensorInstrumentationPoint.isStartsInvocation()));
			assertThat(rscCaptor.getValue().getSettings(), is(sensorInstrumentationPoint.getSettings()));
			assertThat(rscCaptor.getValue().getPropertyAccessorList(), is(sensorInstrumentationPoint.getPropertyAccessorList()));
			verify(coreService, times(1)).getExecutorService();
			verifyNoMoreInteractions(hookDispatcherMapper, connection, classHashHelper, coreService);
		}

	}
}
