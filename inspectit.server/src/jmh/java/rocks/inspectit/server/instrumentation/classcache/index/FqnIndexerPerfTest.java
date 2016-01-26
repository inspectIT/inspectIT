package rocks.inspectit.server.instrumentation.classcache.index;

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

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;

/**
 * Performance test for the {@link FqnIndexer} class.
 * <p>
 * In this test we test two methods {@link FqnIndexer#lookup(String)} and
 * {@link FqnIndexer#findStartsWith(String)}. As the setup for this method we are putting around 5K
 * real class names into the indexer. These classes can be found in the <i>classes</i> file in the
 * same package as this class.
 *
 * @author Ivan Senic
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
@State(Scope.Thread)
@SuppressWarnings("PMD")
public class FqnIndexerPerfTest {

	private FqnIndexer<Type> indexer;

	private List<String> classes;

	@Setup
	public void init() throws IOException {
		indexer = new FqnIndexer<>();
		classes = new ArrayList<>(10000);

		Path p = Paths.get("src", "jmh", "resources", "rocks", "inspectit", "server", "instrumentation", "classcache", "index", "classNamesFqnIndexerPerfTest");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(p, StandardOpenOption.READ)))) {
			String line;
			while ((line = br.readLine()) != null) {
				String className = line.trim();
				classes.add(className);
				indexer.index(new ClassType(className));
			}
		}
	}

	/**
	 * Baseline for both find methods.
	 */
	@Benchmark
	public String findBaseline() {
		return getRandomClassName();
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
