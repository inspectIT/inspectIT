package rocks.inspectit.agent.java.sensor.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.analyzer.classes.MyTestException;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.sensor.exception.ExceptionSensorHook;
import rocks.inspectit.agent.java.util.StringConstraint;
import rocks.inspectit.shared.all.communication.ExceptionEvent;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;

@SuppressWarnings("PMD")
public class ExceptionSensorHookTest extends AbstractLogSupport {
	@Mock
	private IPlatformManager platformManager;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	private Map<String, Object> parameter;

	private int stringLength;

	private ExceptionSensorHook exceptionHook;

	@BeforeMethod
	public void initTestClass() {
		stringLength = 1000;
		parameter = new HashMap<String, Object>();
		parameter.put("stringLength", String.valueOf(stringLength));
		exceptionHook = new ExceptionSensorHook(platformManager, parameter);
	}

	@Test
	public void throwableObjectWasCreated() throws InstantiationException, IllegalAccessException, IdNotAvailableException {
		long constructorId = 5L;
		long sensorTypeId = 3L;
		long platformId = 1L;

		Object[] parameters = new Object[0];
		MyTestException exceptionObject = MyTestException.class.newInstance();
		when(platformManager.getPlatformId()).thenReturn(platformId);

		when(registeredSensorConfig.getTargetClassFqn()).thenReturn(MyTestException.class.getName());

		exceptionHook.afterConstructor(coreService, constructorId, sensorTypeId, exceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(1)).getPlatformId();

		verifyNoMoreInteractions(platformManager);
		// verify(coreService, never());
	}

	@Test
	public void throwableObjectDifferentThenRSCTargetClass() throws InstantiationException, IllegalAccessException, IdNotAvailableException {
		long constructorId = 5L;
		long sensorTypeId = 3L;
		long platformId = 1L;

		Object[] parameters = new Object[0];
		Exception exceptionObject = Exception.class.newInstance();
		when(platformManager.getPlatformId()).thenReturn(platformId);

		when(registeredSensorConfig.getTargetClassFqn()).thenReturn(MyTestException.class.getName());

		exceptionHook.afterConstructor(coreService, constructorId, sensorTypeId, exceptionObject, parameters, registeredSensorConfig);

		verifyNoMoreInteractions(platformManager);
	}

	@Test
	public void throwableObjectCreatedThrownAndHandled() throws InstantiationException, IllegalAccessException, IdNotAvailableException {
		long methodId = 5L;
		long constructorId = 4L;
		long sensorTypeId = 3L;
		long platformId = 1L;
		long methodIdTwo = 20L;

		Object[] parameters = new Object[0];
		Object object = mock(Object.class);
		MyTestException exceptionObject = MyTestException.class.newInstance();

		ExceptionSensorData exceptionSensorData = new ExceptionSensorData(new Timestamp(System.currentTimeMillis()), platformId, sensorTypeId, methodIdTwo);
		exceptionSensorData.setErrorMessage(exceptionObject.getMessage());
		exceptionSensorData.setThrowableIdentityHashCode(System.identityHashCode(exceptionObject));

		when(platformManager.getPlatformId()).thenReturn(platformId);

		when(registeredSensorConfig.getTargetClassFqn()).thenReturn(MyTestException.class.getName());

		exceptionSensorData.setExceptionEvent(ExceptionEvent.CREATED);
		exceptionHook.afterConstructor(coreService, constructorId, sensorTypeId, exceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(1)).getPlatformId();
		verify(coreService, times(1)).addExceptionSensorData(eq(sensorTypeId), eq(exceptionSensorData.getThrowableIdentityHashCode()),
				argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));

