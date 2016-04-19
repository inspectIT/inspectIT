package rocks.inspectit.server.indexing;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
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
import org.openjdk.jmh.annotations.Warmup;

import rocks.inspectit.server.indexing.impl.RootBranchFactory;
import rocks.inspectit.server.indexing.impl.RootBranchFactory.RootBranch;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.processor.impl.CacheIdGeneratorCmrProcessor;
import rocks.inspectit.server.processor.impl.IndexerCmrProcessor;
import rocks.inspectit.server.processor.impl.InvocationModifierCmrProcessor;
import rocks.inspectit.server.util.CacheIdGenerator;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.indexing.impl.IndexQuery;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.InvocationSequenceDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.query.factory.impl.TimerDataQueryFactory;
import rocks.inspectit.shared.cs.indexing.query.provider.impl.IndexQueryProvider;
import rocks.inspectit.shared.cs.indexing.restriction.IIndexQueryRestrictionProcessor;
import rocks.inspectit.shared.cs.indexing.restriction.impl.CachingIndexQueryRestrictionProcessor;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
@Fork(2)
@State(Scope.Thread)
public class IndexingTreePerfTest {

	/**
	 * Indexing tree under test.
	 */
	private RootBranch<DefaultData> indexingTree;

	/**
	 * Number of invocations to be added to the indexing tree.
	 */
	@Param({ "1000", "10000" })
	private int invocations;

	/**
	 * Number of invocations to be added to the indexing tree.
	 */
	@Param({ "500", "1000" })
	private int children;

	/**
	 * Number of different agents to simulate.
	 */
	@Param({ "2" })
	private int agents;

	/**
	 * Number of different sensors to simulate.
	 */
	@Param({ "10" })
	private int sensors;

	/**
	 * Number of different methods to simulate.
	 */
	@Param({ "100" })
	private int methods;

	/**
	 * Spread of data in duration of 1 hour.
	 */
	@Param({ "3600000" })
	private int timestampSpread;

	/**
	 * Aggregated timer data query.
	 */
	private IndexQuery aggregatedTimerDataQuery;

	/**
	 * Aggregated timer data query for specific time-frame.
	 */
	private IndexQuery aggregatedTimerDataQuery15MinsTimeframe;

	/**
	 * Aggregated timer data query for single method id.
	 */
	private IndexQuery aggregatedTimerDataQueryMethod;

	/**
	 * Invocation overview query.
	 */
	private IndexQuery invocationOverviewQuery;


	/**
	 * ForkJoinPool
	 */
	private ForkJoinPool forkJoinPool;

	/**
	 * Number of processors used by the forkJoinPool
	 */
	@Param({"4", "8"})
	private int numberOfProcessors;

	/**
	 * Set up, prepare indexing tree.
	 */
	@Setup(Level.Trial)
	public void initIndexingTree() throws Exception {
		forkJoinPool = new ForkJoinPool(numberOfProcessors);

		RootBranchFactory rootBranchFactory = new RootBranchFactory();
		indexingTree = rootBranchFactory.getObject();

		CacheIdGeneratorCmrProcessor idProcessor = new CacheIdGeneratorCmrProcessor();
		idProcessor.setCacheIdGenerator(new CacheIdGenerator());
		IndexerCmrProcessor indexerProcessor = new IndexerCmrProcessor();
		indexerProcessor.setIndexingTree(indexingTree);
		List<AbstractCmrDataProcessor> chained = new ArrayList<AbstractCmrDataProcessor>(2);
		chained.add(idProcessor);
		chained.add(indexerProcessor);
		InvocationModifierCmrProcessor invocationProcessor = new InvocationModifierCmrProcessor(chained);

		for (int i = 0; i < invocations; i++) {
			InvocationSequenceData data = getInvocationSequenceDataInstance(children);
			Collection<DefaultData> toProcess = Collections.<DefaultData> singleton(data);
			idProcessor.process(toProcess, null);
			invocationProcessor.process(toProcess, null);
			indexingTree.put(data);
		}

		// prepare queries
		Random random = new Random();
		long platformIdent = getRandomPlatformIdent(random);
		long methodIdent = getRandomMethodIdent(random);

		final IIndexQueryRestrictionProcessor restrictionProcessor = new CachingIndexQueryRestrictionProcessor();
		IndexQueryProvider indexQueryProvider = new IndexQueryProvider() {

			@Override
			public IndexQuery createNewIndexQuery() {
				IndexQuery indexQuery = new IndexQuery();
				indexQuery.setRestrictionProcessor(restrictionProcessor);
				return indexQuery;
			}
		};

		// timer data
		TimerDataQueryFactory<IndexQuery> timerDataQueryFactory = new TimerDataQueryFactory<IndexQuery>();
		timerDataQueryFactory.setIndexQueryProvider(indexQueryProvider);

		aggregatedTimerDataQuery = timerDataQueryFactory.getAggregatedTimerDataQuery(new TimerData(null, platformIdent, 0, 0), null, null);
		aggregatedTimerDataQueryMethod = timerDataQueryFactory.getAggregatedTimerDataQuery(new TimerData(null, platformIdent, 0, methodIdent), null, null);

		Date fromDate;
		Date toDate;
		long time15mins = 15000;
		Date date = new Date(getRandomTimestamp(random));
		if (date.after(new Date(System.currentTimeMillis() - 15000))) {
			toDate = date;
			fromDate = new Date(date.getTime() - time15mins);
		} else {
			fromDate = date;
			toDate = new Date(date.getTime() + time15mins);
		}
		aggregatedTimerDataQuery15MinsTimeframe = timerDataQueryFactory.getAggregatedTimerDataQuery(new TimerData(null, platformIdent, 0, 0), fromDate, toDate);

		// invocation data
		InvocationSequenceDataQueryFactory<IndexQuery> invocationSequenceDataQueryFactory = new InvocationSequenceDataQueryFactory<IndexQuery>();
		invocationSequenceDataQueryFactory.setIndexQueryProvider(indexQueryProvider);

		invocationOverviewQuery = invocationSequenceDataQueryFactory.getInvocationSequenceOverview(platformIdent, 0, null, null);
	}

