package rocks.inspectit.shared.cs.storage.processor.write.impl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData.MutableInt;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.all.serializer.util.KryoSerializationPreferences;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.AggregationPerformer;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageManager;
import rocks.inspectit.shared.cs.storage.StorageWriter;
import rocks.inspectit.shared.cs.storage.processor.write.AbstractWriteDataProcessor;

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
		if (Boolean.FALSE.equals(kryoPreferences.get(KryoSerializationPreferences.WRITE_INVOCATION_AFFILIATION_DATA)) && (defaultData instanceof InvocationAwareData)) {
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
