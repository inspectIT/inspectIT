package rocks.inspectit.agent.java.sensor.method.timer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.ThreadParams;

import rocks.inspectit.agent.java.config.impl.PropertyAccessor;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.sensor.method.AbstractHookPerfTest;
import rocks.inspectit.agent.java.sensor.method.invocationsequence.InvocationSequenceHook;
import rocks.inspectit.agent.java.util.Timer;

/**
 * JMH Test for {@link TimerHook} in combination with {@link InvocationSequenceHook} as
 * core-service.
 *
 * @author Matthias Huber
 *
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 100000)
@Measurement(iterations = 10, batchSize = 100000)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
public class TimerHookWithInvocationPerfTest extends AbstractHookPerfTest {

	@Param({ "0" })
	public int tokens;

	@Param({ "false" })
	public boolean charting;

	private InvocationSequenceHook invocationSequenceHook;
	private TimerHook timerHook;
	private TimerHook timerHookNoThreadCPU;

	private RegisteredSensorConfig registeredSensorConfig;

	@Setup(Level.Trial)
	public void checkCondition() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		boolean validTest = bean.isCurrentThreadCpuTimeSupported();
		if (validTest) {
			validTest = bean.isThreadCpuTimeEnabled();
			if (!validTest) {
				// try if it can be set
				bean.setThreadCpuTimeEnabled(true);
				validTest = bean.isThreadCpuTimeEnabled();
			}
		}
		if (!validTest) {
			throw new IllegalStateException("Tests cannot differentiate between CPU Thread enabled or disabled!");
		}
	}

	@Setup(Level.Trial)
	public void createRegisteredSensorConfig() {
		registeredSensorConfig = new RegisteredSensorConfig();
		registeredSensorConfig.setSettings(Collections.<String, Object> singletonMap("charting", charting));
	}

	@Override
	@Setup(Level.Iteration)
	public void init(ThreadParams threadParams) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		super.init(threadParams);

		invocationSequenceHook = new InvocationSequenceHook(new Timer(), platformManager, coreService, new TracerImpl(), new PropertyAccessor(), new HashMap<String, Object>(), false);

		timerHook = new TimerHook(new Timer(), platformManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());
		timerHookNoThreadCPU = new TimerHook(new Timer(), platformManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());

		// disable CPU Threading
		Field cpuThreadEnabledField = timerHookNoThreadCPU.getClass().getDeclaredField("enabled");
		cpuThreadEnabledField.setAccessible(true);
		cpuThreadEnabledField.setBoolean(timerHookNoThreadCPU, false);
		cpuThreadEnabledField.setAccessible(false);

		// start an invocation
		invocationSequenceHook.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);
	}

	public void baselineConsumeCPU() {
		Blackhole.consumeCPU(tokens);
	}

	/**
	 * Benchmarks the overhead time of measuring a method invocation with inspectIT.
	 * <p>
	 * TODOs: We use only one sensor on one method per thread! Is this valid for testing!!!
	 * <p>
	 * It measures the best case scenario:<br>
	 * - creation of a timestamp before the method is invoked.<br>
	 * - creation of a timestamp after the method invocation is finished.<br>
	 * - shortest second-after-body-scenario possible.<br>
	 * <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	@Benchmark
	public void measureMethodWithStartedInvocation() {
		// Blackhole.consumeCPU(tokens);

		timerHook.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
		timerHook.secondAfterBody(invocationSequenceHook, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
	}

	/**
	 * Benchmarks the overhead time of measuring a method invocation + Thread CPU Time with
	 * inspectIT.
	 * <p>
	 * TODOs: We use only one sensor on one method per thread! Is this valid for testing!!!
	 * <p>
	 * It measures the best case scenario:<br>
	 * - creation of a timestamp before the method is invoked.<br>
	 * - creation of a timestamp after the method invocation is finished.<br>
	 * - shortest second-after-body-scenario possible.<br>
	 * <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	@Benchmark
	public void measureMethodWithinInvocationNoThreadCPU() {
		// Blackhole.consumeCPU(tokens);

		timerHookNoThreadCPU.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);
		timerHookNoThreadCPU.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
		timerHookNoThreadCPU.secondAfterBody(invocationSequenceHook, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
	}

	@Override
	@TearDown(Level.Iteration)
	public void cleanUp() throws Exception {
		super.cleanUp();

		invocationSequenceHook = null; // NOPMD
		timerHook = null; // NOPMD
		timerHookNoThreadCPU = null; // NOPMD
	}

}
