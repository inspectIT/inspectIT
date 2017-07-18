package rocks.inspectit.agent.java.sensor.method.timer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.TimerData;

@SuppressWarnings("PMD")
public class TimerHookTest extends AbstractLogSupport {

	@Mock
	private Timer timer;

	@Mock
	private IPlatformManager platformManager;

	@Mock
	private IPropertyAccessor propertyAccessor;

	@Mock
	private ICoreService coreService;

	@Mock
	private RegisteredSensorConfig registeredSensorConfig;

	@Mock
	private ThreadMXBean threadMXBean;

	private TimerHook timerHook;

	@BeforeMethod
	public void initTestClass() {
		Map<String, Object> settings = new HashMap<String, Object>();
		when(threadMXBean.isThreadCpuTimeEnabled()).thenReturn(true);
		when(threadMXBean.isThreadCpuTimeSupported()).thenReturn(true);
		timerHook = new TimerHook(timer, platformManager, propertyAccessor, settings, threadMXBean);
	}

	@Test
	public void sameMethodTwice() {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.0d;
		Double secondTimerValue = 1323.0d;
		Double thirdTimerValue = 1894.0d;
		Double fourthTimerValue = 2812.0d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue).thenReturn(thirdTimerValue).thenReturn(fourthTimerValue);
		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(registeredSensorConfig.getSettings()).thenReturn(Collections.<String, Object> singletonMap("charting", Boolean.TRUE));

		// First call
		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(platformManager).getPlatformId();
		verify(registeredSensorConfig).isPropertyAccess();

		ArgumentCaptor<TimerData> captor = ArgumentCaptor.forClass(TimerData.class);
		verify(coreService).addDefaultData(captor.capture());

