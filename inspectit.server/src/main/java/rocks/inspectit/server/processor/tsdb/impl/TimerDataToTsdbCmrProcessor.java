package rocks.inspectit.server.processor.tsdb.impl;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.IInfluxDBService;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * This processor writes timer data that is marked as chartable to a timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class TimerDataToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_METHODS = "methods";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * Simple method name tag.
	 */
	private static final String TAG_METHOD_NAME = "methodName";

	/**
	 * Class FQN tag.
	 */
	private static final String TAG_CLASS_FQN = "classFqn";

	/**
	 * URI tag.
	 */
	private static final String TAG_URI = "uri";

	/**
	 * inspectIT tagging header tag.
	 */
	private static final String TAG_INSPECTIT_TAGGING_HEADER = "inspectitTaggingHeader";

	/**
	 * Fully qualified method signature tag.
	 */
	private static final String TAG_METHOD_SIGNATURE = "fqnMethodSignature";

	/**
	 * Minimum duration field.
	 */
	private static final String FIELD_MIN_DURATION = "minDuration";

	/**
	 * Average duration field.
	 */
	private static final String FIELD_DURATION = "avgDuration";

	/**
	 * Maximum duration field.
	 */
	private static final String FIELD_MAX_DURATION = "maxDuration";

	/**
	 * Minimum CPU time field.
	 */
	private static final String FIELD_MIN_CPU_TIME = "minCpuTime";

	/**
	 * Average CPU time field.
	 */
	private static final String FIELD_CPU_TIME = "avgCpuTime";

	/**
	 * Maximum CPU time field.
	 */
	private static final String FIELD_MAX_CPU_TIME = "maxCpuTime";

	/**
	 * Minimum exclusive time field.
	 */
	private static final String FIELD_MIN_EXCLUSIVE_TIME = "minExclusiveTime";

	/**
	 * Average exclusive time field.
	 */
	private static final String FIELD_EXCLUSIVE_TIME = "avgExclusiveTime";

	/**
	 * Maximum exclusive time field.
	 */
	private static final String FIELD_MAX_EXCLUSIVE_TIME = "maxExclusiveTime";

	/**
	 * {@link IInfluxDBService} used to write data to an influx database.
	 */
	@Autowired
	IInfluxDBService influxDbService;

	/**
	 * {@link IGlobalDataAccessService} used to retrieve the agent information.
	 */
	@Autowired
	IGlobalDataAccessService globalDataAccessService;

	/**
	 * {@link ICachedDataService} used to retrieve method ident information.
	 */
	@Autowired
	ICachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		TimerData data = (TimerData) defaultData;
		MethodIdent mIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

		String uri = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		String taggingHeader = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		if (data instanceof HttpTimerData) {
			HttpTimerData httpTimerData = (HttpTimerData) data;
			HttpInfo httpInfo = httpTimerData.getHttpInfo();
			uri = httpInfo.getUri();
			if (httpInfo.hasInspectItTaggingHeader()) {
				taggingHeader = httpInfo.getInspectItTaggingHeaderValue();
			}
		}

		String agentName;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(data.getPlatformIdent());

			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}

		// measurement
		Builder builder = Point.measurement(MEASUREMENT_METHODS);
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);
		builder.tag(TAG_METHOD_NAME, mIdent.getMethodName());
		builder.tag(TAG_CLASS_FQN, mIdent.getFQN());
		builder.tag(TAG_URI, uri);
		builder.tag(TAG_METHOD_SIGNATURE, mIdent.getFullyQualifiedMethodSignature());
		builder.tag(TAG_INSPECTIT_TAGGING_HEADER, taggingHeader);

		// fields
		builder.addField(FIELD_MIN_DURATION, data.getMin());
		builder.addField(FIELD_DURATION, data.getAverage());
		builder.addField(FIELD_MAX_DURATION, data.getMax());
		builder.addField(FIELD_MIN_CPU_TIME, data.getCpuMin());
		builder.addField(FIELD_CPU_TIME, data.getCpuAverage());
		builder.addField(FIELD_MAX_CPU_TIME, data.getCpuMax());
		builder.addField(FIELD_MIN_EXCLUSIVE_TIME, data.getExclusiveMin());
		builder.addField(FIELD_EXCLUSIVE_TIME, data.getExclusiveAverage());
		builder.addField(FIELD_MAX_EXCLUSIVE_TIME, data.getExclusiveMax());

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return influxDbService.isOnline() && (defaultData instanceof TimerData) && ((TimerData) defaultData).isCharting();
	}

}
