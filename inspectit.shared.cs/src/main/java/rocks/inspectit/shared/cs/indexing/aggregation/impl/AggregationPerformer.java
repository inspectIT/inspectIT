package rocks.inspectit.shared.cs.indexing.aggregation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.IAggregatedData;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;

/**
 * This class encapsulates the aggregation process. When ever aggregation is needed, it this class
 * should be used with combination of available {@link IAggregator}s.
 *
 * @author Ivan Senic
 *
 * @param <E>
 *            Type to be aggregated.
 */
public class AggregationPerformer<E extends DefaultData> {

	/**
	 * Map for caching.
	 */
	private Map<Object, IAggregatedData<E>> aggregationMap;

	/**
	 * {@link IAggregator} used.
	 */
	private IAggregator<E> aggregator;

	/**
	 * Default constructor.
	 *
	 * @param aggregator
	 *            {@link IAggregator} to use. Must not be <code>null</code>.
	 */
	public AggregationPerformer(IAggregator<E> aggregator) {
		if (null == aggregator) {
			throw new IllegalArgumentException("Aggregator can not be null.");
		}
		this.aggregator = aggregator;
		this.aggregationMap = new HashMap<>();
	}

	/**
	 * Process one element.
	 *
	 * @param element
	 *            Element to process.
	 */
	public void processElement(E element) {
		Object key = aggregator.getAggregationKey(element);
		IAggregatedData<E> aggregatedObject = aggregationMap.get(key);
		if (null != aggregatedObject) {
			aggregator.aggregate(aggregatedObject, element);
		} else {
			aggregatedObject = aggregator.getClone(element);
			aggregationMap.put(key, aggregatedObject);
			aggregator.aggregate(aggregatedObject, element);
		}
	}

	/**
	 * Process the collection of elements.
	 *
	 * @param collection
	 *            Collection that should be aggregated.
	 */
	public void processCollection(Collection<E> collection) {
		for (E element : collection) {
			processElement(element);
		}
	}

	/**
	 * Process the list of elements starting from the fromIndex (inclusive) to toIndex (exclusive).
	 *
	 * @param list
	 *            List of elements.
	 * @param fromIndex
	 *            Starting index.
	 * @param toIndex
	 *            Ending index.
	 */
	public void processList(List<E> list, int fromIndex, int toIndex) {
		int size = list.size();
		if ((fromIndex < 0) || (fromIndex >= size)) {
			throw new IllegalArgumentException("Starting index " + fromIndex + " is not valid for given list of size " + size);
		}

		if ((toIndex < fromIndex) || (toIndex > size)) {
			throw new IllegalArgumentException("Ending index " + toIndex + " is not valid for given list of size " + size + " and starting index " + fromIndex);
		}

		for (int i = fromIndex; i < toIndex; i++) {
			E element = list.get(i);
			processElement(element);
		}

	}

	/**
	 * Returns aggregation results.
	 *
	 * @return Returns aggregation results.
	 */
	public List<E> getResultList() {
		List<E> returnList = new ArrayList<>();
		for (IAggregatedData<E> aggregatedData : aggregationMap.values()) {
			returnList.add(aggregatedData.getData());
		}
		return returnList;
	}

	/**
	 * Resets the current results of the aggregations so that the new clean aggregation can start.
	 */
	public void reset() {
		aggregationMap.clear();
	}

}
