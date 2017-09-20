package rocks.inspectit.server.processor.impl;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;

import rocks.inspectit.server.diagnosis.service.IDiagnosisService;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * This processor starts the {@link #diagnosisService} and stores the results in
 * {@link #diagnosisResults}.
 *
 * @author Claudio Waldvogel, Christian Voegele
 *
 */
public class DiagnosisCmrProcessor extends AbstractCmrDataProcessor {
	/**
	 * Diagnosis service interface.
	 */
	IDiagnosisService diagnosisService;

	/**
	 * The value of the baseline defined in the configuration.
	 */
	@Value("${diagnosis.baseline}")
	private double baseline;

	/**
	 * Diagnosis service is enabled.
	 */
	@Value("${diagnosis.enabled}")
	private boolean diagnosisEnabled;

	/**
	 * Write data in influx is active, so the diagnosis service will be performed.
	 */
	@Value("${influxdb.active}")
	private boolean influxActive;

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
		return diagnosisEnabled && influxActive && (defaultData instanceof InvocationSequenceData) && (((InvocationSequenceData) defaultData).getDuration() > baseline);
	}

	/**
	 * @param diagnosisService
	 *            Sets the diagnosis service.
	 */
	void setDiagnosisService(IDiagnosisService diagnosisService) {
		this.diagnosisService = diagnosisService;
	}

	/**
	 * Gets {@link #baseline}.
	 *
	 * @return {@link #baseline}
	 */
	public double getBaseline() {
		return this.baseline;
	}

	/**
	 * Sets {@link #baseline}.
	 *
	 * @param baseline
	 *            New value for {@link #baseline}
	 */
	public void setBaseline(double baseline) {
		this.baseline = baseline;
	}

	/**
	 * Gets {@link #diagnosisEnabled}.
	 *
	 * @return {@link #diagnosisEnabled}
	 */
	public boolean isDiagnosisEnabled() {
		return this.diagnosisEnabled;
	}

	/**
	 * Sets {@link #diagnosisEnabled}.
	 *
	 * @param diagnosisEnabled
	 *            New value for {@link #diagnosisEnabled}
	 */
	public void setDiagnosisEnabled(boolean diagnosisEnabled) {
		this.diagnosisEnabled = diagnosisEnabled;
	}

	/**
	 * Gets {@link #influxActive}.
	 *
	 * @return {@link #influxActive}
	 */
	public boolean isInfluxActive() {
		return this.influxActive;
	}

	/**
	 * Sets {@link #influxActive}.
	 *
	 * @param influxActive
	 *            New value for {@link #influxActive}
	 */
	public void setInfluxActive(boolean influxActive) {
		this.influxActive = influxActive;
	}
}