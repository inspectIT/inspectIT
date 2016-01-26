package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;
import info.novatec.inspectit.org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
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
import org.openjdk.jmh.annotations.Warmup;

import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;

/**
 * Performance test for the {@link ClassInstrumenter} class using JMH framework.
 *
 * @author Ivan Senic
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 1)
@State(Scope.Thread)
public class ClassInstrumenterPerfTest {

	@Param({ "java.lang.String", "java.lang.Boolean" })
	private String clazz;

	@Param({ "0", "1", "10" })
	private int maxMethods;

	@Param({ "true", "false" })
	private boolean enhancedExceptionSensor;

	private Collection<MethodInstrumentationConfig> configs;

	@Setup(Level.Iteration)
	public void init() throws ClassNotFoundException {
		Class<?> instrumentedClass = Class.forName(clazz);
		Method[] methods = instrumentedClass.getDeclaredMethods();

		configs = new ArrayList<MethodInstrumentationConfig>();
		for (int i = 0; i < maxMethods && methods.length > 0; i++) {
			int index = RandomUtils.nextInt(methods.length);
			Method method = methods[index];
			methods = (Method[]) ArrayUtils.remove(methods, index);

			MethodInstrumentationConfig instrumentationConfig = new MethodInstrumentationConfig();
			instrumentationConfig.setTargetClassFqn(clazz);
			instrumentationConfig.setTargetMethodName(method.getName());
			instrumentationConfig.setReturnType(method.getReturnType().getCanonicalName());
			SensorInstrumentationPoint registeredSensorConfig = new SensorInstrumentationPoint();
			instrumentationConfig.setSensorInstrumentationPoint(registeredSensorConfig);
			configs.add(instrumentationConfig);
		}
	}

	/**
	 * Instrumenting methods of class.
	 */
	@Benchmark
	public void instrument() throws InterruptedException, IOException {
		ClassReader classReader = new ClassReader(clazz);
		LoaderAwareClassWriter classWriter = new LoaderAwareClassWriter(classReader, ClassWriter.COMPUTE_FRAMES, null);
		ClassInstrumenter classInstrumenter = new ClassInstrumenter(classWriter, new ArrayList<MethodInstrumentationConfig>(configs), enhancedExceptionSensor);
		classReader.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
	}
}
