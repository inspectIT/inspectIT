package rocks.inspectit.server.processor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rocks.inspectit.server.diagnosis.service.DiagnosisService;
import rocks.inspectit.server.diagnosis.service.IDiagnosisService;
import rocks.inspectit.server.influx.builder.ProblemOccurrencePointBuilder;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
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
@Configuration
public class DiagnosisCmrProcessor extends AbstractCmrDataProcessor implements Consumer<ProblemOccurrence> {

	/**
	 * Diagnosis service interface.
	 */
	private IDiagnosisService diagnosisService;

	/**
	 * Builder needed to store the resulting ProblemOccurrence into influx.
	 */
	@Autowired
	ProblemOccurrencePointBuilder problemOccurrencePointBuilder;

	/**
	 * {@link InfluxDBDao} to write to.
	 */
	@Autowired
	InfluxDBDao influxDBDao;

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
	 * Gets the diagnosis service initializing it with the configurations established.
	 *
	 * @param processor
	 *            Diagnosis CMR processor.
	 * @return Returns the diagnosis service.
	 */
	@Bean
	@Autowired
	public IDiagnosisService getDiagnosisService(DiagnosisCmrProcessor processor) {
		List<String> rulesPackages = new ArrayList<>();
		rulesPackages.add("rocks.inspectit.server.diagnosis.service.rules.impl");
		IDiagnosisService diagnosisService = new DiagnosisService(this, rulesPackages, 2, 10L, 2);
		processor.setDiagnosisService(diagnosisService);
		return diagnosisService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		if (diagnosisEnabled && influxActive) {
			diagnosisService.diagnose((InvocationSequenceData) defaultData, baseline);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return diagnosisEnabled && influxActive && (defaultData instanceof InvocationSequenceData) && (((InvocationSequenceData) defaultData).getDuration() > baseline);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(ProblemOccurrence problemOccurrence) {
		Builder builder = problemOccurrencePointBuilder.getBuilder(problemOccurrence);
		influxDBDao.insert(builder.build());
	}

	/**
	 * @param diagnosisService
	 *            Sets the diagnosis service.
	 */
	void setDiagnosisService(IDiagnosisService diagnosisService) {
		this.diagnosisService = diagnosisService;
	}
}