		TimerData timerData = captor.getValue();
		assertThat(timerData.getPlatformIdent(), is(platformId));
		assertThat(timerData.getMethodIdent(), is(methodId));
		assertThat(timerData.getSensorTypeIdent(), is(sensorTypeId));
		assertThat(timerData.getTimeStamp(), is(not(nullValue())));
		assertThat(timerData.getCount(), is(1L));
		assertThat(timerData.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(timerData.getMin(), is(secondTimerValue - firstTimerValue));
		assertThat(timerData.getMax(), is(secondTimerValue - firstTimerValue));
		assertThat(timerData.isCharting(), is(true));

		// second one
		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(3)).getCurrentTime();

		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(timer, times(4)).getCurrentTime();

		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);

		verify(registeredSensorConfig, times(2)).isPropertyAccess();
		verify(registeredSensorConfig, times(2)).getSettings();
		verify(platformManager, times(2)).getPlatformId();
		verify(coreService, times(2)).addDefaultData(captor.capture());

		timerData = captor.getValue();
		assertThat(timerData.getPlatformIdent(), is(platformId));
		assertThat(timerData.getMethodIdent(), is(methodId));
		assertThat(timerData.getSensorTypeIdent(), is(sensorTypeId));
		assertThat(timerData.getTimeStamp(), is(not(nullValue())));
		assertThat(timerData.getCount(), is(1L));
		assertThat(timerData.getDuration(), is(fourthTimerValue - thirdTimerValue));
		assertThat(timerData.getMin(), is(fourthTimerValue - thirdTimerValue));
		assertThat(timerData.getMax(), is(fourthTimerValue - thirdTimerValue));
		assertThat(timerData.isCharting(), is(true));

		verifyNoMoreInteractions(timer, platformManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	@Test
	public void propertyAccess() {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[2];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;
		ParameterContentData parameterContentData = Mockito.mock(ParameterContentData.class);

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(registeredSensorConfig.isPropertyAccess()).thenReturn(true);
		when(propertyAccessor.getParameterContentData(registeredSensorConfig.getPropertyAccessorList(), object, parameters, result, false)).thenReturn(Collections.singletonList(parameterContentData));

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);

		verify(registeredSensorConfig, times(1)).isPropertyAccess();
		verify(propertyAccessor, times(1)).getParameterContentData(registeredSensorConfig.getPropertyAccessorList(), object, parameters, result, false);
		ArgumentCaptor<TimerData> captor = ArgumentCaptor.forClass(TimerData.class);
		verify(coreService).addDefaultData(captor.capture());

		TimerData timerData = captor.getValue();
		assertThat(timerData.getParameterContentData(), hasSize(1));
		assertThat(timerData.getParameterContentData(), hasItem(parameterContentData));
	}

	@Test
	public void propertyAccessException() {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[2];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(registeredSensorConfig.isPropertyAccess()).thenReturn(true);

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, true, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, true, registeredSensorConfig);

		verify(registeredSensorConfig, times(1)).isPropertyAccess();
		verify(propertyAccessor, times(1)).getParameterContentData(registeredSensorConfig.getPropertyAccessorList(), object, parameters, result, true);
	}

	@Test
	public void charting() {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[2];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(platformManager.getPlatformId()).thenReturn(platformId);
		when(registeredSensorConfig.getSettings()).thenReturn(Collections.<String, Object> singletonMap("charting", Boolean.TRUE));

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);

		ArgumentCaptor<TimerData> captor = ArgumentCaptor.forClass(TimerData.class);
		verify(coreService).addDefaultData(captor.capture());

		TimerData timerData = captor.getValue();
		assertThat(timerData.isCharting(), is(true));
	}

	@Test
	public void oneRecordWithCpuTime() {
		// set up data
		long platformId = 1L;
		long methodId = 3L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue);
		when(threadMXBean.getCurrentThreadCpuTime()).thenReturn(firstCpuTimerValue).thenReturn(secondCpuTimerValue);
		when(platformManager.getPlatformId()).thenReturn(platformId);

		when(registeredSensorConfig.getSettings()).thenReturn(Collections.<String, Object> singletonMap("charting", Boolean.TRUE));

		timerHook.beforeBody(methodId, sensorTypeId, object, parameters, registeredSensorConfig);
		verify(timer, times(1)).getCurrentTime();

		timerHook.firstAfterBody(methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(timer, times(2)).getCurrentTime();

		timerHook.secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		verify(platformManager).getPlatformId();
		verify(registeredSensorConfig).isPropertyAccess();
		verify(registeredSensorConfig).getSettings();

		ArgumentCaptor<TimerData> captor = ArgumentCaptor.forClass(TimerData.class);
		verify(coreService).addDefaultData(captor.capture());

		TimerData timerData = captor.getValue();
		assertThat(timerData.getPlatformIdent(), is(platformId));
		assertThat(timerData.getMethodIdent(), is(methodId));
		assertThat(timerData.getSensorTypeIdent(), is(sensorTypeId));
		assertThat(timerData.getTimeStamp(), is(not(nullValue())));
		assertThat(timerData.getCount(), is(1L));
		assertThat(timerData.getDuration(), is(secondTimerValue - firstTimerValue));
		assertThat(timerData.getMin(), is(secondTimerValue - firstTimerValue));
		assertThat(timerData.getMax(), is(secondTimerValue - firstTimerValue));
		assertThat(timerData.getCpuDuration(), is((secondCpuTimerValue - firstCpuTimerValue) / 1000000.0d));
		assertThat(timerData.getCpuMin(), is((secondCpuTimerValue - firstCpuTimerValue) / 1000000.0d));
		assertThat(timerData.getCpuMax(), is((secondCpuTimerValue - firstCpuTimerValue) / 1000000.0d));
		assertThat(timerData.isCharting(), is(true));

		verifyNoMoreInteractions(timer, platformManager, coreService, registeredSensorConfig);
		verifyZeroInteractions(propertyAccessor, object, result);
	}

	@Test
	public void twoRecordsWithCpuTime() {
		long platformId = 1L;
		long methodIdOne = 3L;
		long methodIdTwo = 9L;
		long sensorTypeId = 11L;
		Object object = mock(Object.class);
		Object[] parameters = new Object[0];
		Object result = mock(Object.class);

		Double firstTimerValue = 1000.453d;
		Double secondTimerValue = 1323.675d;
		Double thirdTimerValue = 1578.92d;
		Double fourthTimerValue = 2319.712d;

		Long firstCpuTimerValue = 5000L;
		Long secondCpuTimerValue = 6872L;
		Long thirdCpuTimerValue = 8412L;
		Long fourthCpuTimerValue = 15932L;

		when(timer.getCurrentTime()).thenReturn(firstTimerValue).thenReturn(secondTimerValue).thenReturn(thirdTimerValue).thenReturn(fourthTimerValue);
		when(threadMXBean.getCurrentThreadCpuTime()).thenReturn(firstCpuTimerValue).thenReturn(secondCpuTimerValue).thenReturn(thirdCpuTimerValue).thenReturn(fourthCpuTimerValue);
		when(platformManager.getPlatformId()).thenReturn(platformId);

		timerHook.beforeBody(methodIdOne, sensorTypeId, object, parameters, registeredSensorConfig);
		timerHook.beforeBody(methodIdTwo, sensorTypeId, object, parameters, registeredSensorConfig);

		timerHook.firstAfterBody(methodIdTwo, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodIdTwo, sensorTypeId, object, parameters, result, false, registeredSensorConfig);

		ArgumentCaptor<TimerData> captor = ArgumentCaptor.forClass(TimerData.class);
		verify(coreService).addDefaultData(captor.capture());

		TimerData timerData = captor.getValue();
		assertThat(timerData.getPlatformIdent(), is(platformId));
		assertThat(timerData.getMethodIdent(), is(methodIdTwo));
		assertThat(timerData.getSensorTypeIdent(), is(sensorTypeId));
		assertThat(timerData.getTimeStamp(), is(not(nullValue())));
		assertThat(timerData.getCount(), is(1L));
		assertThat(timerData.getDuration(), is(thirdTimerValue - secondTimerValue));
		assertThat(timerData.getMin(), is(thirdTimerValue - secondTimerValue));
		assertThat(timerData.getMax(), is(thirdTimerValue - secondTimerValue));
		assertThat(timerData.getCpuDuration(), is((thirdCpuTimerValue - secondCpuTimerValue) / 1000000.0d));
		assertThat(timerData.getCpuMin(), is((thirdCpuTimerValue - secondCpuTimerValue) / 1000000.0d));
		assertThat(timerData.getCpuMax(), is((thirdCpuTimerValue - secondCpuTimerValue) / 1000000.0d));
		assertThat(timerData.isCharting(), is(false));

		timerHook.firstAfterBody(methodIdOne, sensorTypeId, object, parameters, result, false, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodIdOne, sensorTypeId, object, parameters, result, false, registeredSensorConfig);

		verify(coreService, times(2)).addDefaultData(captor.capture());

		timerData = captor.getValue();
		assertThat(timerData.getPlatformIdent(), is(platformId));
		assertThat(timerData.getMethodIdent(), is(methodIdOne));
		assertThat(timerData.getSensorTypeIdent(), is(sensorTypeId));
		assertThat(timerData.getTimeStamp(), is(not(nullValue())));
		assertThat(timerData.getCount(), is(1L));
		assertThat(timerData.getDuration(), is(fourthTimerValue - firstTimerValue));
		assertThat(timerData.getMin(), is(fourthTimerValue - firstTimerValue));
		assertThat(timerData.getMax(), is(fourthTimerValue - firstTimerValue));
		assertThat(timerData.getCpuDuration(), is((fourthCpuTimerValue - firstCpuTimerValue) / 1000000.0d));
		assertThat(timerData.getCpuMin(), is((fourthCpuTimerValue - firstCpuTimerValue) / 1000000.0d));
		assertThat(timerData.getCpuMax(), is((fourthCpuTimerValue - firstCpuTimerValue) / 1000000.0d));
		assertThat(timerData.isCharting(), is(false));
	}

}
