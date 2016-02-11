package info.novatec.inspectit.storage.processor.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.IIdsAwareAggregatedData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.storage.processor.AbstractDataProcessor;
import info.novatec.inspectit.storage.serializer.util.KryoSerializationPreferences;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;

/**
 * This class aggregates data and writes only aggregated objects to the writer.
 * 
 * @param <E>
 *            Type of data to aggregate.
 * 
 * @author Ivan Senic
 * 
 */
public class DataAggregatorProcessor<E extends TimerData> extends AbstractDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -883029940157040641L;

	/**
	 * Max elements by default.
	 */
	private static final int DEFAULT_MAX_ELEMENTS = 200;

	/**
	 * Period of time in which all timer data should be aggregated. In milliseconds.
	 */
	private long aggregationPeriod;

	/**
	 * Max elements in the cache.
	 */
	private int maxElements;

	/**
	 * Current element count in cache.
	 */
	private AtomicInteger elementCount = new AtomicInteger(0);

	/**
	 * Map for caching.
	 */
	private ConcurrentHashMap<Integer, IAggregatedData<E>> map = new ConcurrentHashMap<Integer, IAggregatedData<E>>(64, 0.75f, 4);

	/**
	 * Queue for knowing the order.
	 */
	private ConcurrentLinkedQueue<IAggregatedData<E>> queue = new ConcurrentLinkedQueue<IAggregatedData<E>>();

	/**
	 * List of classes that should be aggregated by this processor. Only instances of
	 * {@link TimerData} can be aggregated.
	 */
	private Class<E> clazz;

	/**
	 * Timer data aggregator.
	 */
	private IAggregator<E> dataAggregator;

	/**
	 * If invocation affiliation should be written along side {@link InvocationAwareData}.
	 */
	private boolean writeInvocationAffiliation;

	/**
	 * No-arg constructor.
	 */
	public DataAggregatorProcessor() {
	}

	/**
	 * Default constructor. Sets max elements to {@value #DEFAULT_MAX_ELEMENTS}.
	 * 
	 * @param clazz
	 *            List of classes to be saved to storage by this {@link AbstractDataProcessor}.
	 * @param aggregationPeriod
	 *            Period of time in which data should be aggregated. In milliseconds.
	 * @param dataAggregator
	 *            {@link IAggregator} used to aggregate data. Must not be null.
	 * @param writeInvocationAffiliation
	 *            If invocation affiliation should be written along side {@link InvocationAwareData}
	 */
	public DataAggregatorProcessor(Class<E> clazz, long aggregationPeriod, IAggregator<E> dataAggregator, boolean writeInvocationAffiliation) {
		this(clazz, aggregationPeriod, DEFAULT_MAX_ELEMENTS, dataAggregator, writeInvocationAffiliation);
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param clazz
	 *            List of classes to be saved to storage by this {@link AbstractDataProcessor}.
	 * @param aggregationPeriod
	 *            Period of time in which data should be aggregated. In milliseconds.
	 * @param maxElements
	 *            Max elements in the cache of the processor.
	 * @param dataAggregator
	 *            {@link IAggregator} used to aggregate data. Must not be null.
	 * @param writeInvocationAffiliation
	 *            If invocation affiliation should be written along side {@link InvocationAwareData}
	 */
	public DataAggregatorProcessor(Class<E> clazz, long aggregationPeriod, int maxElements, IAggregator<E> dataAggregator, boolean writeInvocationAffiliation) {
		if (null == clazz) {
			throw new IllegalArgumentException("Aggregation class can not be null.");
		}
		if (aggregationPeriod <= 0) {
			throw new IllegalArgumentException("Aggregation period must be a positive number greater than zero.");
		}
		if (maxElements <= 0) {
			throw new IllegalArgumentException("Max elements must be a positive number greater than zero.");
		}
		if (null == dataAggregator) {
			throw new IllegalArgumentException("Aggregator can not be null.");
		}
		this.clazz = clazz;
		this.aggregationPeriod = aggregationPeriod;
		this.maxElements = maxElements;
		this.dataAggregator = dataAggregator;
		this.clazz = clazz;
		this.writeInvocationAffiliation = writeInvocationAffiliation;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	protected Collection<Future<Void>> processData(DefaultData defaultData) {
		E timerData = (E) defaultData;
		long alteredTimestamp = getAlteredTimestamp(timerData);
		int cacheHash = getCacheHash(timerData, alteredTimestamp);

		IAggregatedData<E> aggData = map.get(cacheHash);
		if (null == aggData) {
			aggData = clone(timerData, alteredTimestamp);
			IAggregatedData<E> insertedData = map.putIfAbsent(cacheHash, aggData);
			// if put happened null will be returned
			if (null == insertedData) {
				queue.add(aggData);
				int count = elementCount.incrementAndGet();
				if (maxElements < count) {
					this.writeOldest();
				}
			} else {
				aggData = insertedData;
			}
		}
		dataAggregator.aggregate(aggData, timerData);
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBeProcessed(DefaultData defaultData) {
		if (null != defaultData) {
			return clazz.equals(defaultData.getClass());
		}
		return false;
	}

	/**
	 * Writes the oldest data to the storage.
	 */
	private void writeOldest() {
		IAggregatedData<E> oldest = queue.poll();
		E data = oldest.getData();
		map.remove(getCacheHash(data, data.getTimeStamp().getTime()));
		data.finalizeData();
		elementCount.decrementAndGet();
		passToStorageWriter(data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<Future<Void>> flush() {
		Collection<Future<Void>> futures = new ArrayList<Future<Void>>();
		IAggregatedData<E> oldest = queue.poll();
		while (null != oldest) {
			E data = oldest.getData();
			map.remove(getCacheHash(data, data.getTimeStamp().getTime()));
			data.finalizeData();
			elementCount.decrementAndGet();
			Future<Void> future = passToStorageWriter(data);
			CollectionUtils.addIgnoreNull(futures, future);

			oldest = queue.poll();
		}
		return futures;
	}

	/**
	 * Passes data to StorageWriter to be written.
	 * 
	 * @param data
	 *            Data to be written.
	 * @return {@link Future} received from Storage writer.
	 */
	private Future<Void> passToStorageWriter(E data) {
		// clear aggregated ids when saving to storage
		if (data instanceof IIdsAwareAggregatedData) {
			((IIdsAwareAggregatedData<?>) data).clearAggregatedIds();
		}

		// if I am writing the InvocationAwareData and invocations are not saved
		// make sure we don't save the invocation affiliation
		if (!writeInvocationAffiliation) {
			Map<String, Boolean> kryoPreferences = new HashMap<String, Boolean>(1);
			kryoPreferences.put(KryoSerializationPreferences.WRITE_INVOCATION_AFFILIATION_DATA, Boolean.FALSE);
			return getStorageWriter().write(data, kryoPreferences);
		} else {
			return getStorageWriter().write(data);
		}
	}

	/**
	 * Returns the cache hash code.
	 * 
	 * @param timerData
	 *            Object to calculate cache hash for.
	 * @param timestampValue
	 *            Time stamp value as long.
	 * @return Cache hash for the given set of values.
	 */
	private int getCacheHash(E timerData, long timestampValue) {
		final int prime = 31;
		Object key = dataAggregator.getAggregationKey(timerData);
		int result = key.hashCode();
		result = prime * result + (int) (timestampValue ^ (timestampValue >>> 32));
		return result;
	}

	/**
	 * Returns the value of the time stamp based on a aggregation period.
	 * 
	 * @param timerData
	 *            {@link TimerData} to get aggregation time stamp.
	 * @return Aggregation time stamp.
	 */
	private long getAlteredTimestamp(TimerData timerData) {
		long timestampValue = timerData.getTimeStamp().getTime();
		long newTimestampValue = timestampValue - timestampValue % aggregationPeriod;
		return newTimestampValue;
	}

	/**
	 * Creates new {@link TimerData} object for aggregation purposes.
	 * 
	 * @param timerData
	 *            {@link TimerData} to clone.
	 * @param alteredTimestamp
	 *            New altered time stamp clone to have.
	 * @return Cloned object ready for aggregation.
	 */
	private IAggregatedData<E> clone(E timerData, long alteredTimestamp) {
		IAggregatedData<E> clone = dataAggregator.getClone(timerData);
		clone.getData().setId(timerData.getId());
		clone.getData().setTimeStamp(new Timestamp(alteredTimestamp));
		return clone;
	}

}
