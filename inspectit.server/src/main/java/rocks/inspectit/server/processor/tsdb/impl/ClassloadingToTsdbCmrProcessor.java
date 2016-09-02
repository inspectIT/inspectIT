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
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * This processor writes class loading information to a timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class ClassloadingToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_CLASS_LOADING = "classLoading";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * Loaded classes count field.
	 */
	private static final String FIELD_LOADED_CLASSES = "loadedClassCount";

	/**
	 * Total loaded classes count field.
	 */
	private static final String FIELD_TOTAL_LOADED_CLASSES = "totalLoadedClassCount";

	/**
	 * Unloaded classes count field.
	 */
	private static final String FIELD_UNLOADED_CLASSES = "unloadedClassCount";

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
		ClassLoadingInformationData data = (ClassLoadingInformationData) defaultData;
		String agentName;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(data.getPlatformIdent());
			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}

		long count = data.getCount();
		long loadedClassCount = data.getTotalLoadedClassCount() / count;
		long totalLoadedClassCount = data.getTotalTotalLoadedClassCount() / count;
		long unloadedClassCount = data.getTotalUnloadedClassCount() / count;

		// measurement
		Builder builder = Point.measurement(MEASUREMENT_CLASS_LOADING);
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);

		// fields
		builder.addField(FIELD_LOADED_CLASSES, loadedClassCount);
		builder.addField(FIELD_TOTAL_LOADED_CLASSES, totalLoadedClassCount);
		builder.addField(FIELD_UNLOADED_CLASSES, unloadedClassCount);

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof ClassLoadingInformationData);
	}

}
