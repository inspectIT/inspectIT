package info.novatec.inspectit.communication.valueobject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.TimerData;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the functionality of {@link TimerRawVO}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class TimerRawVOTest {

	private static final long PLATFORM_ID = 10L;

	private static final long SENSOR_ID = 20L;

	private static final long METHOD_ID = 30L;

	/**
	 * Class under test.
	 */
	private TimerRawVO timerRawVO;

	/**
	 * Init method.
	 */
	@BeforeMethod
	public void init() {
		timerRawVO = new TimerRawVO(null, PLATFORM_ID, SENSOR_ID, METHOD_ID, null, false);
	}

	/**
	 * Tests adding of time only.
	 */
	@Test
	public void time() {
		double time1 = 5.5d;
		double time2 = 13.688d;

		timerRawVO.add(time1);
		timerRawVO.add(time2);
		DefaultData data = timerRawVO.finalizeData();

		assertThat(data, is(instanceOf(TimerData.class)));
		TimerData timerData = (TimerData) data;
		assertThat(timerData.getPlatformIdent(), is(PLATFORM_ID));
		assertThat(timerData.getSensorTypeIdent(), is(SENSOR_ID));
		assertThat(timerData.getMethodIdent(), is(METHOD_ID));
		assertThat(timerData.getCount(), is(2l));
		assertThat(timerData.isTimeDataAvailable(), is(true));
		assertThat(timerData.getAverage(), is((time1 + time2) / 2));
		assertThat(timerData.getDuration(), is(time1 + time2));
		assertThat(timerData.getMin(), is(Math.min(time1, time2)));
		assertThat(timerData.getMax(), is(Math.max(time1, time2)));
		assertThat(timerData.isExclusiveTimeDataAvailable(), is(false));
		assertThat(timerData.isCpuMetricDataAvailable(), is(false));

	}

	/**
	 * Tests adding of time and cpu time.
	 */
	@Test
	public void timeAndCpu() {
		double time1 = 7.54545d;
		double time2 = 187.688d;

		double cpu1 = 5.65646d;
		double cpu2 = 155.2323d;

		timerRawVO.add(time1, cpu1);
		timerRawVO.add(time2, cpu2);
		DefaultData data = timerRawVO.finalizeData();

		assertThat(data, is(instanceOf(TimerData.class)));
		TimerData timerData = (TimerData) data;
		assertThat(timerData.getPlatformIdent(), is(PLATFORM_ID));
		assertThat(timerData.getSensorTypeIdent(), is(SENSOR_ID));
		assertThat(timerData.getMethodIdent(), is(METHOD_ID));
		assertThat(timerData.getCount(), is(2l));
		assertThat(timerData.isTimeDataAvailable(), is(true));
		assertThat(timerData.getAverage(), is((time1 + time2) / 2));
		assertThat(timerData.getDuration(), is(time1 + time2));
		assertThat(timerData.getMin(), is(Math.min(time1, time2)));
		assertThat(timerData.getMax(), is(Math.max(time1, time2)));
		assertThat(timerData.isExclusiveTimeDataAvailable(), is(false));
		assertThat(timerData.isCpuMetricDataAvailable(), is(true));
		assertThat(timerData.getCpuAverage(), is((cpu1 + cpu2) / 2));
		assertThat(timerData.getCpuDuration(), is(cpu1 + cpu2));
		assertThat(timerData.getCpuMin(), is(Math.min(cpu1, cpu2)));
		assertThat(timerData.getCpuMax(), is(Math.max(cpu1, cpu2)));
	}
}
