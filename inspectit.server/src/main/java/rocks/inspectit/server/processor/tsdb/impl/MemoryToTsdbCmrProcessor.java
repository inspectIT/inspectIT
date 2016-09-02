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
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * This processor writes memory usage information to a timeseries database.
 *
 * @author Alexander Wert
 *
 */
public class MemoryToTsdbCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * The name of the measurement.
	 */
	private static final String MEASUREMENT_MEMORY = "memory";

	/**
	 * Agent name tag.
	 */
	private static final String TAG_AGENT_NAME = "agentName";

	/**
	 * Agent id tag.
	 */
	private static final String TAG_AGENT_ID = "agentId";

	/**
	 * Free physical memory field.
	 */
	private static final String FIELD_AVG_FREE_PHYS_MEMORY = "freePhysicalMem";

	/**
	 * Free swap space field.
	 */
	private static final String FIELD_AVG_FREE_SWAP_SPACE = "freeSwapSpace";

	/**
	 * Committed heap size field.
	 */
	private static final String FIELD_AVG_COMMITTED_HEAP_MEMORY = "committedHeapMemorySize";

	/**
	 * Committed non-heap size field.
	 */
	private static final String FIELD_AVG_COMMITTED_NON_HEAP_MEMORY = "committedNonHeapMemorySize";

	/**
	 * Average used heap size field.
	 */
	private static final String FIELD_AVG_USED_HEAP_MEMORY = "avgUsedHeapMemorySize";

	/**
	 * Minimum used heap size field.
	 */
	private static final String FIELD_MIN_USED_HEAP_MEMORY = "minUsedHeapMemorySize";

	/**
	 * Maximum used heap size field.
	 */
	private static final String FIELD_MAX_USED_HEAP_MEMORY = "maxUsedHeapMemorySize";

	/**
	 * Average used non-heap size field.
	 */
	private static final String FIELD_AVG_USED_NON_HEAP_MEMORY = "avgUsedNonHeapMemorySize";

	/**
	 * Minimum used non-heap size field.
	 */
	private static final String FIELD_MIN_USED_NON_HEAP_MEMORY = "minUsedNonHeapMemorySize";

	/**
	 * Maximum used non-heap size field.
	 */
	private static final String FIELD_MAX_USED_NON_HEAP_MEMORY = "maxUsedNonHeapMemorySize";

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
		MemoryInformationData data = (MemoryInformationData) defaultData;

		String agentName;
		try {
			PlatformIdent pIdent = globalDataAccessService.getCompleteAgent(data.getPlatformIdent());
			agentName = pIdent.getAgentName();
		} catch (BusinessException e) {
			agentName = TsdbPersistingCmrProcessor.VALUE_NOT_AVAILABLE;
		}
		int count = data.getCount();
		long freePhysMemory = 0;
		if (data.getTotalFreePhysMemory() > 0) {
			freePhysMemory = data.getTotalFreePhysMemory() / count;
		}
		long freeSwapSpace = 0;
		if (data.getTotalFreeSwapSpace() > 0) {
			freeSwapSpace = data.getTotalFreeSwapSpace() / count;
		}
		long committedHeapMemorySize = 0;
		if (data.getTotalComittedHeapMemorySize() > 0) {
			committedHeapMemorySize = data.getTotalComittedHeapMemorySize() / count;
		}
		long committedNonHeapMemorySize = 0;
		if (data.getTotalComittedNonHeapMemorySize() > 0) {
			committedNonHeapMemorySize = data.getTotalComittedNonHeapMemorySize() / count;
		}
		long usedHeapMemorySize = 0;
		if (data.getTotalUsedHeapMemorySize() > 0) {
			usedHeapMemorySize = data.getTotalUsedHeapMemorySize() / count;
		}
		long usedNonHeapMemorySize = 0;
		if (data.getTotalUsedNonHeapMemorySize() > 0) {
			usedNonHeapMemorySize = data.getTotalUsedNonHeapMemorySize() / count;
		}

		// measurement
		Builder builder = Point.measurement(MEASUREMENT_MEMORY);
		builder.time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS);

		// tags
		builder.tag(TAG_AGENT_ID, String.valueOf(data.getPlatformIdent()));
		builder.tag(TAG_AGENT_NAME, agentName);

		// fields
		builder.addField(FIELD_AVG_FREE_PHYS_MEMORY, freePhysMemory);
		builder.addField(FIELD_AVG_FREE_SWAP_SPACE, freeSwapSpace);
		builder.addField(FIELD_AVG_COMMITTED_HEAP_MEMORY, committedHeapMemorySize);
		builder.addField(FIELD_AVG_COMMITTED_NON_HEAP_MEMORY, committedNonHeapMemorySize);
		builder.addField(FIELD_AVG_USED_HEAP_MEMORY, usedHeapMemorySize);
		builder.addField(FIELD_MIN_USED_HEAP_MEMORY, data.getMinUsedHeapMemorySize());
		builder.addField(FIELD_MAX_USED_HEAP_MEMORY, data.getMaxUsedHeapMemorySize());
		builder.addField(FIELD_AVG_USED_NON_HEAP_MEMORY, usedNonHeapMemorySize);
		builder.addField(FIELD_MIN_USED_NON_HEAP_MEMORY, data.getMinUsedNonHeapMemorySize());
		builder.addField(FIELD_MAX_USED_NON_HEAP_MEMORY, data.getMaxUsedNonHeapMemorySize());

		influxDbService.insert(builder.build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return (defaultData instanceof MemoryInformationData);
	}
}
