/**
 *
 */
package rocks.inspectit.server.diagnosis.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.diagnosis.engine.session.ISessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.SessionContext;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.diagnosis.results.CauseStructure;
import rocks.inspectit.shared.all.communication.data.diagnosis.results.ProblemOccurrence;

/**
 * This class collects the results of the diagnosis engine and converts the results in a List of
 * {@link #ProblemOccurrence}. This collector expects the results of a certain structure of the
 * rules following the logic GlobalContext, ProblemContext, RootCause, and CauseStructure. In case
 * other Rules are used this collector must be adapted.
 *
 * @author Alexander Wert
 *
 */
@Component
public class ProblemOccurenceResultCollector implements ISessionResultCollector<InvocationSequenceData, List<ProblemOccurrence>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ProblemOccurrence> collect(SessionContext<InvocationSequenceData> sessionContext) {
		List<ProblemOccurrence> problems = new ArrayList<>();
		InvocationSequenceData inputInvocationSequence = sessionContext.getInput();
		Collection<Tag> leafTags = sessionContext.getStorage().mapTags(TagState.LEAF).values();
		for (Tag leafTag : leafTags) {
			InvocationSequenceData globalContext = getGlobalContext(leafTag);
			InvocationSequenceData problemContext = getProblemContext(leafTag);
			AggregatedInvocationSequenceData rootCauseInvocations = getRootCauseInvocations(leafTag);
			CauseStructure causeStructure = getCauseStructure(leafTag);

			// create new ProblemOccurrence
			ProblemOccurrence problem = new ProblemOccurrence(inputInvocationSequence, globalContext, problemContext, rootCauseInvocations, causeStructure);
			problem.setPlatformIdent(inputInvocationSequence.getPlatformIdent());
			problem.setTimeStamp(inputInvocationSequence.getTimeStamp());
			problem.setSensorTypeIdent(inputInvocationSequence.getSensorTypeIdent());
			problems.add(problem);
		}

		return problems;
	}

	/**
	 * Returns the InvocationSequenceData of GlobalContext Tag.
	 *
	 * @param leafTag
	 *            leafTag for which the InvocationSequenceData should be returned
	 * @return InvocationSequenceData of GlobalContext
	 */
	private InvocationSequenceData getGlobalContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_GLOBAL_CONTEXT)) {
				return (InvocationSequenceData) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Global context could not be found!");
	}

	/**
	 * Returns the InvocationSequenceData of ProblemContext Tag.
	 *
	 * @param leafTag
	 *            leafTag for which the InvocationSequenceData should be returned
	 * @return InvocationSequenceData of ProblemContext
	 */
	private InvocationSequenceData getProblemContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_PROBLEM_CONTEXT)) {
				return (InvocationSequenceData) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Problem context could not be found!");
	}

	/**
	 * Returns the AggregatedInvocationSequenceData of RootCauseInvocations Tag.
	 *
	 * @param leafTag
	 *            leafTag for which the AggregatedInvocationSequenceData should be returned
	 * @return AggregatedInvocationSequenceData of RootCauseInvocations
	 */
	private AggregatedInvocationSequenceData getRootCauseInvocations(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_PROBLEM_CAUSE)) {
				return (AggregatedInvocationSequenceData) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Problem root cause could not be found!");
	}

	/**
	 * Returns the CauseStructure of the CauseStructure Tag.
	 *
	 * @param leafTag
	 *            leafTag for which the CauseStructure should be returned
	 * @return CauseStructure of leafTag
	 */
	private CauseStructure getCauseStructure(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.TAG_CAUSE_STRUCTURE)) {
				return (CauseStructure) leafTag.getValue();
			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Cause structure could not be found!");
	}
}