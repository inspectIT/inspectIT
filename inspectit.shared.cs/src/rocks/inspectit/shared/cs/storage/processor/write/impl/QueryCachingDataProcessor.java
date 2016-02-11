package info.novatec.inspectit.storage.processor.write.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationAwareData.MutableInt;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.StorageWriter;
import info.novatec.inspectit.storage.processor.write.AbstractWriteDataProcessor;
import info.novatec.inspectit.storage.serializer.util.KryoSerializationPreferences;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processor that can create a cached result set for a storage with given query and aggregator.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of elements processed.
 */
public class QueryCachingDataProcessor<E extends DefaultData> extends AbstractWriteDataProcessor {

	/**
	 * {@link IIndexQuery} to take into consideration.
	 */
	private IIndexQuery query;

	/**
	 * {@link IAggregator} for aggregaton.
	 */
	private IAggregator<E> aggregator;

	/**
	 * {@link AggregationPerformer}s must be separated by platform ID.
	 */
	private ConcurrentHashMap<Long, AggregationPerformer<E>> aggregationPerformerMap;

	/**
	 * @param query
	 *            {@link IIndexQuery} to take into consideration.
	 * @param aggregator
	 *            {@link IAggregator} for aggregaton.
	 */
	public QueryCachingDataProcessor(IIndexQuery query, IAggregator<E> aggregator) {
		this.query = query;
		this.aggregator = aggregator;
		this.aggregationPerformerMap = new ConcurrentHashMap<>(4, 0.75f, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void processData(DefaultData defaultData, Map<?, ?> kryoPreferences) {
		Long key = Long.valueOf(defaultData.getPlatformIdent());
		AggregationPerformer<E> aggregationPerformer = aggregationPerformerMap.get(key);
		if (null == aggregationPerformer) {
			aggregationPerformer = aggregationPerformerMap.putIfAbsent(key, new AggregationPerformer<>(aggregator));
			if (null == aggregationPerformer) {
				aggregationPerformer = aggregationPerformerMap.get(key);
			}
		}

		// deal with no saving of the invocation affiliation for cached views as well
		if (Boolean.FALSE.equals(kryoPreferences.get(KryoSerializationPreferences.WRITE_INVOCATION_AFFILIATION_DATA)) && defaultData instanceof InvocationAwareData) {
			InvocationAwareData invocationAwareData = (InvocationAwareData) defaultData;
			Map<Long, MutableInt> temp = invocationAwareData.getInvocationsParentsIdMap();
			invocationAwareData.setInvocationsParentsIdMap(Collections.<Long, MutableInt> emptyMap());
			aggregationPerformer.processElement((E) defaultData);
			invocationAwareData.setInvocationsParentsIdMap(temp);
		} else {
			aggregationPerformer.processElement((E) defaultData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData.isQueryComplied(query);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onFinalization(StorageManager storageManager, StorageWriter storageWriter, StorageData storageData) throws Exception {
		for (Map.Entry<Long, AggregationPerformer<E>> entry : aggregationPerformerMap.entrySet()) {
			query.setPlatformIdent(entry.getKey().longValue());
			AggregationPerformer<E> aggregationPerformer = entry.getValue();
			storageManager.cacheStorageData(storageData, aggregationPerformer.getResultList(), storageManager.getCachedDataHash(query, aggregator));
		}
	}

}
