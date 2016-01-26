package info.novatec.inspectit.cmr.instrumentation.classcache.index;

import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
@SuppressWarnings("PMD")
public class FQNIndexePerfTest {

	private FQNIndexer<Type> indexer;

	private List<String> classes;

	@Setup
	public void init() throws IOException {
		indexer = new FQNIndexer<>();
		classes = new ArrayList<>(10000);

		Path p = Paths.get("perf", "info", "novatec", "inspectit", "cmr", "instrumentation", "classcache", "index", "classes");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(p, StandardOpenOption.READ)))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("[Loaded")) {
					int from = line.indexOf(' ');
					int to = line.indexOf(' ', from + 1);
					String className = line.substring(from + 1, to);
					classes.add(className);
					indexer.index(new ClassType(className));
				}
			}
		}
	}

	/**
	 * Find by class.
	 */
	@Benchmark
	public void findExact() {
		if (null == indexer.lookup(getRandomClassName())) {
			throw new RuntimeException("Performance test not valid, class not found in indexer.");
		}
	}

	/**
	 * Baseline for find by wild-card.
	 */
	@Benchmark
	public String findStartWithBaseline() {
		return getRandomClassName();
	}

	/**
	 * Find by wild-card.
	 */
	@Benchmark
	public void findStartWith() {
		String className = getRandomClassName();
		String wildCard = className.substring(0, RandomUtils.nextInt(className.length()));
		if (CollectionUtils.isEmpty(indexer.findStartsWith(wildCard))) {
			throw new RuntimeException("Performance test not valid, classes not found by wild card in indexer.");
		}
	}

	private String getRandomClassName() {
		return classes.get(RandomUtils.nextInt(classes.size()));
	}

}
