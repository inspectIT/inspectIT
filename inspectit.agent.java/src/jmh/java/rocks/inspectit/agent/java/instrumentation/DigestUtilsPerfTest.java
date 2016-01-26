package rocks.inspectit.agent.java.instrumentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.google.common.io.ByteStreams;

/**
 * Performance test for the {@link DigestUtils#sha256(byte[])} method that we use in the byte code
 * analyzer.
 *
 * @author Ivan Senic
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(value = 1)
@State(Scope.Thread)
public class DigestUtilsPerfTest {

	@Param({ "java.lang.String", "java.lang.Object", "java.lang.Comparable" })
	private String clazz;

	@Benchmark
	public String sha256Hex() throws InterruptedException, IOException {
		return DigestUtils.sha256Hex(getByteCode(clazz));
	}

	@Benchmark
	public byte[] baseline() {
		return getByteCode(clazz);
	}

	public byte[] getByteCode(String className) {
		InputStream is = null;
		try {
			is = ClassLoader.getSystemResourceAsStream(className.replace('.', '/') + ".class");
			if (null == is) {
				// nothing we can do here
				return null;
			}
			return ByteStreams.toByteArray(is);
		} catch (IOException e) {
			return null;
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) { // NOPMD //NOCHK
					// ignore
				}
			}
		}
	}

}
