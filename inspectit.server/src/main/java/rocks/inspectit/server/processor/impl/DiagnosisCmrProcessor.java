package rocks.inspectit.server.processor.impl;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.diagnosis.results.IDiagnosisResults;
import rocks.inspectit.server.diagnosis.service.IDiagnosisResultNotificationService;
import rocks.inspectit.server.diagnosis.service.IDiagnosisService;
import rocks.inspectit.server.influx.builder.ProblemOccurrencePointBuilder;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;

/**
 * This processor starts the {@link #diagnosisService} and stores the results in
 * {@link #diagnosisResults}.
 *
 * @author Claudio Waldvogel, Christian Voegele
 *
 */
public class DiagnosisCmrProcessor extends AbstractCmrDataProcessor implements IDiagnosisResultNotificationService {

	/**
	 * Diagnosis service interface.
	 */
	@Autowired(required = false)
	private IDiagnosisService diagnosisService;

	/**
	 * Diagnosis service interface.
	 */
	@Autowired(required = false)
	private IDiagnosisResults<ProblemOccurrence> diagnosisResults;

	/**
	 * Builder needed to store the resulting ProblemOccurrence into influx.
	 */
	@Autowired
	ProblemOccurrencePointBuilder problemOccurrencePointBuilder;

	/**
	 * Baseline value.
	 */
	private final double baseline;

	/**
	 * Basic constructor.
	 *
	 * @param baseline
	 *            The default baseline value defined in the configuration.
	 */
	public DiagnosisCmrProcessor(final double baseline) {
		this.baseline = baseline;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		diagnosisService.diagnose((InvocationSequenceData) defaultData, baseline);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof InvocationSequenceData) && (((InvocationSequenceData) defaultData).getDuration() > baseline);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewDiagnosisResult(ProblemOccurrence problemOccurrence) {
		diagnosisResults.getDiagnosisResults().add(problemOccurrence);
		problemOccurrencePointBuilder.saveProblemOccurrenceToInflux(problemOccurrence);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNewDiagnosisResult(Collection<ProblemOccurrence> problemOccurrences) {
		diagnosisResults.getDiagnosisResults().addAll(problemOccurrences);
		for (ProblemOccurrence problemOccurrence : problemOccurrences) {
			problemOccurrencePointBuilder.saveProblemOccurrenceToInflux(problemOccurrence);
		}
	}
}