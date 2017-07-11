package rocks.inspectit.server.influx.builder;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
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
	 * Unknown method fully qualified name.
	 */
	private static final String UNKNOWN_METHOD_FQN = "Unknown method";

	/**
	 * {@link ICachedDataService} for resolving all needed names.
	 */
	@Autowired
	protected ICachedDataService cachedDataService;

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
		String businessTxName = BusinessTransactionDefinition.UNKNOWN_BUSINESS_TX;
		BusinessTransactionData businessTx = cachedDataService.getBusinessTransactionForId(data.getApplicationNameIdent(), data.getBusinessTransactionNameIdent());
		if (null != businessTx) {
			businessTxName = businessTx.getName();
		}

		String applicationName = ApplicationDefinition.UNKNOWN_APP;
		ApplicationData applicationData = cachedDataService.getApplicationForId(data.getApplicationNameIdent());
		if (applicationData != null) {
			applicationName = applicationData.getName();
		}

		String problemContextName = UNKNOWN_METHOD_FQN;
		MethodIdent problemContextMethodIdent = cachedDataService.getMethodIdentForId(data.getProblemContext().getMethodIdent());
		if (problemContextMethodIdent != null) {
			problemContextName = problemContextMethodIdent.getFullyQualifiedMethodSignature();
		}

		String rootCauseName = UNKNOWN_METHOD_FQN;
		MethodIdent rootCauseMethodIdent = cachedDataService.getMethodIdentForId(data.getRootCause().getMethodIdent());
		if (rootCauseMethodIdent != null) {
			rootCauseName = rootCauseMethodIdent.getFullyQualifiedMethodSignature();
		}

		builder.tag(Series.ProblemOccurrenceInformation.TAG_APPLICATION_NAME, applicationName);
		builder.tag(Series.ProblemOccurrenceInformation.TAG_BUSINESS_TRANSACTION_NAME, businessTxName);
		builder.tag(Series.ProblemOccurrenceInformation.TAG_PROBLEM_CONTEXT_METHOD_NAME, problemContextName);
		builder.tag(Series.ProblemOccurrenceInformation.TAG_ROOTCAUSE_METHOD_NAME, rootCauseName);
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
		builder.addField(Series.ProblemOccurrenceInformation.FIELD_ROOTCAUSE_METHOD_EXCLUSIVE_TIME, data.getRootCause().getAggregatedDiagnosisTimerData().getExclusiveDuration());
	}

	/**
	 * Returns the builder for the concrete problemOccurrence.
	 *
	 * @param problemOccurrence
	 *            Instance of problemOccurrenct to build the builder with the proper tags and
	 *            fields.
	 * @return Returns the builder.
	 */
	public Builder getBuilder(ProblemOccurrence problemOccurrence) {
		Builder builder = Point.measurement(getSeriesName());
		builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		addTags(problemOccurrence, builder);
		addFields(problemOccurrence, builder);
		return builder;
	}
}
