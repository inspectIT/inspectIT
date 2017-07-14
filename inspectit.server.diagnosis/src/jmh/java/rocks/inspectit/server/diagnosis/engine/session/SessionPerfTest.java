package rocks.inspectit.server.diagnosis.engine.session;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.rule.factory.Rules;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.server.diagnosis.service.rules.impl.CauseStructureRule;
import rocks.inspectit.server.diagnosis.service.rules.impl.GlobalContextRule;
import rocks.inspectit.server.diagnosis.service.rules.impl.ProblemCauseRule;
import rocks.inspectit.server.diagnosis.service.rules.impl.ProblemContextRule;
import rocks.inspectit.server.diagnosis.service.rules.impl.TimeWastingOperationsRule;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;

/**
 * Tests the throughput of the DiagnosisService.
 *
 * @author Tobias Angerstein
 *
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 5)
@Fork(2)
@State(Scope.Thread)
public class SessionPerfTest {

	/**
	 * Default CPU duration which is used in {@link TimerData}.
	 */
	private static final int CPU_DURATION = 20;

	/**
	 * Exclusive duration of parent.
	 */
	private static final int EXCLUSIVE_DURATION_OF_PARENT = 50;

	/**
	 * Exclusive duration of child.
	 */
	private static final int EXCLUSIVE_DURATION_OF_CHILD = 20;

	/**
	 * Exclusive duration of grand child.
	 */
	private static final int EXCLUSIVE_DURATION_OF_GRAND_CHILD = 200;

	/**
	 * Baseline of diagnosis.
	 */
	private static final double DIAGNOSIS_BASELINE = 1000;

	/**
	 * Number of calls.
	 */
	@Param({ "1000", "2000", "3000", "4000" })
	private int numberOfCalls;

	/**
	 * {@link InvocationSequenceData} with iterative calls.
	 */
	private InvocationSequenceData rootIterativeInvocationSequence;

	/**
	 * {@link InvocationSequenceData} with recursive calls.
	 */
	private InvocationSequenceData rootRecursiveInvocationSequence;

	/**
	 * {@link Session} under test.
	 */
	private Session<InvocationSequenceData, DefaultSessionResult<InvocationSequenceData>> session;

	/**
	 * {@link InvocationSequenceData} with n+1 sql database calls.
	 */
	private InvocationSequenceData rootNplusOneAwareInvocationSequenceData;;

	/**
	 * Prepare diagnosis engine.
	 *
	 * @throws RuleDefinitionException
	 */
	@Setup(Level.Trial)
	public void init() {
		// create iterative InvocationSequenceData
		rootIterativeInvocationSequence = getIterativeInvocationSequence(
				new InvocationSequenceData(new Timestamp(System.currentTimeMillis()), 0, 0, 0), numberOfCalls);

		// create recursive InvocationSequenceData
		InvocationSequenceData leafRecursiveInvocationSequence = new InvocationSequenceData(
				new Timestamp(System.currentTimeMillis()), 0, 0, 1000);
		rootRecursiveInvocationSequence = getRecursiveInvocationSequence(leafRecursiveInvocationSequence, 0,
				numberOfCalls);

		// create NplusOneAwareInvocationSequenceData
		rootNplusOneAwareInvocationSequenceData = getNplusOneAwareInvocationSequence(
				new InvocationSequenceData(new Timestamp(System.currentTimeMillis()), 0, 0, 0), numberOfCalls);

		// create Session
		try {
			session = new Session<>(
					Rules.define(GlobalContextRule.class, TimeWastingOperationsRule.class, ProblemContextRule.class,
							ProblemCauseRule.class, CauseStructureRule.class),
					new DefaultSessionResultCollector<InvocationSequenceData>());
		} catch (RuleDefinitionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tests the rules with an iterative call.
	 *
	 * @throws Exception
	 */
	@Benchmark
	public void testDiagnosisServiceWithIterativeInvocationSequences() {
		try {
			session.activate(rootIterativeInvocationSequence,
					Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, DIAGNOSIS_BASELINE));
			session.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		session.passivate();
	}

	/**
	 * Tests the rules with a recursive call.
	 *
	 * @throws Exception
	 */
	@Benchmark
	public void testDiagnosisServiceWithRecursiveInvocationSequences() {
		try {
			session.activate(rootRecursiveInvocationSequence,
					Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, DIAGNOSIS_BASELINE));
			session.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		session.passivate();
	}

	/**
	 * Tests the rules with an iterative call, which includes a NplusOne.
	 * Database call.
	 *
	 * @throws Exception
	 */
	@Benchmark
	public void testDiagnosisServiceWithNplusOneProblem() {
		try {
			session.activate(rootNplusOneAwareInvocationSequenceData,
					Collections.singletonMap(RuleConstants.DIAGNOSIS_VAR_BASELINE, DIAGNOSIS_BASELINE));
			DefaultSessionResult<InvocationSequenceData> sdfsdf = session.call();
			sdfsdf.getEndTags();
		} catch (Exception e) {
			e.printStackTrace();
		}
		session.passivate();
	}

	/**
	 * Creates an invocationSequence with recursive invocations.
	 *
	 * @param leaf
	 *            The leaf of the recursive invocation
	 * @param currentHight
	 *            The current height of the invocation sequence (should be
	 *            initially set to 0)
	 * @param maxHeight
	 *            The max height of the invocation sequence
	 * @return {@link InvocationSequenceData} with maxHeight- children
	 */
	private InvocationSequenceData getRecursiveInvocationSequence(InvocationSequenceData leaf, int currentHight,
			int maxHeight) {
		if (currentHight >= maxHeight) {
			return leaf;
		} else {
			InvocationSequenceData parent = new InvocationSequenceData(
					new Timestamp(System.currentTimeMillis() + (leaf.getTimeStamp().getTime() - 100)), 0, 0,
					leaf.getMethodIdent());
			leaf.setParentSequence(parent);
			parent.setDuration(leaf.getDuration() + EXCLUSIVE_DURATION_OF_PARENT);

			// Create TimerData
			TimerData timerData = new TimerData(parent.getTimeStamp(), parent.getPlatformIdent(),
					parent.getSensorTypeIdent(), parent.getMethodIdent());
			timerData.addDuration(leaf.getDuration() + EXCLUSIVE_DURATION_OF_PARENT);
			timerData.setExclusiveDuration(EXCLUSIVE_DURATION_OF_PARENT);
			timerData.addCpuDuration(20);
			timerData.calculateExclusiveMin(EXCLUSIVE_DURATION_OF_PARENT);
			parent.setTimerData(timerData);

			parent.getNestedSequences().add(leaf);
			return getRecursiveInvocationSequence(parent, currentHight + 1, maxHeight);
		}
	}

	/**
	 * Creates an invocationSequence which has a huge number of iterative calls.
	 *
	 * @param parentSequence
	 *            The root invocation Sequence
	 * @param numberOfChildren
	 *            the number of iterative children
	 * @return the parent sequence with added children
	 */
	private InvocationSequenceData getIterativeInvocationSequence(InvocationSequenceData parentSequence,
			int numberOfChildren) {
		List<InvocationSequenceData> nestedSequencesOfParent = new ArrayList<InvocationSequenceData>();

		double durationOfParent = 0;
		int splitFactor = 2;

		// create list of childs
		for (int i = 0; i < splitFactor; i++) {
			InvocationSequenceData childSequence = new InvocationSequenceData(
					new Timestamp(System.currentTimeMillis() + (i + 100)), 0, 0, parentSequence.getMethodIdent() + i);
			childSequence.setParentSequence(parentSequence);

			List<InvocationSequenceData> nestedSequencesOfChild = new ArrayList<InvocationSequenceData>();

			double durationOfChild = 0;

			// create list of grand childs
			for (int j = 0; j < (numberOfChildren / splitFactor); j++) {
				InvocationSequenceData grandChildSequence = new InvocationSequenceData(
						new Timestamp(System.currentTimeMillis() + (j + 100)), 0, 0,
						childSequence.getMethodIdent() + 1);
				grandChildSequence.setParentSequence(childSequence);
				grandChildSequence.setDuration(EXCLUSIVE_DURATION_OF_GRAND_CHILD);

				// Create TimerData for grandchild
				TimerData timerDataGrandChild = new TimerData(grandChildSequence.getTimeStamp(),
						grandChildSequence.getPlatformIdent(), grandChildSequence.getSensorTypeIdent(),
						grandChildSequence.getMethodIdent());
				timerDataGrandChild.addDuration(EXCLUSIVE_DURATION_OF_GRAND_CHILD);
				timerDataGrandChild.setExclusiveDuration(EXCLUSIVE_DURATION_OF_GRAND_CHILD);
				timerDataGrandChild.addCpuDuration(CPU_DURATION);
				timerDataGrandChild.calculateExclusiveMin(EXCLUSIVE_DURATION_OF_GRAND_CHILD);

				grandChildSequence.setTimerData(timerDataGrandChild);
				nestedSequencesOfChild.add(grandChildSequence);
				durationOfChild += EXCLUSIVE_DURATION_OF_GRAND_CHILD;
			}

			// Add exclusive duration
			durationOfChild += EXCLUSIVE_DURATION_OF_CHILD;

			childSequence.setDuration(durationOfChild);
			childSequence.getNestedSequences().addAll(nestedSequencesOfChild);
			nestedSequencesOfParent.add(childSequence);
			durationOfParent += durationOfChild;

			// Create TimerData for child
			TimerData timerDataChild = new TimerData(childSequence.getTimeStamp(), childSequence.getPlatformIdent(),
					childSequence.getSensorTypeIdent(), childSequence.getMethodIdent());
			timerDataChild.addDuration(durationOfChild);
			timerDataChild.setExclusiveDuration(EXCLUSIVE_DURATION_OF_CHILD);
			timerDataChild.addCpuDuration(CPU_DURATION);
			timerDataChild.calculateExclusiveMin(EXCLUSIVE_DURATION_OF_CHILD);
			childSequence.setTimerData(timerDataChild);
		}
		// Add exclusive duration
		durationOfParent += EXCLUSIVE_DURATION_OF_PARENT;

		parentSequence.getNestedSequences().addAll(nestedSequencesOfParent);
		parentSequence.setDuration(durationOfParent);

		// Create TimerData for parent
		TimerData timerDataParent = new TimerData(parentSequence.getTimeStamp(), parentSequence.getPlatformIdent(),
				parentSequence.getSensorTypeIdent(), parentSequence.getMethodIdent());
		timerDataParent.addDuration(durationOfParent);
		timerDataParent.setExclusiveDuration(EXCLUSIVE_DURATION_OF_PARENT);
		timerDataParent.addCpuDuration(CPU_DURATION);
		timerDataParent.calculateExclusiveMin(EXCLUSIVE_DURATION_OF_PARENT);
		parentSequence.setTimerData(timerDataParent);

		return parentSequence;
	}

	/**
	 * Creates an invocationSequence which includes two n+1 database calls.
	 *
	 * @param parentSequence
	 *            the given root invocation sequence
	 * @param numberOfChildren
	 *            the number of grandchildren
	 * @return {@link InvocationSequenceData}
	 */
	private InvocationSequenceData getNplusOneAwareInvocationSequence(InvocationSequenceData parentSequence,
			int numberOfChildren) {
		List<InvocationSequenceData> nestedSequencesOfParent = new ArrayList<InvocationSequenceData>();

		double durationOfParent = 0;
		int splitFactor = 2;

		// create list of childs
		for (int i = 0; i < splitFactor; i++) {
			InvocationSequenceData childSequence = new InvocationSequenceData(
					new Timestamp(System.currentTimeMillis() + (i + 100)), 0, 0, parentSequence.getMethodIdent() + i);
			childSequence.setParentSequence(parentSequence);
			List<InvocationSequenceData> nestedSequencesOfChild = new ArrayList<InvocationSequenceData>();
			double durationOfChild = 0;

			// create list of grand childs
			for (int j = 0; j < ((numberOfChildren / splitFactor) - 1); j++) {
				InvocationSequenceData grandChildSequence = getInvocationSequenceWithSqlData(childSequence,
						"SELECT ID FROM table" + i, EXCLUSIVE_DURATION_OF_GRAND_CHILD,
						new Timestamp(System.currentTimeMillis() + (j + 100)));
				nestedSequencesOfChild.add(grandChildSequence);
				durationOfChild += EXCLUSIVE_DURATION_OF_GRAND_CHILD;
			}

			// Add additional invocationSequence (n+1 st). This invocation
			// sequence is needed to trigger the ProblemCauseNPlusOneDB
			InvocationSequenceData grandChildSequence = getInvocationSequenceWithSqlData(childSequence,
					"SELECT * FROM table" + i, DIAGNOSIS_BASELINE + 1000, new Timestamp(System.currentTimeMillis()));
			nestedSequencesOfChild.add(grandChildSequence);
			durationOfChild += DIAGNOSIS_BASELINE + 1000;

			// Add exclusive duration
			durationOfChild += EXCLUSIVE_DURATION_OF_CHILD;
			childSequence.setDuration(durationOfChild);

			childSequence.getNestedSequences().addAll(nestedSequencesOfChild);
			nestedSequencesOfParent.add(childSequence);
			durationOfParent += durationOfChild;

			// Create TimerData for child
			TimerData timerDataChild = new TimerData(childSequence.getTimeStamp(), childSequence.getPlatformIdent(),
					childSequence.getSensorTypeIdent(), childSequence.getMethodIdent());
			timerDataChild.addDuration(durationOfChild);
			timerDataChild.setExclusiveDuration(EXCLUSIVE_DURATION_OF_CHILD);
			timerDataChild.addCpuDuration(CPU_DURATION);
			timerDataChild.calculateExclusiveMin(EXCLUSIVE_DURATION_OF_CHILD);
			childSequence.setTimerData(timerDataChild);
			childSequence.setNestedSqlStatements(true);
		}

		// Add exclusive duration
		durationOfParent += EXCLUSIVE_DURATION_OF_PARENT;
		parentSequence.getNestedSequences().addAll(nestedSequencesOfParent);
		parentSequence.setDuration(durationOfParent);

		// Create TimerData for parent
		TimerData timerDataParent = new TimerData(parentSequence.getTimeStamp(), parentSequence.getPlatformIdent(),
				parentSequence.getSensorTypeIdent(), parentSequence.getMethodIdent());
		timerDataParent.addDuration(durationOfParent);
		timerDataParent.setExclusiveDuration(EXCLUSIVE_DURATION_OF_PARENT);
		timerDataParent.addCpuDuration(CPU_DURATION);
		timerDataParent.calculateExclusiveMin(EXCLUSIVE_DURATION_OF_PARENT);
		parentSequence.setTimerData(timerDataParent);
		parentSequence.setNestedSqlStatements(true);

		return parentSequence;
	}

	/**
	 * Creates an invocationSequence with sqlData.
	 *
	 * @param parentSequence
	 *            Parent of the invocation sequence
	 * @param sql
	 *            sql statement
	 * @param exclusiveDuration
	 *            exlusive Duration
	 * @param timestamp
	 *            Timestamp of the invocation sequence
	 * @return {@link InvocationSequenceData}
	 */
	private InvocationSequenceData getInvocationSequenceWithSqlData(InvocationSequenceData parentSequence, String sql,
			double exclusiveDuration, Timestamp timestamp) {
		InvocationSequenceData grandChildSequence = new InvocationSequenceData(timestamp, 0, 0,
				parentSequence.getMethodIdent() + 1);
		grandChildSequence.setParentSequence(parentSequence);
		grandChildSequence.setDuration(exclusiveDuration);

		// Add sql statement data of additional invocationSequence
		SqlStatementData sqlData = new SqlStatementData(grandChildSequence.getTimeStamp(),
				grandChildSequence.getPlatformIdent(), grandChildSequence.getSensorTypeIdent(),
				grandChildSequence.getMethodIdent());
		sqlData.addDuration(grandChildSequence.getDuration());
		sqlData.addExclusiveDuration(exclusiveDuration);
		sqlData.calculateExclusiveMin(exclusiveDuration);
		sqlData.setDatabaseUrl("jdbc://test");
		sqlData.setSql(sql);
		grandChildSequence.setSqlStatementData(sqlData);
		return grandChildSequence;
	}
}