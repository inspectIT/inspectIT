package rocks.inspectit.agent.java.instrumentation.asm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.instrumentation.InstrumenterFactory;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class InstrumenterFactoryTest extends TestBase {

	@InjectMocks
	InstrumenterFactory factory;

	@Mock
	SensorInstrumentationPoint sensorInstrumentationPoint;

	@Mock
	SpecialInstrumentationPoint specialInstrumentationPoint;

	@Mock
	MethodVisitor superMethodVisitor;

	public static class GetMethodVisitor extends InstrumenterFactoryTest {

		@Test
		public void method() {
			long id = 7L;
			String name = "method";
			String desc = "()V";
			boolean enhancedExceptionSensor = false;
			when(sensorInstrumentationPoint.isConstructor()).thenReturn(false);
			when(sensorInstrumentationPoint.getId()).thenReturn(id);

			MethodVisitor methodVisitor = factory.getMethodVisitor(sensorInstrumentationPoint, superMethodVisitor, 0, name, desc, enhancedExceptionSensor);

			assertThat(methodVisitor, is(instanceOf(MethodInstrumenter.class)));
			MethodInstrumenter methodInstrumenter = (MethodInstrumenter) methodVisitor;
			assertThat(methodInstrumenter.getMethodId(), is(id));
			assertThat(methodInstrumenter.isEnhancedExceptionSensor(), is(enhancedExceptionSensor));
		}

		@Test
		public void methodEnchancedExceptionSensor() {
			long id = 7L;
			String name = "method";
			String desc = "()V";
			boolean enhancedExceptionSensor = true;
			when(sensorInstrumentationPoint.isConstructor()).thenReturn(false);
			when(sensorInstrumentationPoint.getId()).thenReturn(id);

			MethodVisitor methodVisitor = factory.getMethodVisitor(sensorInstrumentationPoint, superMethodVisitor, 0, name, desc, enhancedExceptionSensor);

			assertThat(methodVisitor, is(instanceOf(MethodInstrumenter.class)));
			MethodInstrumenter methodInstrumenter = (MethodInstrumenter) methodVisitor;
			assertThat(methodInstrumenter.getMethodId(), is(id));
			assertThat(methodInstrumenter.isEnhancedExceptionSensor(), is(enhancedExceptionSensor));
		}

		@Test
		public void constructor() {
			long id = 7L;
			String name = "method";
			String desc = "()V";
			boolean enhancedExceptionSensor = false;
			when(sensorInstrumentationPoint.isConstructor()).thenReturn(true);
			when(sensorInstrumentationPoint.getId()).thenReturn(id);

			MethodVisitor methodVisitor = factory.getMethodVisitor(sensorInstrumentationPoint, superMethodVisitor, 0, name, desc, enhancedExceptionSensor);

			assertThat(methodVisitor, is(instanceOf(ConstructorInstrumenter.class)));
			ConstructorInstrumenter methodInstrumenter = (ConstructorInstrumenter) methodVisitor;
			assertThat(methodInstrumenter.getMethodId(), is(id));
			assertThat(methodInstrumenter.isEnhancedExceptionSensor(), is(enhancedExceptionSensor));
		}

		@Test
		public void constructorEnchancedExceptionSensor() {
			long id = 7L;
			String name = "method";
			String desc = "()V";
			boolean enhancedExceptionSensor = true;
			when(sensorInstrumentationPoint.isConstructor()).thenReturn(true);
			when(sensorInstrumentationPoint.getId()).thenReturn(id);

			MethodVisitor methodVisitor = factory.getMethodVisitor(sensorInstrumentationPoint, superMethodVisitor, 0, name, desc, enhancedExceptionSensor);

			assertThat(methodVisitor, is(instanceOf(ConstructorInstrumenter.class)));
			ConstructorInstrumenter methodInstrumenter = (ConstructorInstrumenter) methodVisitor;
			assertThat(methodInstrumenter.getMethodId(), is(id));
			assertThat(methodInstrumenter.isEnhancedExceptionSensor(), is(enhancedExceptionSensor));
		}

		@Test
		public void specialMethod() {
			long id = 7L;
			String name = "method";
			String desc = "()V";
			boolean enhancedExceptionSensor = false;
			when(specialInstrumentationPoint.getId()).thenReturn(id);

			MethodVisitor methodVisitor = factory.getMethodVisitor(specialInstrumentationPoint, superMethodVisitor, 0, name, desc, enhancedExceptionSensor);

			assertThat(methodVisitor, is(instanceOf(SpecialMethodInstrumenter.class)));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void nullInstrumentationPoint() {
			String name = "method";
			String desc = "()V";
			boolean enhancedExceptionSensor = false;

			factory.getMethodVisitor(null, superMethodVisitor, 0, name, desc, enhancedExceptionSensor);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void unknownInstrumentationPoint() {
			IMethodInstrumentationPoint instrumentationPoint = mock(IMethodInstrumentationPoint.class);
			String name = "method";
			String desc = "()V";
			boolean enhancedExceptionSensor = false;

			factory.getMethodVisitor(instrumentationPoint, superMethodVisitor, 0, name, desc, enhancedExceptionSensor);
		}

	}

}
