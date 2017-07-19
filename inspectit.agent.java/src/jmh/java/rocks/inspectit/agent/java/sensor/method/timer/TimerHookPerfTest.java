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
import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.agent.java.sensor.method.AbstractHookPerfTest;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.data.ParameterContentType;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;

/**
 * JMH Test for {@link TimerHook} in combination with {@link CoreService}.
 *
 * @author Matthias Huber
 *
 */
@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 10000)
@Measurement(iterations = 10, batchSize = 10000)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
public class TimerHookPerfTest extends AbstractHookPerfTest {

	@Param({ "0" })
	public int tokens;

	@Param({ "false" })
	public boolean charting;

	private TimerHook timerHook;
	private TimerHook timerHookNoThreadCPU;

	private RegisteredSensorConfig registeredSensorConfig;
	private RegisteredSensorConfig captureReturnRegisteredSensorConfig;
	private RegisteredSensorConfig captureParameterRegisteredSensorConfig;

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

	@Setup(Level.Trial)
	public void createRegisteredSensorConfigWithReturnValueCapturing() {
		captureReturnRegisteredSensorConfig = new RegisteredSensorConfig();
		captureReturnRegisteredSensorConfig.setSettings(Collections.<String, Object> singletonMap("charting", charting));
		PropertyPathStart propertyPathStart = new PropertyPathStart();
		propertyPathStart.setContentType(ParameterContentType.RETURN);
		captureReturnRegisteredSensorConfig.setPropertyAccessorList(Collections.singletonList(propertyPathStart));
	}

	@Setup(Level.Trial)
	public void createRegisteredSensorConfigWithParameterValueCapturing() {
		captureParameterRegisteredSensorConfig = new RegisteredSensorConfig();
		captureParameterRegisteredSensorConfig.setSettings(Collections.<String, Object> singletonMap("charting", charting));
		PropertyPathStart propertyPathStart = new PropertyPathStart();
		propertyPathStart.setContentType(ParameterContentType.PARAM);
		propertyPathStart.setSignaturePosition(0);
		captureParameterRegisteredSensorConfig.setPropertyAccessorList(Collections.singletonList(propertyPathStart));
	}

	@Override
	@Setup(Level.Iteration)
	public void init(ThreadParams threadParams) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		super.init(threadParams);

		timerHook = new TimerHook(new Timer(), platformManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());
		timerHookNoThreadCPU = new TimerHook(new Timer(), platformManager, new PropertyAccessor(), new HashMap<String, Object>(), ManagementFactory.getThreadMXBean());

		// disable CPU Threading
		Field cpuThreadEnabledField = timerHookNoThreadCPU.getClass().getDeclaredField("enabled");
		cpuThreadEnabledField.setAccessible(true);
		cpuThreadEnabledField.setBoolean(timerHookNoThreadCPU, false);
		cpuThreadEnabledField.setAccessible(false);
	}

	public void baselineConsumeCPU() {
		Blackhole.consumeCPU(tokens);
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
	 * ---- storage is created in the first invocation of the benchmark method. All other
	 * invocations add to the previously created storage. <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	@Benchmark
	public void measureMethod() {
		timerHook.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);
		timerHook.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
	}

	@Benchmark
	public void measureMethodWithReturnValueCapturing() {
		timerHook.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, captureReturnRegisteredSensorConfig);
		timerHook.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, captureReturnRegisteredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, captureReturnRegisteredSensorConfig);
	}

	@Benchmark
	public void measureMethodWithParameterValueCapturing() {
		timerHook.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, captureParameterRegisteredSensorConfig);
		timerHook.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, captureParameterRegisteredSensorConfig);
		timerHook.secondAfterBody(coreService, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, captureParameterRegisteredSensorConfig);
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
	 * ---- storage is created in the first invocation of the benchmark method. All other
	 * invocations add to the previously created storage. <br>
	 * What is not measured in the second-after-body:<br>
	 * - parameter extraction
	 *
	 */
	@Benchmark
	public void measureMethodNoThreadCPU() {
		timerHookNoThreadCPU.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);
		timerHookNoThreadCPU.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
		timerHookNoThreadCPU.secondAfterBody(coreService, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
	}

	@Override
	@TearDown(Level.Iteration)
	public void cleanUp() throws Exception {
		super.cleanUp();

		timerHook = null; // NOPMD
		timerHookNoThreadCPU = null; // NOPMD
	}
}
