package rocks.inspectit.agent.java.sensor.method.invocationsequence;

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
import rocks.inspectit.agent.java.util.Timer;

@State(Scope.Benchmark)
@Warmup(iterations = 10, batchSize = 100000)
@Measurement(iterations = 10, batchSize = 10000)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
public class InvocationHookPerfTest extends AbstractHookPerfTest {

	@Param({ "0" })
	public int tokens;

	private InvocationSequenceHook invocationSequenceHook;

	private long nestedMethodId;
	private RegisteredSensorConfig registeredSensorConfig;

	@Setup(Level.Trial)
	public void createRegisteredSensorConfig() {
		registeredSensorConfig = new RegisteredSensorConfig();
		registeredSensorConfig.setSettings(Collections.<String, Object> emptyMap());
	}

	@Override
	@Setup(Level.Iteration)
	public void init(ThreadParams threadParams) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		super.init(threadParams);

		nestedMethodId = methodId + 10;

		invocationSequenceHook = new InvocationSequenceHook(new Timer(), platformManager, coreService, new TracerImpl(), new PropertyAccessor(), new HashMap<String, Object>(), false);
	}

	public void baselineConsumeCPU() {
		Blackhole.consumeCPU(tokens);
	}

	@Benchmark
	public void startAnInvocation() {
		// Blackhole.consumeCPU(tokens);

		invocationSequenceHook.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);
		invocationSequenceHook.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
	}

	@Benchmark
	public void startAnInvocationPlusMethod() {
		// Blackhole.consumeCPU(tokens);

		// invocation sequence
		invocationSequenceHook.beforeBody(methodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);

		// nested method
		invocationSequenceHook.beforeBody(nestedMethodId, SENSOR_ID, TARGET, PARAMS, registeredSensorConfig);
		invocationSequenceHook.firstAfterBody(nestedMethodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, nestedMethodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);

		// stop the invocation sequence
		invocationSequenceHook.firstAfterBody(methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
		invocationSequenceHook.secondAfterBody(coreService, methodId, SENSOR_ID, TARGET, PARAMS, RETURN_VALUE, false, registeredSensorConfig);
	}

	@Override
	@TearDown(Level.Iteration)
	public void cleanUp() throws Exception {
		super.cleanUp();

		invocationSequenceHook = null; // NOPMD
	}

}