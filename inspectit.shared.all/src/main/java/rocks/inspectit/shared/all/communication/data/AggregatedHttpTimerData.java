package rocks.inspectit.shared.all.communication.data;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.IIdsAwareAggregatedData;

/**
 * Aggregated {@link TimerData} object.
 *
 * @author Ivan Senic
 *
 */
public class AggregatedHttpTimerData extends HttpTimerData implements IIdsAwareAggregatedData<HttpTimerData> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -6077701184038575208L;

	/**
	 * Aggregated IDs. We need a set functionality so we will simulate it with the Map (SetFromMap
	 * is not available in Java5). The values in this map should always be the {@link Boolean#TRUE}
	 * since the keys are only values we are interested in.
	 */
	@SuppressWarnings("CPD-START")
	private Map<Long, Boolean> aggregatedIds = new ConcurrentHashMap<Long, Boolean>(16, 0.75f, 4);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aggregate(HttpTimerData data) {
		this.aggregateHttpTimerData(data);
		if (data instanceof AggregatedHttpTimerData) {
			AggregatedHttpTimerData aggregatedData = (AggregatedHttpTimerData) data;
			if (null != aggregatedData.getAggregatedIds()) {
				for (Long id : aggregatedData.getAggregatedIds()) {
					aggregatedIds.put(id, Boolean.TRUE);
				}
			}
		} else if (0 != data.getId()) {
			aggregatedIds.put(data.getId(), Boolean.TRUE);
		}
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Long> getAggregatedIds() {
		return aggregatedIds.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAggregatedIds() {
		aggregatedIds.clear();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpTimerData getData() {
		return this;
	}

	/**
	 * Aggregates the {@link HttpTimerData}.
	 *
	 * @param data
	 *            {@link HttpTimerData}
	 */
	public void aggregateHttpTimerData(HttpTimerData data) {
		super.aggregateTimerData(data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		if (null != aggregatedIds) {
			size += objectSizes.getSizeOfConcurrentHashMap(aggregatedIds.size(), 4);
			size += aggregatedIds.size() * objectSizes.getSizeOfLongObject();
		}
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((aggregatedIds == null) ? 0 : aggregatedIds.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AggregatedHttpTimerData other = (AggregatedHttpTimerData) obj;
		if (aggregatedIds == null) {
			if (other.aggregatedIds != null) {
				return false;
			}
		} else if (!aggregatedIds.equals(other.aggregatedIds)) {
			return false;
		}
		return true;
	}

}