		exceptionSensorData.setExceptionEvent(ExceptionEvent.PASSED);
		exceptionHook.dispatchOnThrowInBody(coreService, methodId, sensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(2)).getPlatformId();
		verify(coreService, times(1)).addExceptionSensorData(eq(sensorTypeId), eq(exceptionSensorData.getThrowableIdentityHashCode()),
				argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));

		exceptionSensorData.setExceptionEvent(ExceptionEvent.HANDLED);
		exceptionHook.dispatchBeforeCatchBody(coreService, methodIdTwo, sensorTypeId, exceptionObject, registeredSensorConfig);
		verify(platformManager, times(3)).getPlatformId();
		verify(coreService, times(1)).addExceptionSensorData(eq(sensorTypeId), eq(exceptionSensorData.getThrowableIdentityHashCode()),
				argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));

		verifyNoMoreInteractions(platformManager, coreService);
	}

	@Test
	public void differentThrowableObjectsCreatedAndThrown() throws InstantiationException, IllegalAccessException, IdNotAvailableException {
		long methodId = 5L;
		long constructorId = 4L;
		long sensorTypeId = 3L;
		long platformId = 1L;

		Object[] parameters = new Object[0];
		Object object = mock(Object.class);
		MyTestException firstExceptionObject = MyTestException.class.newInstance();
		MyTestException secondExceptionObject = MyTestException.class.newInstance();

		when(platformManager.getPlatformId()).thenReturn(platformId);

		when(registeredSensorConfig.getTargetClassFqn()).thenReturn(MyTestException.class.getName());

		exceptionHook.afterConstructor(coreService, constructorId, sensorTypeId, firstExceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(1)).getPlatformId();
		// verify(coreService,
		// times(1)).addExceptionSensorData(eq(registeredSensorTypeId),
		// argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));

		exceptionHook.dispatchOnThrowInBody(coreService, methodId, sensorTypeId, object, secondExceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(2)).getPlatformId();

		verifyNoMoreInteractions(platformManager);
	}

	@Test
	public void throwableHasCause() throws InstantiationException, IllegalAccessException, IdNotAvailableException, SecurityException, NoSuchFieldException {
		long methodId = 5L;
		long constructorId = 4L;
		long sensorTypeId = 3L;
		long platformId = 1L;
		long methodIdTwo = 20L;

		Object[] parameters = new Object[0];
		Object object = mock(Object.class);
		MyTestException exceptionObject = MyTestException.class.newInstance();
		Throwable cause = Throwable.class.newInstance();

		// setting the cause at the exceptionObject
		// we can only access the cause field from the overall superclass
		// Throwable
		Field causeField = exceptionObject.getClass().getSuperclass().getSuperclass().getDeclaredField("cause");
		causeField.setAccessible(true);
		causeField.set(exceptionObject, cause);

		ExceptionSensorData exceptionSensorData = new ExceptionSensorData(new Timestamp(System.currentTimeMillis()), platformId, sensorTypeId, methodIdTwo);
		exceptionSensorData.setErrorMessage(exceptionObject.getMessage());
		exceptionSensorData.setCause(exceptionObject.getCause().getClass().getName());
		exceptionSensorData.setThrowableIdentityHashCode(System.identityHashCode(exceptionObject));

		when(platformManager.getPlatformId()).thenReturn(platformId);

		when(registeredSensorConfig.getTargetClassFqn()).thenReturn(MyTestException.class.getName());

		exceptionSensorData.setExceptionEvent(ExceptionEvent.CREATED);
		exceptionHook.afterConstructor(coreService, constructorId, sensorTypeId, exceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(1)).getPlatformId();
		verify(coreService, times(1)).addExceptionSensorData(eq(sensorTypeId), eq(exceptionSensorData.getThrowableIdentityHashCode()),
				argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));
		assertThat(exceptionSensorData.getCause(), is(equalTo(cause.getClass().getName())));

		// resetting the cause to null as we need the cause only in the first
		// data object
		exceptionSensorData.setCause(null);

		exceptionSensorData.setExceptionEvent(ExceptionEvent.PASSED);
		exceptionHook.dispatchOnThrowInBody(coreService, methodId, sensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(2)).getPlatformId();
		verify(coreService, times(1)).addExceptionSensorData(eq(sensorTypeId), eq(exceptionSensorData.getThrowableIdentityHashCode()),
				argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));

		exceptionSensorData.setExceptionEvent(ExceptionEvent.HANDLED);
		exceptionHook.dispatchBeforeCatchBody(coreService, methodIdTwo, sensorTypeId, exceptionObject, registeredSensorConfig);
		verify(platformManager, times(3)).getPlatformId();
		verify(coreService, times(1)).addExceptionSensorData(eq(sensorTypeId), eq(exceptionSensorData.getThrowableIdentityHashCode()),
				argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));

		verifyNoMoreInteractions(platformManager, coreService);
	}

	@Test
	public void valueTooLong() throws InstantiationException, IllegalAccessException, IdNotAvailableException {
		long constructorId = 5L;
		long sensorTypeId = 3L;
		long platformId = 1L;

		Object[] parameters = new Object[0];
		StringConstraint strConstraint = new StringConstraint(parameter);

		// the actual error message is too long, so it should be cropped later on
		String exceptionMessage = fillString('x', stringLength + 1);
		MyTestException exceptionObject = new MyTestException(exceptionMessage);

		ExceptionSensorData exceptionSensorData = new ExceptionSensorData(new Timestamp(System.currentTimeMillis()), platformId, sensorTypeId, constructorId);
		exceptionSensorData.setExceptionEvent(ExceptionEvent.CREATED);
		exceptionSensorData.setThrowableIdentityHashCode(System.identityHashCode(exceptionObject));

		// the actual error message to be verified against
		exceptionSensorData.setErrorMessage(strConstraint.crop(exceptionMessage));

		when(platformManager.getPlatformId()).thenReturn(platformId);

		when(registeredSensorConfig.getTargetClassFqn()).thenReturn(MyTestException.class.getName());

		exceptionHook.afterConstructor(coreService, constructorId, sensorTypeId, exceptionObject, parameters, registeredSensorConfig);
		verify(platformManager, times(1)).getPlatformId();
		verify(coreService, times(1)).addExceptionSensorData(eq(sensorTypeId), eq(exceptionSensorData.getThrowableIdentityHashCode()),
				argThat(new ExceptionSensorDataVerifier(exceptionSensorData)));

		verifyNoMoreInteractions(platformManager);
	}

	@Test
	public void platformIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long methodId = 3L;
		long constructorId = 7L;
		long exceptionSensorTypeId = 11L;
		Object object = mock(Object.class);
		MyTestException exceptionObject = mock(MyTestException.class);
		Object[] parameters = new Object[0];

		doThrow(new IdNotAvailableException("")).when(platformManager).getPlatformId();

		exceptionHook.afterConstructor(coreService, constructorId, exceptionSensorTypeId, exceptionObject, parameters, registeredSensorConfig);
		exceptionHook.dispatchOnThrowInBody(coreService, methodId, exceptionSensorTypeId, object, exceptionObject, parameters, registeredSensorConfig);
		exceptionHook.dispatchBeforeCatchBody(coreService, methodId, exceptionSensorTypeId, exceptionObject, registeredSensorConfig);

		verify(coreService, never()).addExceptionSensorData(anyLong(), anyInt(), (ExceptionSensorData) isNull());
	}

	private static class ExceptionSensorDataVerifier extends ArgumentMatcher<ExceptionSensorData> {
		private final ExceptionSensorData exceptionSensorData;

		public ExceptionSensorDataVerifier(ExceptionSensorData exceptionSensorData) {
			this.exceptionSensorData = exceptionSensorData;
		}

		@Override
		public boolean matches(Object object) {
			if (!ExceptionSensorData.class.isInstance(object)) {
				return false;
			}

			ExceptionSensorData otherExceptionSensorData = (ExceptionSensorData) object;
			if ((null != exceptionSensorData.getCause()) && !exceptionSensorData.getCause().equals(otherExceptionSensorData.getCause())) {
				return false;
			}
			if ((null != exceptionSensorData.getErrorMessage()) && !exceptionSensorData.getErrorMessage().equals(otherExceptionSensorData.getErrorMessage())) {
				return false;
			}
			if (!exceptionSensorData.getExceptionEvent().equals(otherExceptionSensorData.getExceptionEvent())) {
				return false;
			}
			if ((null != exceptionSensorData.getThrowableType()) && !exceptionSensorData.getThrowableType().equals(otherExceptionSensorData.getThrowableType())) {
				return false;
			}
			if (exceptionSensorData.getThrowableIdentityHashCode() != otherExceptionSensorData.getThrowableIdentityHashCode()) {
				return false;
			}

			return true;
		}

	}

	public String fillString(char character, int count) {
		// creates a string of 'x' repeating characters
		char[] chars = new char[count];
		while (count > 0) {
			chars[--count] = character;
		}
		return new String(chars);
	}
}
