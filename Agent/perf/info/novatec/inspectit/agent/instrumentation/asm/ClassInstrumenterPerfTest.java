package info.novatec.inspectit.agent.instrumentation.asm;

import info.novatec.inspectit.agent.instrumentation.asm.ClassInstrumenter;
import info.novatec.inspectit.agent.instrumentation.asm.LoaderAwareClassWriter;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
import info.novatec.inspectit.instrumentation.config.impl.RegisteredSensorConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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

	private int instrumented;

	@Setup(Level.Invocation)
	public void init() {
		instrumented = 0;
	}

	/**
	 * Instrumenting methods of class.
	 */
	@Benchmark
	public void instrument() throws InterruptedException, IOException {
		ClassReader classReader = new ClassReader(clazz);
		ClassWriter classWriter = new LoaderAwareClassWriter(classReader, ClassWriter.COMPUTE_FRAMES, null);
		ClassInstrumenter classInstrumenter = new ClassInstrumenter(classWriter) {

			@Override
			MethodInstrumentationConfig shouldInstrument(String name, String desc) {
				instrumented++;
				if (instrumented <= maxMethods) {
					MethodInstrumentationConfig config = new MethodInstrumentationConfig();
					RegisteredSensorConfig registeredSensorConfig = new RegisteredSensorConfig(config);
					registeredSensorConfig.setEnhancedExceptionSensor(enhancedExceptionSensor);
					config.setRegisteredSensorConfig(registeredSensorConfig);
					return config;
				}
				return null;
			}
		};

		classReader.accept(classInstrumenter, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
	}
}
