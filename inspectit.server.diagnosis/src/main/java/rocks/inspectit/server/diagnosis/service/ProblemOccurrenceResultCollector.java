package rocks.inspectit.server.diagnosis.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.diagnosis.engine.session.ISessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.SessionContext;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;
import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.server.diagnosis.service.data.CauseCluster;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;
import rocks.inspectit.shared.cs.communication.data.diagnosis.RootCause;

/**
 * This class collects the results of the diagnosis engine and converts the results in a List of
 * {@link #ProblemOccurrence}. This collector expects the results of a certain structure of the
 * rules following the logic GlobalContext, ProblemContext, RootCause, and CauseStructure. In case
 * other Rules are used this collector must be adapted.
 *
 * @author Alexander Wert, Christian Voegele
 *
 */
@Component
public class ProblemOccurrenceResultCollector implements ISessionResultCollector<InvocationSequenceData, List<ProblemOccurrence>> {

	/**
	 * {@inheritDoc}
	 *
	 * This method converts the results of the rule engine to a {@link #ProblemOccurrence} object.
	 *
	 */
	@Override
	public List<ProblemOccurrence> collect(SessionContext<InvocationSequenceData> sessionContext) {
		List<ProblemOccurrence> problems = new ArrayList<>();
		InvocationSequenceData inputInvocationSequence = sessionContext.getInput();
		Collection<Tag> leafTags = sessionContext.getStorage().mapTags(TagState.LEAF).values();
		for (Tag leafTag : leafTags) {
			InvocationSequenceData globalContext = getGlobalContext(leafTag);
			CauseCluster problemContext = getProblemContext(leafTag);
			AggregatedDiagnosisData rootCauseInvocations = getRootCauseInvocations(leafTag);
			CauseStructure causeStructure = getCauseStructure(leafTag);
			RootCause rootCause = new RootCause(rootCauseInvocations.getMethodIdent(), rootCauseInvocations.getAggregatedDiagnosisTimerData());

			// create new ProblemOccurrence
			ProblemOccurrence problem = new ProblemOccurrence(inputInvocationSequence, globalContext,
					problemContext.getCommonContext(), rootCause, causeStructure.getCauseType(), causeStructure.getSourceType());
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
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT)) {

				if (leafTag.getValue() instanceof InvocationSequenceData) {
					return (InvocationSequenceData) leafTag.getValue();
				} else {
					throw new RuntimeException("Global context has wrong datatype!");
				}

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
	private CauseCluster getProblemContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT)) {

				if (leafTag.getValue() instanceof CauseCluster) {
					return (CauseCluster) leafTag.getValue();
				} else {
					throw new RuntimeException("Problem context has wrong datatype!");
				}

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
	private AggregatedDiagnosisData getRootCauseInvocations(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE)) {
				if (leafTag.getValue() instanceof AggregatedDiagnosisData) {
					return (AggregatedDiagnosisData) leafTag.getValue();
				} else {
					throw new RuntimeException("Problem cause has wrong datatype!");
				}
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
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE)) {
				if (leafTag.getValue() instanceof CauseStructure) {
					return (CauseStructure) leafTag.getValue();
				} else {
					throw new RuntimeException("Cause structure has wrong datatype!");
				}

			}
			leafTag = leafTag.getParent();
		}

		throw new RuntimeException("Cause structure could not be found!");
	}
}