package rocks.inspectit.server.diagnosis.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import rocks.inspectit.server.diagnosis.engine.session.ISessionResultCollector;
import rocks.inspectit.server.diagnosis.engine.session.SessionContext;
import rocks.inspectit.server.diagnosis.engine.tag.Tag;
import rocks.inspectit.server.diagnosis.engine.tag.TagState;
import rocks.inspectit.server.diagnosis.service.aggregation.AggregatedDiagnosisData;
import rocks.inspectit.server.diagnosis.service.data.CauseCluster;
import rocks.inspectit.server.diagnosis.service.rules.RuleConstants;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.communication.data.diagnosis.AggregatedDiagnosisTimerData;
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
			Optional<InvocationSequenceData> globalContext = getGlobalContext(leafTag);
			Optional<CauseCluster> problemContext = getProblemContext(leafTag);
			Optional<AggregatedDiagnosisData> rootCauseInvocations = getRootCauseInvocations(leafTag);
			Optional<CauseStructure> causeStructure = getCauseStructure(leafTag);

			// if no globalContext is found, then the invocationSequence has no TimerData. Just
			// continue and do nothing.
			if (globalContext.isPresent()) {
				// if problemContext,rootCauseInvocations, and causeStructure is found then create
				// ProblemOccurrence.
				if ((problemContext.isPresent()) && (rootCauseInvocations.isPresent()) && (causeStructure.isPresent())) {
					RootCause rootCause = new RootCause(rootCauseInvocations.get().getMethodIdent(), rootCauseInvocations.get().getAggregatedDiagnosisTimerData());
					ProblemOccurrence problem = new ProblemOccurrence(inputInvocationSequence, globalContext.get(), problemContext.get().getCommonContext(), rootCause,
							causeStructure.get().getCauseType(), causeStructure.get().getSourceType());
					problems.add(problem);

					// if no problemContext,rootCauseInvocations, or causeStructure is found then no
					// TimeWastingOperation was found for the GlobalContext. Then take only the
					// GlobalContext as basis for the ProblemOccurrence.
				} else {
					RootCause rootCause = new RootCause(globalContext.get().getMethodIdent(),
							new AggregatedDiagnosisTimerData(InvocationSequenceDataHelper.getTimerDataOrSQLData(globalContext.get())));
					ProblemOccurrence problem = new ProblemOccurrence(inputInvocationSequence, globalContext.get(), rootCause);
					problems.add(problem);
				}
			}

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
	private Optional<InvocationSequenceData> getGlobalContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_GLOBAL_CONTEXT)) {

				if (leafTag.getValue() instanceof InvocationSequenceData) {
					return Optional.of((InvocationSequenceData) leafTag.getValue());
				} else {
					throw new RuntimeException("Global context has wrong datatype!");
				}

			}
			leafTag = leafTag.getParent();
		}

		return Optional.empty();
	}

	/**
	 * Returns the InvocationSequenceData of ProblemContext Tag.
	 *
	 * @param leafTag
	 *            leafTag for which the InvocationSequenceData should be returned
	 * @return InvocationSequenceData of ProblemContext
	 */
	private Optional<CauseCluster> getProblemContext(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CONTEXT)) {

				if (leafTag.getValue() instanceof CauseCluster) {
					return Optional.of((CauseCluster) leafTag.getValue());
				} else {
					throw new RuntimeException("Problem context has wrong datatype!");
				}

			}
			leafTag = leafTag.getParent();
		}

		return Optional.empty();
	}

	/**
	 * Returns the AggregatedInvocationSequenceData of RootCauseInvocations Tag.
	 *
	 * @param leafTag
	 *            leafTag for which the AggregatedInvocationSequenceData should be returned
	 * @return AggregatedInvocationSequenceData of RootCauseInvocations
	 */
	private Optional<AggregatedDiagnosisData> getRootCauseInvocations(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_PROBLEM_CAUSE)) {
				if (leafTag.getValue() instanceof AggregatedDiagnosisData) {
					return Optional.of((AggregatedDiagnosisData) leafTag.getValue());
				} else {
					throw new RuntimeException("Problem cause has wrong datatype!");
				}
			}

			leafTag = leafTag.getParent();
		}

		return Optional.empty();
	}

	/**
	 * Returns the CauseStructure of the CauseStructure Tag.
	 *
	 * @param leafTag
	 *            leafTag for which the CauseStructure should be returned
	 * @return CauseStructure of leafTag
	 */
	private Optional<CauseStructure> getCauseStructure(Tag leafTag) {
		while (null != leafTag) {
			if (leafTag.getType().equals(RuleConstants.DIAGNOSIS_TAG_CAUSE_STRUCTURE)) {
				if (leafTag.getValue() instanceof CauseStructure) {
					return Optional.of((CauseStructure) leafTag.getValue());
				} else {
					throw new RuntimeException("Cause structure has wrong datatype!");
				}

			}
			leafTag = leafTag.getParent();
		}

		return Optional.empty();
	}
}