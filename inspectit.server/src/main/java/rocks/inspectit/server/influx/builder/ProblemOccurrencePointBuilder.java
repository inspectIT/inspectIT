package rocks.inspectit.server.influx.builder;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.server.influx.dao.InfluxDBDao;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.cs.communication.data.diagnosis.ProblemOccurrence;

/**
 * Point builder for the {@link ProblemOccurrence}.
 *
 * @author Christian Voegele
 *
 */
@Component
public class ProblemOccurrencePointBuilder {

	/**
	 * {@link ICachedDataService} for resolving all needed names.
	 */
	@Autowired
	protected ICachedDataService cachedDataService;

	/**
	 * {@link InfluxDBDao} to write to.
	 */
	@Autowired
	private InfluxDBDao influxDbDao;

	/**
	 * Returns series name for this builder.
	 *
	 * @return Returns series name for this builder.
	 */
	protected String getSeriesName() {
		return Series.ProblemOccurrenceInformation.NAME;
	}

	/**
	 * Add tags to builder.
	 *
	 * @param data
	 *            ProblemOccurrence instance
	 * @param builder
	 *            Builder that can be used to create influx points.
	 */
	protected void addTags(ProblemOccurrence data, Builder builder) {
		builder.tag(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, cachedDataService.getApplicationForId(data.getApplicationNameIdent()).getName());
		builder.tag(Series.ProblemOccurrenceInformation.TAG_BUSINESS_CONTEXT,
				cachedDataService.getBusinessTransactionForId(data.getApplicationNameIdent(), data.getBusinessTransactionNameIdent()).getName());
		builder.tag(Series.ProblemOccurrenceInformation.TAG_GLOBAL_CONTEXT_METHOD_NAME, cachedDataService.getMethodIdentForId(data.getGlobalContext().getMethodIdent()).getMethodName());
		builder.tag(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, cachedDataService.getMethodIdentForId(data.getProblemContext().getMethodIdent()).getMethodName());
		builder.tag(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, cachedDataService.getMethodIdentForId(data.getRootCause().getMethodIdent()).getMethodName());
		builder.tag(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_CAUSE_TYPE, data.getCauseType().toString());
		builder.tag(Series.ProblemOccurrenceInformation.TAG_CAUSESTRUCTURE_SOURCE_TYPE, data.getSourceType().toString());
	}

	/**
	 * Add fields to builder.
	 *
	 * @param data
	 *            ProblemOccurrence instance
	 * @param builder
	 *            Builder that can be used to create influx points.
	 */
	protected void addFields(ProblemOccurrence data, Builder builder) {
		builder.addField(Series.ProblemOccurrenceInformation.FIELD_INVOCATION_ROOT_DURATION, data.getRequestRoot().getDiagnosisTimerData().getDuration());
		builder.addField(Series.ProblemOccurrenceInformation.FIELD_GLOBAL_CONTEXT_METHOD_EXCLUSIVE_TIME, data.getGlobalContext().getDiagnosisTimerData().getExclusiveDuration());
		builder.addField(Series.ProblemOccurrenceInformation.FIELD_PROBLEM_CONTEXT_METHOD_EXCLUSIVE_TIME, data.getProblemContext().getDiagnosisTimerData().getExclusiveDuration());
		builder.addField(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, data.getRootCause().getAggregatedDiagnosisTimerData().getExclusiveDuration());
		builder.addField(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_COUNT, data.getRootCause().getAggregatedDiagnosisTimerData().getExclusiveCount());
	}

	/**
	 * @param data
	 *            ProblemOccurrence instance
	 */
	public void saveProblemOccurrenceToInflux(ProblemOccurrence data) {
		Builder builder = Point.measurement(getSeriesName());
		builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

		this.addTags(data, builder);
		this.addFields(data, builder);

		influxDbDao.insert(builder.build());
	}
}
