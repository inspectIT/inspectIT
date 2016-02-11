package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link IBranchIndexer} that indexes on the timestamp of the {@link DefaultData}. The index is
 * calculated in the way that a key in made for each {@link #indexingPeriod/1000} seconds of time.2
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class TimestampIndexer<E extends DefaultData> implements IBranchIndexer<E> {

	/**
	 * Constant for empty keys.
	 */
	private static final Object[] EMPTY_KEYS = new Object[0];

	/**
	 * Indexing period. Value is {@value #INDEXING_PERIOD} milliseconds.
	 * <p>
	 * ISE: Increased to 15 minutes, because it s not necessary to have such a strict limit.
	 */
	private static final long INDEXING_PERIOD = 15 * 60 * 1000;

	/**
	 * To make this class serializable and support concurrency we have to serialize the map, and can
	 * not use set because there is not concurrent set. The java.util.Collections$SetFromMap is not
	 * being able to be serialized because it has no no-arg constructor.
	 */
	private ConcurrentHashMap<Long, Boolean> createdKeysMap = new ConcurrentHashMap<Long, Boolean>(8, 0.75f, 1);

	/**
	 * Min created key. Used for providing keys for queries.
	 */
	private long minCreatedKey = Long.MAX_VALUE;

	/**
	 * Max created key. Used for providing keys for queries.
	 */
	private long maxCreatedKey = 0;

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (null == element.getTimeStamp()) {
			return null;
		}
		long key = getKey(element.getTimeStamp());
		createdKeysMap.put(Long.valueOf(key), Boolean.TRUE);
		if (key < minCreatedKey) {
			minCreatedKey = key;
		}
		if (key > maxCreatedKey) {
			maxCreatedKey = key;
		}
		return key;
	}

	/**
	 * {@inheritDoc}
	 */

	public Object[] getKeys(IIndexQuery query) {
		if (!query.isIntervalSet()) {
			return EMPTY_KEYS; // NOPMD
		}

		long startKey = 0;
		if (null != query.getFromDate()) {
			startKey = getKey(query.getFromDate());
		}
		if (startKey < minCreatedKey) {
			startKey = minCreatedKey;
		}

		long endKey = Long.MAX_VALUE;
		if (null != query.getToDate()) {
			endKey = getKey(query.getToDate());
		}
		if (endKey > maxCreatedKey) {
			endKey = maxCreatedKey;
		}

		int size = (int) ((endKey - startKey) / INDEXING_PERIOD + 1);
		ArrayList<Object> keysList = new ArrayList<Object>();
		for (int i = 0; i < size; i++) {
			long key = startKey + i * INDEXING_PERIOD;
			if (createdKeysMap.containsKey(key)) {
				keysList.add(key);
			}
		}
		return keysList.toArray(new Object[keysList.size()]);
	}

	/**
	 * Returns proper key for given timestamp.
	 * 
	 * @param timestamp
	 *            Timestamp to map.
	 * @return Mapping key.
	 */
	private long getKey(Timestamp timestamp) {
		return timestamp.getTime() - timestamp.getTime() % INDEXING_PERIOD;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean sharedInstance() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBranchIndexer<E> getNewInstance() {
		return new TimestampIndexer<E>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdKeysMap == null) ? 0 : createdKeysMap.hashCode());
		result = prime * result + (int) (maxCreatedKey ^ (maxCreatedKey >>> 32));
		result = prime * result + (int) (minCreatedKey ^ (minCreatedKey >>> 32));
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TimestampIndexer<E> other = (TimestampIndexer<E>) obj;
		if (createdKeysMap == null) {
			if (other.createdKeysMap != null) {
				return false;
			}
		} else if (!createdKeysMap.equals(other.createdKeysMap)) {
			return false;
		}
		if (maxCreatedKey != other.maxCreatedKey) {
			return false;
		}
		if (minCreatedKey != other.minCreatedKey) {
			return false;
		}
		return true;
	}

}