	// Query fork&join benchmarks
	@Benchmark
	public List<DefaultData> queryTimerDataForkJoin() {
		return indexingTree.query(aggregatedTimerDataQuery, forkJoinPool);
	}

	@Benchmark
	public List<DefaultData> queryTimerData15MinsTimeframeForkJoin() {
		return indexingTree.query(aggregatedTimerDataQuery15MinsTimeframe, forkJoinPool);
	}

	@Benchmark
	public List<DefaultData> queryTimerDataMethodForkJoin() {
		return indexingTree.query(aggregatedTimerDataQueryMethod, forkJoinPool);
	}

	@Benchmark
	public List<DefaultData> queryInvocationOverviewForkJoin() {
		return indexingTree.query(invocationOverviewQuery, forkJoinPool);
	}

	// Query benchmarks without fork&join
	@Benchmark
	public List<DefaultData> queryTimerData() {
		return indexingTree.query(aggregatedTimerDataQuery);
	}

	@Benchmark
	public List<DefaultData> queryTimerData15MinsTimeframe() {
		return indexingTree.query(aggregatedTimerDataQuery15MinsTimeframe);
	}

	@Benchmark
	public List<DefaultData> queryTimerDataMethod() {
		return indexingTree.query(aggregatedTimerDataQueryMethod);
	}

	@Benchmark
	public List<DefaultData> queryInvocationOverview() {
		return indexingTree.query(invocationOverviewQuery);
	}

	// private helpers
	private InvocationSequenceData getInvocationSequenceDataInstance(int childCount) {
		Random random = new Random();
		InvocationSequenceData invData = new InvocationSequenceData(new Timestamp(getRandomTimestamp(random)), getRandomPlatformIdent(random), getRandomSensorIdent(random),
				getRandomMethodIdent(random));

		setRadnomDataObject(invData, random);

		if (childCount == 0) {
			return invData;
		}

		List<InvocationSequenceData> children = new ArrayList<InvocationSequenceData>();
		for (int i = 0; i < childCount;) {
			int childCountForChild = childCount / 10;
			if ((childCountForChild + i + 1) > childCount) {
				childCountForChild = childCount - i - 1;
			}
			InvocationSequenceData child = getInvocationSequenceDataInstance(childCountForChild);
			setRadnomDataObject(child, random);
			child.setParentSequence(invData);
			children.add(child);
			i += childCountForChild + 1;

		}
		invData.setChildCount(childCount);
		invData.setNestedSequences(children);
		return invData;
	}

	private long getRandomPlatformIdent(Random random) {
		return 1L + random.nextInt(agents);
	}

	private long getRandomSensorIdent(Random random) {
		return 1L + random.nextInt(sensors);
	}

	private long getRandomMethodIdent(Random random) {
		return 1L + random.nextInt(methods);
	}

	private long getRandomTimestamp(Random random) {
		return System.currentTimeMillis() - random.nextInt(timestampSpread);
	}

	private void setRadnomDataObject(InvocationSequenceData invocationSequenceData, Random random) {
		int objectSplit = random.nextInt(100);

		// http 5%, exceptions 5%, sqls 25%, timers 65%
		if (objectSplit < 5) {
			HttpTimerData httpTimerData = new HttpTimerData(new Timestamp(getRandomTimestamp(random)), getRandomPlatformIdent(random), getRandomSensorIdent(random), getRandomMethodIdent(random));
			setTime(httpTimerData);
			invocationSequenceData.setTimerData(httpTimerData);
		} else if (objectSplit < 10) {
			ExceptionSensorData exData = new ExceptionSensorData(new Timestamp(getRandomTimestamp(random)), getRandomPlatformIdent(random), getRandomSensorIdent(random), getRandomMethodIdent(random));
			invocationSequenceData.setExceptionSensorDataObjects(Collections.singletonList(exData));
		} else if (objectSplit < 35) {
			SqlStatementData sqlData = new SqlStatementData(new Timestamp(getRandomTimestamp(random)), getRandomPlatformIdent(random), getRandomSensorIdent(random), getRandomMethodIdent(random));
			setTime(sqlData);
			invocationSequenceData.setSqlStatementData(sqlData);
		} else {
			TimerData timerData = new TimerData(new Timestamp(getRandomTimestamp(random)), getRandomPlatformIdent(random), getRandomSensorIdent(random), getRandomMethodIdent(random));
			setTime(timerData);
			invocationSequenceData.setTimerData(timerData);
		}
	}

	private void setTime(TimerData timerData) {
		timerData.setCount(1L);
	}

}
