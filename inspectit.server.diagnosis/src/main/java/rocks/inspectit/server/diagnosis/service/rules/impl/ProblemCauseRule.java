package rocks.inspectit.server.diagnosis.service.rules.impl;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.server.diagnosis.service.aggregation.DiagnosisDataAggregator;
import rocks.inspectit.server.diagnosis.service.data.CauseCluster;
import rocks.inspectit.server.diagnosis.service.data.DiagnosisHelper;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.communication.data.diagnosis.RootCause;


/**
 * Rule for detecting <code>Root Causes</code> within an {@link InvocationSequenceData}. One
 * <code>Root Cause</code> is a method that characterizes a performance problem, hence, whose
 * exclusive time is very high. The <code>Root Causes</code> are aggregated to an object of type
 * {@link AggregatedDiagnosisData}. This rule is triggered fourth in the rule pipeline.
 *
 * @author Alexander Wert, Alper Hidiroglu
 *
 */
@Rule(name = "ProblemCauseRule")
public class ProblemCauseRule {

	/**
	 * A <code>Root Cause</code> candidate is put into a <code>Root Cause</code> object, if the
	 * cumulative exclusive time of already found <code>Root Causes</code> is lower than 80 percent
	 * of the <code>Problem Context's</code> duration.
	 */
	private static final Double PROPORTION = 0.8;

	/**
	 * Injection of a <code>CauseCluster</code>. The common context of this cluster is the
	 * <code>Problem Context</code>.
	 */
	@TagValue(type = RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT)
	private CauseCluster problemContext;

	/**
	 * These are the cause invocations of the <code>Problem Context</code>.
	 */
	private List<InvocationSequenceData> causeCandidates;

	/**
	 * Accumulated time for all the invocations of the causeCandidates.
	 */
	private double sumExclusiveTime = 0.0;

	/**
	 * The {@link AggregatedDiagnosisData} which contains all the rootCauses.
	 */
	private AggregatedDiagnosisData rootCause = null;

	/**
	 * Instance of a {@link DiagnosisDataAggregator} to aggregate the invocations to the rootCause.
	 */
	private DiagnosisDataAggregator diagnosisDataAggregator = DiagnosisDataAggregator.getInstance();

	/**
	 * Rule execution.
	 *
	 * @return DIAGNOSIS_TAG_PROBLEM_CAUSE
	 */
	@Action(resultTag = RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE)
	public AggregatedDiagnosisData action() {
		causeCandidates = problemContext.getCauseInvocations();

		int causeCandidatesChecked = getRootCauses();
		threeSigmaApproach(causeCandidatesChecked);

		return rootCause;
	}

	/**
	 * Aggregate all the invocations that are considered as rootCause to the object
	 * {@link RootCause}.
	 *
	 * @return Returns the number of candidates that are already checked.
	 */
	private int getRootCauses() {
		int causeCandidatesChecked = 0;
		double problemContextCommonContextDuration = InvocationSequenceDataHelper.calculateDuration(problemContext.getCommonContext());
		// Root Cause candidates are put into one Root Cause as long as the condition is true.
		while ((sumExclusiveTime < (PROPORTION * problemContextCommonContextDuration)) && (causeCandidatesChecked < causeCandidates.size())) {
			InvocationSequenceData invocation = causeCandidates.get(causeCandidatesChecked);
			if (null == rootCause) {
				rootCause = diagnosisDataAggregator.getAggregatedDiagnosisData(invocation);
			} else {
				diagnosisDataAggregator.aggregate(rootCause, invocation);
			}
			sumExclusiveTime += DiagnosisHelper.getExclusiveDuration(invocation);
			causeCandidatesChecked++;
		}
		return causeCandidatesChecked;
	}

	/**
	 * If there are Root Cause candidates left that were not considered for the Root Cause before,
	 * the Three-Sigma Limit approach checks if these candidates can also be considered for the Root
	 * Cause.
	 *
	 * @param causeCandidatesChecked
	 *            Number of instances of causeCandidates that have been checked already.
	 */
	private void threeSigmaApproach(int causeCandidatesChecked) {
		if ((causeCandidatesChecked > 1) && (causeCandidatesChecked < causeCandidates.size())) {
			double mean = sumExclusiveTime / causeCandidatesChecked;
			double[] durations = new double[rootCause.size()];
			int i = 0;
			for (InvocationSequenceData invocation : rootCause.getRawInvocationsSequenceElements()) {
				durations[i] = DiagnosisHelper.getExclusiveDuration(invocation);
				i++;
			}

			StandardDeviation standardDeviation = new StandardDeviation(false);
			double sd = standardDeviation.evaluate(durations, mean);
			double lowerThreshold = mean - (3 * sd);

			for (int j = causeCandidatesChecked; j < causeCandidates.size(); j++) {
				InvocationSequenceData invocation = causeCandidates.get(j);
				double duration = DiagnosisHelper.getExclusiveDuration(invocation);
				if (duration > lowerThreshold) {
					diagnosisDataAggregator.aggregate(rootCause, invocation);
				} else {
					break;
				}
			}
		}
	}
}