package rocks.inspectit.server.processor.tsdb.impl;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.IInfluxDBService;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * This processor writes cpu utilization values to a timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class CpuToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_CPU = "cpu";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * Average CPU utilization field.
	 */
	private static final String FIELD_AVG_CPU_UTIL = "avgUtilization";

	/**
	 * Minimum CPU utilization field.
	 */
	private static final String FIELD_MIN_CPU_UTIL = "minUtilization";

	/**
	 * Maximum CPU utilization field.
	 */
	private static final String FIELD_MAX_CPU_UTIL = "maxUtilization";

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
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		CpuInformationData data = (CpuInformationData) defaultData;

		String agentName;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(data.getPlatformIdent());
			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}
		float cpuAverage = data.getTotalCpuUsage() / data.getCount();

		// measurement
		Builder builder = Point.measurement(MEASUREMENT_CPU);
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);

		// fields
		builder.addField(FIELD_AVG_CPU_UTIL, cpuAverage);
		builder.addField(FIELD_MIN_CPU_UTIL, data.getMinCpuUsage());
		builder.addField(FIELD_MAX_CPU_UTIL, data.getMaxCpuUsage());

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof CpuInformationData);
	}

}
