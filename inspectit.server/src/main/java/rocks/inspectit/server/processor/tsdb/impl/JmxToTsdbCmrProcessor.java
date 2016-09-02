package rocks.inspectit.server.processor.tsdb.impl;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.IInfluxDBService;
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * This processor writes JMX attribute values to a timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class JmxToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_JMX = "jmx";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * JMX attribute name tag.
	 */
	private static final String TAG_JMX_ATTRIBUTE_FULL_NAME = "name";

	/**
	 * Average value field.
	 */
	private static final String FIELD_AVG_VALUE = "avgValue";

	/**
	 * Minimum value field.
	 */
	private static final String FIELD_MIN_VALUE = "minValue";

	/**
	 * Maximum value field.
	 */
	private static final String FIELD_MAX_VALUE = "maxValue";

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
	 * {@link ICachedDataService} used to retrieve JMX information.
	 */
	@Autowired
	ICachedDataService cachedDataService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		JmxSensorValueData data = (JmxSensorValueData) defaultData;
		JmxDefinitionDataIdent jmxIdent = cachedDataService.getJmxDefinitionDataIdentForId(data.getJmxSensorDefinitionDataIdentId());

		String agentName;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(data.getPlatformIdent());
			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}

		// measurement
		Builder builder = Point.measurement(MEASUREMENT_JMX);
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);
		builder.tag(TAG_JMX_ATTRIBUTE_FULL_NAME, jmxIdent.getDerivedFullName());

		// fields
		builder.addField(FIELD_AVG_VALUE, data.getAverageValue());
		builder.addField(FIELD_MIN_VALUE, data.getMinValue());
		builder.addField(FIELD_MAX_VALUE, data.getMaxValue());

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof JmxSensorValueData) && ((JmxSensorValueData) defaultData).isBooleanOrNumeric();
	}

}
