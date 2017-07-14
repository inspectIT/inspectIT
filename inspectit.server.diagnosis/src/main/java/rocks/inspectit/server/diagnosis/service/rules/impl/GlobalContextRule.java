package rocks.inspectit.server.diagnosis.service.rules.impl;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.SessionVariable;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.tag.Tags;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * Rule for detecting the <code>Global Context</code> within an {@link InvocationSequenceData}. The
 * <code>Global Context</code> is the deepest node in the invocation tree that subsumes all
 * performance problems. This rule is triggered first in the rule pipeline. With the detection of
 * the <code>Global Context</code>, the next rules will further analyze only the problematic area
 * within the invocation tree.
 *
 * @author Alexander Wert, Alper Hidiroglu
 *
 */
@Rule(name = "GlobalContextRule")
public class GlobalContextRule {

	/**
	 * The duration of a node has to be higher than 80 percent of the trace duration in order to be
	 * a dominating call in the invocation tree.
	 */
	private static final double PROPORTION = 0.8;

	/**
	 * The duration of a node subtracted from the trace duration has to be lower than the baseline
	 * (= 1000) in order to be a dominating call in the invocation tree.
	 */
	@SessionVariable(name = RuleConstants.DIAGNOSIS_VAR_BASELINE, optional = false)
	private double baseline;

	/**
	 * The {@link InvocationSequenceData} that is analyzed.
	 */
	@TagValue(type = Tags.ROOT_TAG)
	private InvocationSequenceData invocationSequenceRoot;

	/**
	 * Rule execution.
	 *
	 * @return DIAGNOSIS_TAG_GLOBAL_CONTEXT
	 */
	@Action(resultTag = RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT)
	public InvocationSequenceData action() {
		InvocationSequenceData childWithMaxDuration = invocationSequenceRoot;
		InvocationSequenceData currentInvocationSequence;
		double traceDuration = invocationSequenceRoot.getDuration();

		// Dig deeper in the tree as long as there is a dominating call.
		do {
			currentInvocationSequence = childWithMaxDuration;
			childWithMaxDuration = getChildWithMaxDuration(currentInvocationSequence);
		} while ((null != childWithMaxDuration) && isDominatingCall(childWithMaxDuration.getDuration(), traceDuration));

		return currentInvocationSequence;
	}

	/**
	 * Checks whether the child with the highest duration of the current investigated invocation
	 * sequence is a dominating call. If not, the invocation sequence is the
	 * <code>Global Context</code>. This method is used to decide whether to dig deeper in the
	 * invocation tree.
	 *
	 * @param durationMaxDurationChild
	 *            Duration of the child with highest duration.
	 * @param traceDuration
	 *            The duration of the analyzed {@link InvocationSequenceData}.
	 * @return Whether child with highest duration is a dominating call.
	 */
	private boolean isDominatingCall(double durationMaxDurationChild, double traceDuration) {
		return ((traceDuration - durationMaxDurationChild) < baseline) && (durationMaxDurationChild > (traceDuration * PROPORTION));
	}

	/**
	 * Returns child of passed {@link InvocationSequenceData} with the highest duration. Returns
	 * <code>null</code> if the passed {@link InvocationSequenceData} has no children.
	 *
	 * @param currentInvocationSequence
	 *            The current investigated invocation sequence in the invocation tree.
	 * @return Child with highest duration.
	 */
	private InvocationSequenceData getChildWithMaxDuration(InvocationSequenceData currentInvocationSequence) {
		boolean first = true;
		InvocationSequenceData childWithMaxDuration = null;
		for (InvocationSequenceData child : currentInvocationSequence.getNestedSequences()) {
			if (first) {
				childWithMaxDuration = child;
				first = false;
			} else if (child.getDuration() > childWithMaxDuration.getDuration()) {
				childWithMaxDuration = child;
			}
		}
		return childWithMaxDuration;
	}
}

