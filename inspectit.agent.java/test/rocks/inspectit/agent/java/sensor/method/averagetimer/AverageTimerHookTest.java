package info.novatec.inspectit.agent.sensor.method.averagetimer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.util.ObjectUtils;
import info.novatec.inspectit.util.Timer;

import java.util.Map;

import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class AverageTimerHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private IIdManager idManager;

	@Mock
	private IPropertyAccessor propertyAccessor;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private Map<String, Object> parameter;

	private AverageTimerHook averageTimerHook;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		averageTimerHook = new AverageTimerHook(timer, idManager, propertyAccessor, parameter);
	}

	@Test
	public void oneRecord() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(idManager).getPlatformId();
		verify(idManager).getRegisteredMethodId(methodId);
		verify(idManager).getRegisteredSensorTypeId(sensorTypeId);
		verify(coreService).getMethodSensorData(sensorTypeId, methodId, null);
		verify(registeredSensorConfig).isPropertyAccess();

		TimerData timerData = new TimerData();
		timerData.setPlatformIdent(platformId);
		timerData.setMethodIdent(registeredMethodId);
		timerData.setSensorTypeIdent(registeredSensorTypeId);
		timerData.setCount(1L);
		timerData.setDuration(secondTimerValue - firstTimerValue);
		timerData.calculateMax(secondTimerValue - firstTimerValue);
		timerData.calculateMin(secondTimerValue - firstTimerValue);
		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new TimerDataVerifier(timerData)));

		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	@Test
	public void twoRecords() throws IdNotAvailableException {
		long platformId = 1L;
		long methodIdOne = 3L;
		long registeredMethodIdOne = 13L;
		long methodIdTwo = 9L;
		long registeredMethodIdTwo = 15L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;
		Double thirdTimerValue = 1578.92d;
		Double fourthTimerValue = 2319.712d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue).thenReturn(thirdTimerValue).thenReturn(fourthTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodIdOne)).thenReturn(registeredMethodIdOne);
		when(idManager.getRegisteredMethodId(methodIdTwo)).thenReturn(registeredMethodIdTwo);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		averageTimerHook.beforeBody(methodIdOne, sensorTypeId, object, parameters, registeredSensorConfig);
		averageTimerHook.beforeBody(methodIdTwo, sensorTypeId, object, parameters, registeredSensorConfig);

		averageTimerHook.firstAfterBody(methodIdTwo, sensorTypeId, object, parameters, result, registeredSensorConfig);
		averageTimerHook.secondAfterBody(coreService, methodIdTwo, sensorTypeId, object, parameters, result, registeredSensorConfig);
		TimerData timerDataTwo = new TimerData();
		timerDataTwo.setPlatformIdent(platformId);
		timerDataTwo.setMethodIdent(registeredMethodIdTwo);
		timerDataTwo.setSensorTypeIdent(registeredSensorTypeId);
		timerDataTwo.setCount(1L);
		timerDataTwo.setDuration(thirdTimerValue - secondTimerValue);
		timerDataTwo.calculateMax(thirdTimerValue - secondTimerValue);
		timerDataTwo.calculateMin(thirdTimerValue - secondTimerValue);
		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodIdTwo), (String) eq(null), argThat(new TimerDataVerifier(timerDataTwo)));

		averageTimerHook.firstAfterBody(methodIdOne, sensorTypeId, object, parameters, result, registeredSensorConfig);
		averageTimerHook.secondAfterBody(coreService, methodIdOne, sensorTypeId, object, parameters, result, registeredSensorConfig);
		TimerData timerDataOne = new TimerData();
		timerDataOne.setPlatformIdent(platformId);
		timerDataOne.setMethodIdent(registeredMethodIdOne);
		timerDataOne.setSensorTypeIdent(registeredSensorTypeId);
		timerDataOne.setCount(1L);
		timerDataOne.setDuration(fourthTimerValue - firstTimerValue);
		timerDataOne.calculateMax(fourthTimerValue - firstTimerValue);
		timerDataOne.calculateMin(fourthTimerValue - firstTimerValue);
		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodIdOne), (String) eq(null), argThat(new TimerDataVerifier(timerDataOne)));
	}

	@Test
	public void sameMethodTwice() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.0d;
		Double secondTimerValue = 1323.0d;
		Double thirdTimerValue = 1894.0d;
		Double fourthTimerValue = 2812.0d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue).thenReturn(thirdTimerValue).thenReturn(fourthTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		// First call
		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(idManager).getPlatformId();
		verify(idManager).getRegisteredMethodId(methodId);
		verify(idManager).getRegisteredSensorTypeId(sensorTypeId);
		verify(coreService).getMethodSensorData(sensorTypeId, methodId, null);
		verify(registeredSensorConfig).isPropertyAccess();

		TimerData timerData = new TimerData();
		timerData.setPlatformIdent(platformId);
		timerData.setMethodIdent(registeredMethodId);
		timerData.setSensorTypeIdent(registeredSensorTypeId);
		timerData.setCount(1L);
		timerData.setDuration(secondTimerValue - firstTimerValue);
		timerData.calculateMax(secondTimerValue - firstTimerValue);
		timerData.calculateMin(secondTimerValue - firstTimerValue);
		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new TimerDataVerifier(timerData)));

		// second one
		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(3)).getCurrentTime();

		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(4)).getCurrentTime();

		when(coreService.getMethodSensorData(sensorTypeId, methodId, null)).thenReturn(timerData);
		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(coreService, times(2)).getMethodSensorData(sensorTypeId, methodId, null);
		verify(registeredSensorConfig, times(2)).isPropertyAccess();

		assertThat(timerData.getPlatformIdent(), is(equalTo(platformId)));
		assertThat(timerData.getMethodIdent(), is(equalTo(registeredMethodId)));
		assertThat(timerData.getSensorTypeIdent(), is(equalTo(registeredSensorTypeId)));
		assertThat(timerData.getCount(), is(equalTo(2L)));
		assertThat(timerData.getDuration(), is(equalTo(fourthTimerValue - thirdTimerValue + secondTimerValue - firstTimerValue)));
		assertThat(timerData.getMax(), is(equalTo(fourthTimerValue - thirdTimerValue)));
		assertThat(timerData.getMin(), is(equalTo(secondTimerValue - firstTimerValue)));

		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	@Test
	public void newMinValue() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		long registeredSensorTypeId = 7L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.0d;
		Double secondTimerValue = 1323.0d;
		Double thirdTimerValue = 1894.0d;
		Double fourthTimerValue = 1934.0d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue).thenReturn(thirdTimerValue).thenReturn(fourthTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		when(idManager.getRegisteredSensorTypeId(sensorTypeId)).thenReturn(registeredSensorTypeId);

		// First call
		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(idManager).getPlatformId();
		verify(idManager).getRegisteredMethodId(methodId);
		verify(idManager).getRegisteredSensorTypeId(sensorTypeId);
		verify(coreService).getMethodSensorData(sensorTypeId, methodId, null);
		verify(registeredSensorConfig).isPropertyAccess();

		TimerData timerData = new TimerData();
		timerData.setPlatformIdent(platformId);
		timerData.setMethodIdent(registeredMethodId);
		timerData.setSensorTypeIdent(registeredSensorTypeId);
		timerData.setCount(1L);
		timerData.setDuration(secondTimerValue - firstTimerValue);
		timerData.calculateMax(secondTimerValue - firstTimerValue);
		timerData.calculateMin(secondTimerValue - firstTimerValue);
		verify(coreService).addMethodSensorData(eq(sensorTypeId), eq(methodId), (String) eq(null), argThat(new TimerDataVerifier(timerData)));

		// second one
		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(3)).getCurrentTime();

		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(timer, times(4)).getCurrentTime();

		when(coreService.getMethodSensorData(sensorTypeId, methodId, null)).thenReturn(timerData);
		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		verify(coreService, times(2)).getMethodSensorData(sensorTypeId, methodId, null);
		verify(registeredSensorConfig, times(2)).isPropertyAccess();

		assertThat(timerData.getPlatformIdent(), is(equalTo(platformId)));
		assertThat(timerData.getMethodIdent(), is(equalTo(registeredMethodId)));
		assertThat(timerData.getSensorTypeIdent(), is(equalTo(registeredSensorTypeId)));
		assertThat(timerData.getCount(), is(equalTo(2L)));
		assertThat(timerData.getDuration(), is(equalTo(fourthTimerValue - thirdTimerValue + secondTimerValue - firstTimerValue)));
		assertThat(timerData.getMax(), is(equalTo(secondTimerValue - firstTimerValue)));
		assertThat(timerData.getMin(), is(equalTo(fourthTimerValue - thirdTimerValue)));

		verifyNoMoreInteractions(timer, idManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	/**
	 * Inner class used to verify the contents of TimerData objects.
	 */
	private static class TimerDataVerifier extends ArgumentMatcher<TimerData> {
		private final TimerData timerData;

		public TimerDataVerifier(TimerData timerData) {
			this.timerData = timerData;
		}

		@Override
		public boolean matches(Object object) {
			if (!TimerData.class.isInstance(object)) {
				return false;
			}
			TimerData otherTimerData = (TimerData) object;
			if (timerData.getPlatformIdent() != otherTimerData.getPlatformIdent()) {
				return false;
			} else if (timerData.getMethodIdent() != otherTimerData.getMethodIdent()) {
				return false;
			} else if (timerData.getSensorTypeIdent() != otherTimerData.getSensorTypeIdent()) {
				return false;
			} else if (timerData.getCount() != otherTimerData.getCount()) {
				return false;
			} else if (timerData.getDuration() != otherTimerData.getDuration()) {
				return false;
			} else if (timerData.getMax() != otherTimerData.getMax()) {
				return false;
			} else if (timerData.getMin() != otherTimerData.getMin()) {
				return false;
			} else if (timerData.getAverage() != otherTimerData.getAverage()) {
				return false;
			} else if (!ObjectUtils.equals(timerData.getParameterContentData(), otherTimerData.getParameterContentData())) {
				return false;
			} else if (timerData.getVariance() != otherTimerData.getVariance()) {
				return false;
			}
			return true;
		}
	}

	@Test
	public void platformIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		doThrow(new IdNotAvailableException("")).when(idManager).getPlatformId();

		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addMethodSensorData(anyLong(), anyLong(), anyString(), (MethodSensorData) isNull());
	}

	@Test
	public void methodIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredMethodId(methodId);

		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addMethodSensorData(anyLong(), anyLong(), anyString(), (MethodSensorData) isNull());
	}

	@Test
	public void sensorTypeIdNotAvailable() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredSensorTypeId(sensorTypeId);

		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(coreService, never()).addMethodSensorData(anyLong(), anyLong(), anyString(), (MethodSensorData) isNull());
	}

	@Test
	public void propertyAccess() throws IdNotAvailableException {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long registeredMethodId = 13L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[2];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(idManager.getPlatformId()).thenReturn(platformId);
		when(idManager.getRegisteredMethodId(methodId)).thenReturn(registeredMethodId);
		doThrow(new IdNotAvailableException("")).when(idManager).getRegisteredSensorTypeId(sensorTypeId);
		when(registeredSensorConfig.isPropertyAccess()).thenReturn(true);

		averageTimerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		averageTimerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);
		averageTimerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, registeredSensorConfig);

		verify(registeredSensorConfig, times(1)).isPropertyAccess();
		verify(propertyAccessor, times(1)).getParameterContentData(registeredSensorConfig.getPropertyAccessorList(), object, parameters, result);
	}

}
