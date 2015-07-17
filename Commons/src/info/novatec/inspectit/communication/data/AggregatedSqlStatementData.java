package info.novatec.inspectit.communication.data;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.IIdsAwareAggregatedData;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aggregated {@link TimerData} object.
 * 
 * @author Ivan Senic
 * 
 */
public class AggregatedSqlStatementData extends SqlStatementData implements IIdsAwareAggregatedData<SqlStatementData> {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -2807612877160083229L;

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
	public void aggregate(SqlStatementData data) {
		this.aggregateSqlStatementData(data);
		if (data instanceof AggregatedSqlStatementData) {
			AggregatedSqlStatementData aggregatedData = (AggregatedSqlStatementData) data;
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
	public Collection<Long> getAggregatedIds() {
		return aggregatedIds.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAggregatedIds() {
		aggregatedIds.clear();

	}

	/**
	 * {@inheritDoc}
	 */
	public SqlStatementData getData() {
		return this;
	}

	/**
	 * Aggregates the {@link SqlStatementData}.
	 * 
	 * @param data
	 *            {@link SqlStatementData}
	 */
	public void aggregateSqlStatementData(SqlStatementData data) {
		super.aggregateTimerData(data);
	}

	/**
	 * {@inheritDoc}
	 */
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
		result = prime * result + ((aggregatedIds == null) ? 0 : aggregatedIds.hashCode());
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
		AggregatedSqlStatementData other = (AggregatedSqlStatementData) obj;
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
