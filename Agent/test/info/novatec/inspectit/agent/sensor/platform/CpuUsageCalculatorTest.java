package info.novatec.inspectit.agent.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.sensor.platform.provider.OperatingSystemInfoProvider;
import info.novatec.inspectit.agent.sensor.platform.provider.sun.SunOperatingSystemInfoProvider;

import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sun.management.OperatingSystemMXBean;

@SuppressWarnings("PMD")
public class CpuUsageCalculatorTest extends AbstractLogSupport {

	@Mock
	private RuntimeMXBean runtimeBean;

	@Mock
	private OperatingSystemMXBean osBean;

	private OperatingSystemInfoProvider wrapper;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		wrapper = new SunOperatingSystemInfoProvider();

		Field field = wrapper.getClass().getDeclaredField("runtimeBean");
		field.setAccessible(true);
		field.set(wrapper, runtimeBean);

		field = wrapper.getClass().getDeclaredField("osBean");
		field.setAccessible(true);
		field.set(wrapper, osBean);
	}

	@Test
	public void calculateCpuUsage() {
		int availableProc = 1;

		// process cpu time is provided as nanoseconds
		long processCpuTime1 = 200L * 1000 * 1000; // ns representation of 200ms
		long processCpuTime2 = 500L * 1000 * 1000; // ns representation of 500ms

		// uptime is provided in milliseconds
		long uptime1 = 500L; // 500ms
		long uptime2 = 1100L; // 1100ms

		when(runtimeBean.getUptime()).thenReturn(uptime1).thenReturn(uptime2);
		when(osBean.getAvailableProcessors()).thenReturn(availableProc);
		when(osBean.getProcessCpuTime()).thenReturn(processCpuTime1).thenReturn(processCpuTime2);

		float cpuUsage1 = wrapper.retrieveCpuUsage();
		assertThat((double) cpuUsage1, is(closeTo(0.0d, 0.01d)));

		float cpuUsage2 = wrapper.retrieveCpuUsage();
		// CPU usage can only be deduced after the second call
		long process = (processCpuTime2 - processCpuTime1);
		long upAsNano = ((uptime2 - uptime1) * 1000 * 1000);
		float expectedUsage = (float) process / upAsNano * 100;
		assertThat((double) cpuUsage2, is(closeTo((double) expectedUsage, 0.01d)));
	}

}
