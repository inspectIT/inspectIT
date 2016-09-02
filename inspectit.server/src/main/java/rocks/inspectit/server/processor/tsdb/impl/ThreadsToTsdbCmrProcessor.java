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
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * This processor writes threads information to a timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class ThreadsToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_THREADS = "threads";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * Live thread count field.
	 */
	private static final String FIELD_LIVE_THREAD_COUNT = "liveThreadCount";

	/**
	 * Deamon thread count field.
	 */
	private static final String FIELD_DEAMON_THREAD_COUNT = "daemonThreadCount";

	/**
	 * Total started thread count.
	 */
	private static final String FIELD_TOTAL_STARTED_THREAD_COUNT = "totalStartedThreadCount";

	/**
	 * Peak thread count.
	 */
	private static final String FIELD_PEAK_THREAD_COUNT = "peakThreadCount";

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
		ThreadInformationData data = (ThreadInformationData) defaultData;

		String agentName;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(data.getPlatformIdent());
			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}

		int count = data.getCount();
		long liveThreadCount = data.getTotalThreadCount() / count;
		long daemonThreadCount = data.getTotalDaemonThreadCount() / count;
		long totalStartedThreadCount = data.getTotalTotalStartedThreadCount() / count;
		long peakThreadCount = data.getTotalPeakThreadCount() / count;

		// measurement
		Builder builder = Point.measurement(MEASUREMENT_THREADS);
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);

		// fields
		builder.addField(FIELD_LIVE_THREAD_COUNT, liveThreadCount);
		builder.addField(FIELD_DEAMON_THREAD_COUNT, daemonThreadCount);
		builder.addField(FIELD_TOTAL_STARTED_THREAD_COUNT, totalStartedThreadCount);
		builder.addField(FIELD_PEAK_THREAD_COUNT, peakThreadCount);

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof ThreadInformationData);
	}

}
