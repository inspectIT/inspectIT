package rocks.inspectit.agent.java.instrumentation.asm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.asm.Type;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.IAgent;
import rocks.inspectit.agent.java.hooking.IHookDispatcher;
import rocks.inspectit.shared.all.instrumentation.asm.ClassLoaderDelegationMethodInstrumenter;
import rocks.inspectit.shared.all.instrumentation.asm.ConstructorInstrumenter;
import rocks.inspectit.shared.all.instrumentation.asm.MethodInstrumenter;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;

/**
 * Tests all instrumenters that we have in connection to the {@link ClassInstrumenter}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ClassInstrumenterTest extends AbstractInstrumentationTest {

	protected static final String TEST_CLASS_FQN = "rocks.inspectit.agent.java.instrumentation.asm.InstrumentationTestClass";

	protected static final String EXCEPTION_TEST_CLASS_FQN = "rocks.inspectit.agent.java.instrumentation.asm.InstrumentationExceptionTestClass";

	protected static final String TEST_CLASS_LOADER_FQN = "rocks.inspectit.agent.java.instrumentation.asm.MyTestClassLoader";

	protected static final Answer<MethodVisitor> METHOD_INSTRUMENTER_ANSWER = new Answer<MethodVisitor>() {

		public MethodVisitor answer(InvocationOnMock invocation) throws Throwable {
			Object[] arguments = invocation.getArguments();
			SensorInstrumentationPoint sip = (SensorInstrumentationPoint) invocation.getMock();
			return getMethodInstrumenter((MethodVisitor) arguments[0], (Integer) arguments[1], (String) arguments[2], (String) arguments[3], sip.getId(), (Boolean) arguments[4]);
		}
	};

	protected static final Answer<MethodVisitor> CONSTRUCTOR_INSTRUMENTER_ANSWER = new Answer<MethodVisitor>() {

		public MethodVisitor answer(InvocationOnMock invocation) throws Throwable {
			Object[] arguments = invocation.getArguments();
			SensorInstrumentationPoint sip = (SensorInstrumentationPoint) invocation.getMock();
			return getConstructorInstrumenter((MethodVisitor) arguments[0], (Integer) arguments[1], (String) arguments[2], (String) arguments[3], sip.getId(), (Boolean) arguments[4]);
		}
	};

	protected static final Answer<MethodVisitor> CLASS_LOADING_DELEGATION_INSTRUMENTER_ANSWER = new Answer<MethodVisitor>() {

		public MethodVisitor answer(InvocationOnMock invocation) throws Throwable {
			Object[] arguments = invocation.getArguments();
			return getClassLoaderDelegationMethodInstrumenter((MethodVisitor) arguments[0], (Integer) arguments[1], (String) arguments[2], (String) arguments[3]);
		}
	};

	public static IHookDispatcher dispatcher;

	public static IAgent a;

	@Mock
	IHookDispatcher hookDispatcher;

	@Mock
	IAgent agent;

	@Mock
	SensorInstrumentationPoint sip;

	@Mock
	SensorInstrumentationPoint sip2;

	@Mock
	MethodInstrumentationConfig config;

	@Mock
	MethodInstrumentationConfig config2;

	ClassInstrumenter classInstrumenter;

	LoaderAwareClassWriter classWriter;

	@BeforeMethod
	public void init() {
		dispatcher = hookDispatcher;
		a = agent;
	}

	public class Instrument extends ClassInstrumenterTest {

		// no instrumentation
		@Test
		public void noInstrumenatation() throws Exception {
			String methodName = "stringNullParameter";
			long methodId = 3L;

			when(sip.getId()).thenReturn(methodId);
			when(config.getTargetMethodName()).thenReturn("nonExistingMethod");

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(false));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			this.callMethod(testClass, methodName, null);

			verifyZeroInteractions(hookDispatcher);
		}

		// return, params, static

		@Test
		public void methodHookNoStatic() throws Exception {
			String methodName = "stringNullParameter";
			long methodId = 3L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void methodHookStatic() throws Exception {
			String methodName = "voidNullParameterStatic";
			long methodId = 7L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringNullParameter() throws Exception {
			String methodName = "stringNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], "stringNullParameter");
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intNullParameter() throws Exception {
			String methodName = "intNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], 3);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void doubleNullParameter() throws Exception {
			String methodName = "doubleNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], 5.3D);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], 5.3D);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void floatNullParameter() throws Exception {
			String methodName = "floatNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], Float.MAX_VALUE);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], Float.MAX_VALUE);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void byteNullParameter() throws Exception {
			String methodName = "byteNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], (byte) 127);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], (byte) 127);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void shortNullParameter() throws Exception {
			String methodName = "shortNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], (short) 16345);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], (short) 16345);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void booleanNullParameter() throws Exception {
			String methodName = "booleanNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], false);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], false);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void charNullParameter() throws Exception {
			String methodName = "charNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], '\u1234');
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], '\u1234');
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void voidNullParameterStatic() throws Exception {
			String methodName = "voidNullParameterStatic";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringNullParameterStatic() throws Exception {
			String methodName = "stringNullParameterStatic";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], "stringNullParameterStatic");
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], "stringNullParameterStatic");
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void voidOneParameter() throws Exception {
			String methodName = "voidOneParameter";
			Object[] parameters = { "java.lang.String" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName, String.class);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, parameters);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringOneParameter() throws Exception {
			String methodName = "stringOneParameter";
			Object[] parameters = { "java.lang.String" };
			long methodId = 9L;


			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName, String.class);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, parameters);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, "stringOneParameter");
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, "stringOneParameter");
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void voidTwoParameters() throws Exception {
			String methodName = "voidTwoParameters";
			Object[] parameters = { "java.lang.String", "java.lang.Object" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName, String.class, Object.class);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, parameters);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void mixedTwoParameters() throws Exception {
			String methodName = "mixedTwoParameters";
			Object[] parameters = { "int", "boolean" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName, int.class, boolean.class);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, parameters);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intArrayNullParameter() throws Exception {
			String methodName = "intArrayNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], new int[] { 1, 2, 3 });
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], new int[] { 1, 2, 3 });
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringArrayNullParameter() throws Exception {
			String methodName = "stringArrayNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], new String[] { "test123", "bla" });
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], new String[] { "test123", "bla" });
			verifyNoMoreInteractions(hookDispatcher);
		}

		// exception no enhanced

		@Test
		public void unexpectedExceptionTrowingNoEnhanced() throws Exception {
			String methodName = "unexpectedExceptionThrowing";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			try {
				this.callMethod(testClass, methodName, null);
			} catch (Throwable t) {
			}

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void unexpectedExceptionNotTrowingNoEnhanced() throws Exception {
			String methodName = "unexpectedExceptionNotThrowing";
			Object[] parameters = { "java.lang.Object" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName, Object.class);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			try {
				this.callMethod(testClass, methodName, parameters);
			} catch (Throwable t) {
			}

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, parameters);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, parameters, null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, parameters, null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void exceptionHandledResultReturned() throws Exception {
			String methodName = "exceptionHandledResultReturned";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], 3);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		// constructors

		@Test
		public void staticConstructor() throws Exception {
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockConstructor(config, InstrumentationTestClass.class, true);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// create instance
			this.createInstance(TEST_CLASS_FQN, b);

			verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, new Object[0]);
			verify(hookDispatcher).dispatchConstructorAfterBody(methodId, null, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void constructorNullParameter() throws Exception {
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockConstructor(config, InstrumentationTestClass.class, false);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// create instance
			Object instance = this.createInstance(TEST_CLASS_FQN, b);

			verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, new Object[0]);
			verify(hookDispatcher).dispatchConstructorAfterBody(methodId, instance, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void constructorStringOneParameter() throws Exception {
			Object[] parameters = { "java.lang.String" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockConstructor(config, InstrumentationTestClass.class, false, String.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			Class<?> clazz = createClass(TEST_CLASS_FQN, b);
			Constructor<?> constructor = clazz.getConstructor(new Class[] { String.class });
			Object instance = constructor.newInstance(parameters);

			verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, parameters);
			verify(hookDispatcher).dispatchConstructorAfterBody(methodId, instance, parameters);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void nestedConstructorBooleanOneParameter() throws Exception {
			Object[] parameters = { Boolean.TRUE };
			Object[] nestedParameters = { "delegate" };
			long methodId = 9L;
			long nestedMethodId = 13L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockConstructor(config, InstrumentationTestClass.class, false, boolean.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			when(sip2.getId()).thenReturn(nestedMethodId);
			prepareConfigurationMockConstructor(config2, InstrumentationTestClass.class, false, String.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip2).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config2.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip2));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config, config2);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			Class<?> clazz = createClass(TEST_CLASS_FQN, b);
			Constructor<?> constructor = clazz.getConstructor(new Class[] { Boolean.TYPE });
			Object testClass = constructor.newInstance(parameters);

			verify(hookDispatcher).dispatchConstructorBeforeBody(nestedMethodId, nestedParameters);
			verify(hookDispatcher).dispatchConstructorAfterBody(nestedMethodId, testClass, nestedParameters);

			verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, parameters);
			verify(hookDispatcher).dispatchConstructorAfterBody(methodId, testClass, parameters);
			verifyNoMoreInteractions(hookDispatcher);
		}

		// constructor exception no enhanced

		@Test
		public void constructorUnexpectedExceptionTrowingNoEnhanced() throws Exception {
			Object[] parameters = { 3 };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockConstructor(config, InstrumentationTestClass.class, false, int.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			Class<?> clazz = createClass(TEST_CLASS_FQN, b);
			Constructor<?> constructor = clazz.getConstructor(new Class[] { int.class });
			try {
				constructor.newInstance(parameters);
			} catch (Throwable t) {

			}

			verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, parameters);
			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchConstructorAfterBody(eq(methodId), captor.capture(), eq(parameters));

			assertThat(captor.getValue().getClass().getName(), is(TEST_CLASS_FQN));
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void constructorUnexpectedExceptionNotTrowingNoEnhanced() throws Exception {
			Object[] parameters = { "test" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockConstructor(config, InstrumentationTestClass.class, false, Object.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			Class<?> clazz = createClass(TEST_CLASS_FQN, b);
			Constructor<?> constructor = clazz.getConstructor(new Class[] { Object.class });
			try {
				constructor.newInstance(parameters);
			} catch (Throwable t) {

			}

			verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, parameters);
			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchConstructorAfterBody(eq(methodId), captor.capture(), eq(parameters));

			assertThat(captor.getValue().getClass().getName(), is(TEST_CLASS_FQN));

			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void constructorExceptionHandledResultReturned() throws Exception {
			Object[] parameters = { 11L };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockConstructor(config, InstrumentationTestClass.class, false, long.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			Class<?> clazz = createClass(TEST_CLASS_FQN, b);
			Constructor<?> constructor = clazz.getConstructor(new Class[] { long.class });
			Object instance = constructor.newInstance(parameters);

			verify(hookDispatcher).dispatchConstructorBeforeBody(methodId, parameters);
			verify(hookDispatcher).dispatchConstructorAfterBody(methodId, instance, parameters);
			verifyNoMoreInteractions(hookDispatcher);
		}

		// exception enhanced

		@Test
		public void exceptionThrowerIsInstrumented() throws Exception {
			String methodName = "throwsAndHandlesException";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationExceptionTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(EXCEPTION_TEST_CLASS_FQN);
			prepareWriter(cr, null, true, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(EXCEPTION_TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);

			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void exceptionThrowerIsInstrumentedWhenConstructor() throws Exception {
			Object[] params = { "test" };
			long constructorId = 11L;

			when(sip.getId()).thenReturn(constructorId);
			prepareConfigurationMockConstructor(config, InstrumentationExceptionTestClass.class, false, String.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(EXCEPTION_TEST_CLASS_FQN);
			prepareWriter(cr, null, true, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			Class<?> clazz = createClass(EXCEPTION_TEST_CLASS_FQN, b);
			Constructor<?> constructor = clazz.getConstructor(new Class[] { String.class });
			Object instance = constructor.newInstance(params);

			verify(hookDispatcher).dispatchConstructorBeforeBody(constructorId, params);
			verify(hookDispatcher).dispatchConstructorAfterBody(constructorId, instance, params);
			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchConstructorBeforeCatch(eq(constructorId), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));

			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void exceptionThrowerAndHandlerAreInstrumented() throws Exception {
			String methodName = "callsMethodWithException";
			String innerMethodName = "throwsAnException";
			long methodId = 9L;
			long innerMethodId = 11L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationExceptionTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			when(sip2.getId()).thenReturn(innerMethodId);
			prepareConfigurationMockMethod(config2, InstrumentationExceptionTestClass.class, innerMethodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip2).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config2.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip2));

			ClassReader cr = new ClassReader(EXCEPTION_TEST_CLASS_FQN);
			prepareWriter(cr, null, true, config, config2);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(EXCEPTION_TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			// first method
			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);

			// inner method
			verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, testClass, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, testClass, new Object[0], null);

			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), eq(testClass), (Object[]) anyObject(), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));

			verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void exceptionThrowerAndHandlerAreInstrumentedStatic() throws Exception {
			String methodName = "callsStaticMethodWithException";
			String innerMethodName = "staticThrowsAnException";
			long methodId = 9L;
			long innerMethodId = 11L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationExceptionTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			when(sip2.getId()).thenReturn(innerMethodId);
			prepareConfigurationMockMethod(config2, InstrumentationExceptionTestClass.class, innerMethodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip2).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config2.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip2));

			ClassReader cr = new ClassReader(EXCEPTION_TEST_CLASS_FQN);
			prepareWriter(cr, null, true, config, config2);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(EXCEPTION_TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			// first method
			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, null, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, null, new Object[0], null);

			// inner method
			verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, null, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, null, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, null, new Object[0], null);

			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), eq(null), (Object[]) anyObject(), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));

			verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void exceptionMethodThrowingConstructorPassing() throws Exception {
			Object[] params = new Object[] { 3 };
			String innerMethodName = "throwsAnException";
			long innerMethodId = 9L;
			long constructorId = 11L;

			when(sip.getId()).thenReturn(constructorId);
			prepareConfigurationMockConstructor(config, InstrumentationExceptionTestClass.class, false, int.class);
			doAnswer(CONSTRUCTOR_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			when(sip2.getId()).thenReturn(innerMethodId);
			prepareConfigurationMockMethod(config2, InstrumentationExceptionTestClass.class, innerMethodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip2).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config2.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip2));

			ClassReader cr = new ClassReader(EXCEPTION_TEST_CLASS_FQN);
			prepareWriter(cr, null, true, config, config2);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Class<?> clazz = createClass(EXCEPTION_TEST_CLASS_FQN, b);
			Constructor<?> constructor = clazz.getConstructor(new Class[] { int.class });
			try {
				constructor.newInstance(params);
			} catch (Throwable t) {
			}

			// first method
			verify(hookDispatcher).dispatchConstructorBeforeBody(constructorId, params);
			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchConstructorAfterBody(eq(constructorId), captor.capture(), eq(params));

			assertThat(captor.getValue().getClass().getName(), is(EXCEPTION_TEST_CLASS_FQN));

			// inner method
			verify(hookDispatcher).dispatchMethodBeforeBody(eq(innerMethodId), anyObject(), eq(new Object[0]));
			verify(hookDispatcher).dispatchFirstMethodAfterBody(eq(innerMethodId), anyObject(), eq(new Object[0]), eq(null));
			verify(hookDispatcher).dispatchSecondMethodAfterBody(eq(innerMethodId), anyObject(), eq(new Object[0]), eq(null));

			verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), anyObject(), eq(new Object[0]), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));

			verify(hookDispatcher).dispatchConstructorOnThrowInBody(eq(constructorId), anyObject(), eq(params), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void callsMethodWithExceptionAndTryCatchFinally() throws Exception {
			String methodName = "callsMethodWithExceptionAndTryCatchFinally";
			String innerMethodName = "throwsAnException";
			long methodId = 9L;
			long innerMethodId = 11L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationExceptionTestClass.class, methodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			when(sip2.getId()).thenReturn(innerMethodId);
			prepareConfigurationMockMethod(config2, InstrumentationExceptionTestClass.class, innerMethodName);
			doAnswer(METHOD_INSTRUMENTER_ANSWER).when(sip2).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config2.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip2));

			ClassReader cr = new ClassReader(EXCEPTION_TEST_CLASS_FQN);
			prepareWriter(cr, null, true, config, config2);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(EXCEPTION_TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(methodId, testClass, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(methodId, testClass, new Object[0], null);

			ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchBeforeCatch(eq(methodId), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));

			verify(hookDispatcher).dispatchMethodBeforeBody(innerMethodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchFirstMethodAfterBody(innerMethodId, testClass, new Object[0], null);
			verify(hookDispatcher).dispatchSecondMethodAfterBody(innerMethodId, testClass, new Object[0], null);

			captor = ArgumentCaptor.forClass(Object.class);
			verify(hookDispatcher).dispatchOnThrowInBody(eq(innerMethodId), eq(testClass), (Object[]) anyObject(), captor.capture());
			assertThat(captor.getValue().getClass().getName(), is(equalTo(MyTestException.class.getName())));

			verifyNoMoreInteractions(hookDispatcher);
		}

		// class loader delegation

		@Test
		public void classLoadingDelegationActiveLoadClass() throws Exception {
			Class<?> clazz = getClass();
			Object[] parameters = { "java.lang.String" };
			String methodName = "loadClass";

			doReturn(clazz).when(agent).loadClass(parameters);

			prepareConfigurationMockMethod(config, MyTestClassLoader.class, methodName, String.class);
			SpecialInstrumentationPoint functionalInstrumentationPoint = mock(SpecialInstrumentationPoint.class);
			doAnswer(CLASS_LOADING_DELEGATION_INSTRUMENTER_ANSWER).when(functionalInstrumentationPoint).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(),
					anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(functionalInstrumentationPoint));

			ClassReader cr = new ClassReader(TEST_CLASS_LOADER_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_LOADER_FQN, b);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			Class<?> result = (Class<?>) this.callMethod(testClass, methodName, parameters);

			assertThat((Object) result, is(equalTo((Object) clazz)));

			verify(agent, times(1)).loadClass(parameters);
			verifyNoMoreInteractions(agent);
		}

		@Test
		public void classLoadingDelegationActiveDoesNotLoadClass() throws Exception {
			Object[] parameters = { "java.lang.String" };
			String methodName = "loadClass";

			doReturn(null).when(agent).loadClass(parameters);

			prepareConfigurationMockMethod(config, MyTestClassLoader.class, methodName, String.class);
			SpecialInstrumentationPoint functionalInstrumentationPoint = mock(SpecialInstrumentationPoint.class);
			doAnswer(CLASS_LOADING_DELEGATION_INSTRUMENTER_ANSWER).when(functionalInstrumentationPoint).getMethodVisitor(Mockito.<MethodVisitor> any(), anyInt(), anyString(), anyString(),
					anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(functionalInstrumentationPoint));

			ClassReader cr = new ClassReader(TEST_CLASS_LOADER_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_LOADER_FQN, b);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			Class<?> result = (Class<?>) this.callMethod(testClass, methodName, parameters);

			// it's delegated to super class loader so we should get the String class back
			assertThat((Object) result, is(equalTo((Object) String.class)));

			verify(agent, times(1)).loadClass(parameters);
			verifyNoMoreInteractions(agent);
		}

	}

	protected void prepareWriter(ClassReader cr, ClassLoader classLoader, boolean enhancedExceptionSensor, MethodInstrumentationConfig... configs) {
		classWriter = new LoaderAwareClassWriter(cr, ClassWriter.COMPUTE_FRAMES, classLoader);
		classInstrumenter = new ClassInstrumenter(classWriter, Arrays.asList(configs), enhancedExceptionSensor);
	}

	protected static MethodInstrumenter getMethodInstrumenter(MethodVisitor superMethodVisitor, int access, String name, String desc, long id, boolean enhancedExceptionSensor) {
		return new MethodInstrumenter(superMethodVisitor, access, name, desc, id, enhancedExceptionSensor) {
			@Override
			protected void loadHookDispatcher() {
				mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(ClassInstrumenterTest.class), "dispatcher", Type.getDescriptor(IHookDispatcher.class));
			}
		};
	}

	protected static ConstructorInstrumenter getConstructorInstrumenter(MethodVisitor superMethodVisitor, int access, String name, String desc, long id, boolean enhancedExceptionSensor) {
		return new ConstructorInstrumenter(superMethodVisitor, access, name, desc, id, enhancedExceptionSensor) {
			@Override
			protected void loadHookDispatcher() {
				mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(ClassInstrumenterTest.class), "dispatcher", Type.getDescriptor(IHookDispatcher.class));
			}
		};
	}

	protected static ClassLoaderDelegationMethodInstrumenter getClassLoaderDelegationMethodInstrumenter(MethodVisitor superMethodVisitor, int access, String name, String desc) {
		return new ClassLoaderDelegationMethodInstrumenter(superMethodVisitor, access, name, desc) {
			@Override
			protected void loadAgent() {
				mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(ClassInstrumenterTest.class), "a", Type.getDescriptor(IAgent.class));
			}
		};
	}

	protected void prepareConfigurationMockMethod(MethodInstrumentationConfig point, Class<?> clazz, String methodName, Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException {
		Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
		when(point.getTargetClassFqn()).thenReturn(clazz.getName());
		when(point.getTargetMethodName()).thenReturn(methodName);
		if (method.getReturnType().isArray()) {
			when(point.getReturnType()).thenReturn(method.getReturnType().getComponentType().getName() + "[]");
		} else {
			when(point.getReturnType()).thenReturn(method.getReturnType().getName());
		}
		if (ArrayUtils.isNotEmpty(parameterTypes)) {
			List<String> params = new ArrayList<String>();
			for (Class<?> paramType : parameterTypes) {
				if (paramType.isArray()) {
					params.add(paramType.getComponentType().getName() + "[]");
				} else {
					params.add(paramType.getName());
				}
			}
			when(point.getParameterTypes()).thenReturn(params);
		}
	}

	protected void prepareConfigurationMockConstructor(MethodInstrumentationConfig point, Class<?> clazz, boolean staticConstructor, Class<?>... parameterTypes)
			throws SecurityException, NoSuchMethodException {
		clazz.getDeclaredConstructor(parameterTypes);
		when(point.getTargetClassFqn()).thenReturn(clazz.getName());
		when(point.getTargetMethodName()).thenReturn(staticConstructor ? "<clinit>" : "<init>");
		when(point.getReturnType()).thenReturn("void");
		if (ArrayUtils.isNotEmpty(parameterTypes)) {
			List<String> params = new ArrayList<String>();
			for (Class<?> paramType : parameterTypes) {
				if (paramType.isArray()) {
					params.add(paramType.getComponentType().getName() + "[]");
				} else {
					params.add(paramType.getName());
				}
			}
			when(point.getParameterTypes()).thenReturn(params);
		}
	}

}
