package rocks.inspectit.server.influx.builder;

import org.influxdb.dto.Point.Builder;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.influx.constants.Series;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;

/**
 * Point builder for the {@link MemoryInformationData}.
 *
 * @author Ivan Senic
 * @author Alexander Wert
 *
 */
@Component
public class MemoryInformationPointBuilder extends DefaultDataPointBuilder<MemoryInformationData> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<MemoryInformationData> getDataClass() {
		return MemoryInformationData.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getSeriesName() {
		return Series.MemoryInformation.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addFields(MemoryInformationData data, Builder builder) {
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

		// fields
		builder.addField(Series.MemoryInformation.FIELD_AVG_FREE_PHYS_MEMORY, freePhysMemory);
		builder.addField(Series.MemoryInformation.FIELD_AVG_FREE_SWAP_SPACE, freeSwapSpace);
		builder.addField(Series.MemoryInformation.FIELD_AVG_COMMITTED_HEAP_MEMORY, committedHeapMemorySize);
		builder.addField(Series.MemoryInformation.FIELD_AVG_COMMITTED_NON_HEAP_MEMORY, committedNonHeapMemorySize);
		builder.addField(Series.MemoryInformation.FIELD_AVG_USED_HEAP_MEMORY, usedHeapMemorySize);
		builder.addField(Series.MemoryInformation.FIELD_MIN_USED_HEAP_MEMORY, data.getMinUsedHeapMemorySize());
		builder.addField(Series.MemoryInformation.FIELD_MAX_USED_HEAP_MEMORY, data.getMaxUsedHeapMemorySize());
		builder.addField(Series.MemoryInformation.FIELD_AVG_USED_NON_HEAP_MEMORY, usedNonHeapMemorySize);
		builder.addField(Series.MemoryInformation.FIELD_MIN_USED_NON_HEAP_MEMORY, data.getMinUsedNonHeapMemorySize());
		builder.addField(Series.MemoryInformation.FIELD_MAX_USED_NON_HEAP_MEMORY, data.getMaxUsedNonHeapMemorySize());
	}

}
