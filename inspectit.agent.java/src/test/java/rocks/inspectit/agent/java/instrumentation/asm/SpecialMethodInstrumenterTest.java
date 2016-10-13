package rocks.inspectit.agent.java.instrumentation.asm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.asm.Type;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.IAgent;
import rocks.inspectit.agent.java.hooking.IHookDispatcher;
import rocks.inspectit.agent.java.instrumentation.InstrumenterFactory;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class SpecialMethodInstrumenterTest extends AbstractInstrumentationTest {

	protected static final String TEST_CLASS_FQN = "rocks.inspectit.agent.java.instrumentation.asm.InstrumentationTestClass";

	protected static final Answer<MethodVisitor> SPECIAL_INSTRUMENTER_ANSWER = new Answer<MethodVisitor>() {

		@Override
		public MethodVisitor answer(InvocationOnMock invocation) throws Throwable {
			Object[] arguments = invocation.getArguments();
			SpecialInstrumentationPoint sip = (SpecialInstrumentationPoint) arguments[0];
			return getSpecialMethodInstrumenter((MethodVisitor) arguments[1], (Integer) arguments[2], (String) arguments[3], (String) arguments[4], sip.getId());
		}
	};

	public static IHookDispatcher dispatcher;

	public static IAgent a;

	@Mock
	IHookDispatcher hookDispatcher;

	@Mock
	IAgent agent;

	@Mock
	InstrumenterFactory instrumenterFactory;

	@Mock
	SpecialInstrumentationPoint sip;

	@Mock
	SpecialInstrumentationPoint sip2;

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

	public class Instrument extends SpecialMethodInstrumenterTest {

		// static

		@Test
		public void hookStaticVoid() throws Exception {
			String methodName = "voidNullParameterStatic";
			long methodId = 7L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
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

			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, null, new Object[0], null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void hookStaticVoidResultOnBefore() throws Exception {
			String methodName = "voidNullParameterStatic";
			Object returnValue = "returnValue";
			long methodId = 7L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, null, new Object[0])).thenReturn(returnValue);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, null, new Object[0], null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void hookStaticVoidResultOnAfter() throws Exception {
			String methodName = "voidNullParameterStatic";
			Object returnValue = "returnValue";
			long methodId = 7L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, null, new Object[0], null)).thenReturn(returnValue);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			this.callMethod(testClass, methodName, null);

			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, null, new Object[0], null);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringNullParameterStatic() throws Exception {
			String methodName = "stringNullParameterStatic";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) methodName));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, null, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringNullParameterStaticResultOnBefore() throws Exception {
			String methodName = "stringNullParameterStatic";
			Object returnValue = "returnValue";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, null, new Object[0])).thenReturn(returnValue);

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is(returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringNullParameterStaticResultOnBeforeWrong() throws Exception {
			String methodName = "stringNullParameterStatic";
			Object returnValue = true;
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, null, new Object[0])).thenReturn(returnValue);

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) methodName));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, null, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringNullParameterStaticResultOnAfter() throws Exception {
			String methodName = "stringNullParameterStatic";
			Object returnValue = "returnValue";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, null, new Object[0], methodName)).thenReturn(returnValue);

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is(returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, null, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringNullParameterStaticResultOnAfterWrong() throws Exception {
			String methodName = "stringNullParameterStatic";
			Object returnValue = true;
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, null, new Object[0], methodName)).thenReturn(returnValue);

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) methodName));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, null, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, null, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}


		// non static

		@Test
		public void hookNoStatic() throws Exception {
			String methodName = "stringNullParameter";
			long methodId = 3L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
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
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) methodName));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void hookNoStaticResultOnBefore() throws Exception {
			String methodName = "stringNullParameter";
			Object returnValue = "returnValue";
			long methodId = 3L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is(returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void hookNoStaticResultOnBeforeWrong() throws Exception {
			String methodName = "stringNullParameter";
			Object returnValue = Boolean.valueOf(false);
			long methodId = 3L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) methodName));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void hookNoStaticResultOnAfter() throws Exception {
			String methodName = "stringNullParameter";
			Object returnValue = "returnValue";
			long methodId = 3L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], methodName)).thenReturn(returnValue);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is(returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void hookNoStaticResultOnAfterWrong() throws Exception {
			String methodName = "stringNullParameter";
			Object returnValue = Long.valueOf(1L);
			long methodId = 3L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], methodName)).thenReturn(returnValue);
			// call this method via reflection as we would get a class cast
			// exception by casting to the concrete class.
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) methodName));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], methodName);
			verifyNoMoreInteractions(hookDispatcher);
		}

		// primitive

		@Test
		public void intNullParameter() throws Exception {
			String methodName = "intNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Integer.valueOf(3)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intNullParameterResultOnBefore() throws Exception {
			String methodName = "intNullParameter";
			Object returnValue = Integer.valueOf(11);
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is(returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intNullParameterResultOnBeforeOtherPrimitive() throws Exception {
			String methodName = "intNullParameter";
			Double returnValue = Double.valueOf(4552236.22121D);
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Integer.valueOf(3)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intNullParameterResultOnBeforeObject() throws Exception {
			String methodName = "intNullParameter";
			Object returnValue = "returnValue";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Integer.valueOf(3)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intNullParameterResultOnAfter() throws Exception {
			String methodName = "intNullParameter";
			Object returnValue = Integer.valueOf(11);
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3)).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is(returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intNullParameterResultOnAfterOtherPrimitive() throws Exception {
			String methodName = "intNullParameter";
			Double returnValue = Double.valueOf(4552236.22121D);
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3)).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Integer.valueOf(3)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intNullParameterResultOnAfterObject() throws Exception {
			String methodName = "intNullParameter";
			Object returnValue = "returnValue";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3)).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Integer.valueOf(3)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 3);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void doubleNullParameter() throws Exception {
			String methodName = "doubleNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Double.valueOf(5.3D)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 5.3D);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void doubleNullParameterResultOnBefore() throws Exception {
			String methodName = "doubleNullParameter";
			Double returnValue = Double.valueOf(45223.412D);
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void doubleNullParameterResultOnBeforeOtherPrimitive() throws Exception {
			String methodName = "doubleNullParameter";
			Float returnValue = Float.valueOf(45223.412F);
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Double.valueOf(5.3D)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 5.3D);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void doubleNullParameterResultOnAfterObject() throws Exception {
			String methodName = "doubleNullParameter";
			Object returnValue = "whatever";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 5.3D)).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) Double.valueOf(5.3D)));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], 5.3D);
			verifyNoMoreInteractions(hookDispatcher);
		}

		// array

		@Test
		public void intArrayNullParameter() throws Exception {
			String methodName = "intArrayNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) new int[] { 1, 2, 3 }));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], new int[] { 1, 2, 3 });
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intArrayNullParameterResultBefore() throws Exception {
			String methodName = "intArrayNullParameter";
			int[] returnValue = new int[] {4, 5, 6, 7, 8, 9};
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void intArrayNullParameterResultBeforeWrongArray() throws Exception {
			String methodName = "intArrayNullParameter";
			long[] returnValue = new long[] { 4, 5, 6, 7, 8, 9 };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0])).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) new int[] { 1, 2, 3 }));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], new int[] { 1, 2, 3 });
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringArrayNullParameter() throws Exception {
			String methodName = "stringArrayNullParameter";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) new String[] { "test123", "bla" }));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], new String[] { "test123", "bla" });
			verifyNoMoreInteractions(hookDispatcher);
		}

		@Test
		public void stringArrayNullParameterResultOnAfter() throws Exception {
			String methodName = "stringArrayNullParameter";
			String[] returnValue = new String[] { "something", "totally", "sexy" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			when(hookDispatcher.dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], new String[] { "test123", "bla" })).thenReturn(returnValue);
			Object result = this.callMethod(testClass, methodName, null);

			assertThat(result, is((Object) returnValue));
			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, new Object[0], new String[] { "test123", "bla" });
			verifyNoMoreInteractions(hookDispatcher);
		}

		// parameters

		@Test
		public void stringOneParameter() throws Exception {
			String methodName = "stringOneParameter";
			Object[] parameters = { "java.lang.String" };
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName, String.class);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
			when(config.getAllInstrumentationPoints()).thenReturn(Collections.<IMethodInstrumentationPoint> singleton(sip));

			ClassReader cr = new ClassReader(TEST_CLASS_FQN);
			prepareWriter(cr, null, false, config);
			cr.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
			assertThat(classInstrumenter.isByteCodeAdded(), is(true));
			byte b[] = classWriter.toByteArray();

			// now call this method
			Object testClass = this.createInstance(TEST_CLASS_FQN, b);
			this.callMethod(testClass, methodName, parameters);

			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, parameters);
			verify(hookDispatcher).dispatchSpecialMethodAfterBody(methodId, testClass, parameters, "stringOneParameter");
			verifyNoMoreInteractions(hookDispatcher);
		}

		// exception

		@Test
		public void unexpectedExceptionTrowing() throws Exception {
			String methodName = "unexpectedExceptionThrowing";
			long methodId = 9L;

			when(sip.getId()).thenReturn(methodId);
			prepareConfigurationMockMethod(config, InstrumentationTestClass.class, methodName);
			doAnswer(SPECIAL_INSTRUMENTER_ANSWER).when(instrumenterFactory).getMethodVisitor(eq(sip), Matchers.<MethodVisitor> any(), anyInt(), anyString(), anyString(), anyBoolean());
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
			} catch (Throwable t) { // NOPMD
			}

			verify(hookDispatcher).dispatchSpecialMethodBeforeBody(methodId, testClass, new Object[0]);
			verifyNoMoreInteractions(hookDispatcher);
		}
	}

	protected static SpecialMethodInstrumenter getSpecialMethodInstrumenter(MethodVisitor superMethodVisitor, int access, String name, String desc, long id) {
		return new SpecialMethodInstrumenter(superMethodVisitor, access, name, desc, id) {
			@Override
			protected void loadHookDispatcher() {
				mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(SpecialMethodInstrumenterTest.class), "dispatcher", Type.getDescriptor(IHookDispatcher.class));
			}
		};
	}

	protected void prepareWriter(ClassReader cr, ClassLoader classLoader, boolean enhancedExceptionSensor, MethodInstrumentationConfig... configs) {
		classWriter = new LoaderAwareClassWriter(cr, ClassWriter.COMPUTE_FRAMES, classLoader);
		classInstrumenter = new ClassInstrumenter(instrumenterFactory, classWriter, Arrays.asList(configs), enhancedExceptionSensor);
	}

}
