package rocks.inspectit.agent.java.sensor.method.special;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.SpanImpl;
import rocks.inspectit.agent.java.sdk.opentracing.internal.impl.TracerImpl;
import rocks.inspectit.agent.java.tracing.core.async.SpanStore;

/**
 * Test the performance of the {@link ExecutorIntercepterHook}.
 *
 * @author Marius Oehler
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class ExecutorIntercepterHookPerfTest {

	private TracerImpl tracerNotActive;

	private TracerImpl tracerActive;

	private ExecutorIntercepterHook hookNotActive;

	private ExecutorIntercepterHook hookActive;

	private long methodId = 0L;

	private Object object = new Object();

	private SpecialSensorConfig ssc = new SpecialSensorConfig();

	private Object[] parameterIsSpanStore = new Object[] { new SpanStore() };

	private Object[] parameter;

	private SpanImpl span;

	@Setup(Level.Iteration)
	public void setup() {
		tracerNotActive = new TracerImpl();
		tracerActive = new TracerImpl();

		span = tracerActive.buildSpan().build();
		span.start();

		hookNotActive = new ExecutorIntercepterHook(tracerNotActive);
		hookActive = new ExecutorIntercepterHook(tracerActive);
	}

	@TearDown(Level.Iteration)
	public void tearDown() {
		span.finish();
	}

	@Benchmark
	public void baseline() {
		parameter = new Object[] { new Runnable() {
			@Override
			public void run() {
			}
		} };
	}

	@Benchmark
	public void parameterIsSpanStore(Blackhole bh) {
		baseline();

		Object result = hookNotActive.beforeBody(methodId, object, parameterIsSpanStore, ssc);
		bh.consume(result);
	}

	@Benchmark
	public void tracerIsNotActive(Blackhole bh) {
		baseline();

		Object result = hookNotActive.beforeBody(methodId, object, parameter, ssc);
		bh.consume(result);
	}

	@Benchmark
	public void tracerIsActive(Blackhole bh) {
		baseline();

		Object result = hookActive.beforeBody(methodId, object, parameter, ssc);
		bh.consume(result);
	}
}
