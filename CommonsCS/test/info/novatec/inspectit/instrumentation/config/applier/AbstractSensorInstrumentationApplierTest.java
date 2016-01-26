/**
 *
 */
package info.novatec.inspectit.instrumentation.config.applier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.MethodType.Character;
import info.novatec.inspectit.instrumentation.config.impl.AgentConfiguration;
import info.novatec.inspectit.instrumentation.config.impl.ExceptionSensorTypeConfig;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class AbstractSensorInstrumentationApplierTest extends TestBase {

	protected AbstractSensorInstrumentationApplier applier;

	@Mock
	protected Environment environment;

	@Mock
	protected IRegistrationService registrationService;

	@Mock
	protected ClassType classType;

	@Mock
	protected MethodType methodType;

	@BeforeMethod
	public void setup() {
		applier = new AbstractSensorInstrumentationApplier(environment, registrationService) {

			@Override
			public AbstractClassSensorAssignment<?> getSensorAssignment() {
				return null;
			}

			@Override
			protected void applyAssignment(AgentConfiguration agentConfiguration, RegisteredSensorConfig registeredSensorConfig) {
				// ignore
			}

			@Override
			protected boolean matches(ClassType classType) {
				return true;
			}

			@Override
			protected boolean matches(MethodType methodType) {
				return true;
			}

		};

		// class to return one method
		when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
	}

	public class AddInstrumentationPoints extends AbstractSensorInstrumentationApplierTest {

		@Test
		public void enchancedExceptionSensor() {
			long agentId = 13L;
			long methodId = 17L;
			String sensorClassName = "sensorClassName";
			when(registrationService.registerMethodIdent(eq(agentId), anyString(), anyString(), anyString(), Mockito.<List<String>> any(), anyString(), anyInt())).thenReturn(methodId);

			ExceptionSensorTypeConfig exceptionSensorTypeConfig = mock(ExceptionSensorTypeConfig.class);
			when(exceptionSensorTypeConfig.isEnhanced()).thenReturn(true);

			AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
			when(agentConfiguration.getPlatformId()).thenReturn(agentId);
			when(agentConfiguration.getExceptionSensorTypeConfig()).thenReturn(exceptionSensorTypeConfig);

			IMethodSensorConfig methodSensorConfig = mock(IMethodSensorConfig.class);
			when(methodSensorConfig.getClassName()).thenReturn(sensorClassName);
			when(environment.getMethodSensorTypeConfig(Mockito.<Class<? extends IMethodSensorConfig>> any())).thenReturn(methodSensorConfig);

			String packageName = "my.favorite.package";
			String className = "ClassName";
			String methodName = "methodName";
			String returnType = "returnType";
			List<String> parameters = Arrays.asList(new String[] { "p1", "p2" });
			int mod = 10;
			when(classType.getFQN()).thenReturn(packageName + '.' + className);
			when(methodType.getClassOrInterfaceType()).thenReturn(classType);
			when(methodType.getName()).thenReturn(methodName);
			when(methodType.getParameters()).thenReturn(parameters);
			when(methodType.getReturnType()).thenReturn(returnType);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getModifiers()).thenReturn(mod);

			boolean changed = applier.addInstrumentationPoints(agentConfiguration, classType);

			// verify results
			assertThat(changed, is(true));

			// verify registration service
			verify(registrationService, times(1)).registerMethodIdent(agentId, packageName, className, methodName, parameters, returnType, mod);
			verifyNoMoreInteractions(registrationService);

			// check RSC and instrumentation config
			ArgumentCaptor<MethodInstrumentationConfig> captor = ArgumentCaptor.forClass(MethodInstrumentationConfig.class);
			verify(methodType, times(1)).setMethodInstrumentationConfig(captor.capture());

			MethodInstrumentationConfig instrumentationConfig = captor.getValue();
			RegisteredSensorConfig rsc = instrumentationConfig.getRegisteredSensorConfig();
			assertThat(instrumentationConfig.getTargetClassFqn(), is(packageName + '.' + className));
			assertThat(instrumentationConfig.getTargetMethodName(), is(methodName));
			assertThat(instrumentationConfig.getReturnType(), is(returnType));
			assertThat(instrumentationConfig.getParameterTypes(), is(parameters));
			assertThat(instrumentationConfig.getAllInstrumentationPoints(), hasSize(1));
			assertThat(rsc.getId(), is(methodId));
			assertThat(rsc.getSensorIds().length, is(0));
			assertThat(rsc.getTargetClassFqn(), is(packageName + '.' + className));
			assertThat(rsc.getTargetMethodName(), is(methodName));
			assertThat(rsc.getReturnType(), is(returnType));
			assertThat(rsc.getParameterTypes(), is(parameters));
			assertThat(rsc.isEnhancedExceptionSensor(), is(true));
		}
	}

	public class RemoveInstrumentationPoints extends AbstractSensorInstrumentationApplierTest {

		@Test
		public void remove() {
			// something to remove
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classType.hasInstrumentationPoints()).thenReturn(true);

			boolean result = applier.removeInstrumentationPoints(classType);

			assertThat(result, is(true));
			verify(methodType, times(1)).setMethodInstrumentationConfig(null);
		}

		@Test
		public void removeNothing() {
			// nothing to remove
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			when(classType.hasInstrumentationPoints()).thenReturn(false);

			boolean result = applier.removeInstrumentationPoints(classType);

			assertThat(result, is(false));
			verifyZeroInteractions(methodType);
		}
	}

}
