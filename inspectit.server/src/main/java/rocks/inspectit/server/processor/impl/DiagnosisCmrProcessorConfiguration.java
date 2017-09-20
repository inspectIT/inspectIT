package rocks.inspectit.server.processor.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import rocks.inspectit.server.diagnosis.service.DiagnosisService;
import rocks.inspectit.server.diagnosis.service.IDiagnosisService;
import rocks.inspectit.server.influx.builder.ProblemOccurrencePointBuilder;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;

/**
 * @author Isabel Vico Peinado
 *
 */
@Configuration
public class DiagnosisCmrProcessorConfiguration implements Consumer<ProblemOccurrence> {

	/**
	 * Name of the package which have the implementation of the rules.
	 */
	private static final String RULES_PACKAGE = "rocks.inspectit.server.diagnosis.service.rules.impl";

	/**
	 * {@link InfluxDBDao} to write to.
	 */
	@Autowired
	InfluxDBDao influxDBDao;

	/**
	 * Builder needed to store the resulting ProblemOccurrence into influx.
	 */
	@Autowired
	ProblemOccurrencePointBuilder problemOccurrencePointBuilder;

	/**
	 * Gets the diagnosis service initializing it with the configurations established.
	 *
	 * @param processor
	 *            Diagnosis CMR processor.
	 * @return Returns the diagnosis service.
	 */
	@Bean
	public IDiagnosisService getDiagnosisService(DiagnosisCmrProcessor processor) {
		List<String> rulesPackages = new ArrayList<>();
		rulesPackages.add(RULES_PACKAGE);
		IDiagnosisService diagnosisService = new DiagnosisService(this, rulesPackages, 2, 10L, 2);
		processor.setDiagnosisService(diagnosisService);
		return diagnosisService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void accept(ProblemOccurrence problemOccurrence) {
		Builder builder = problemOccurrencePointBuilder.getBuilder(problemOccurrence);
		influxDBDao.insert(builder.build());
	}
}
