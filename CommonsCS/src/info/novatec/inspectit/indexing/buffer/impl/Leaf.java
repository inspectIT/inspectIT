package info.novatec.inspectit.indexing.buffer.impl;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Leaf class is the one that holds the weak references to objects, thus last in tree structure.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Element type that the leaf can index (and hold).
 */
public class Leaf<E extends DefaultData> implements IBufferTreeComponent<E> {

	/**
	 * Initial concurrency level for the {@link ConcurrentHashMap}.
	 */
	private static final int CONCURRENCY_LEVEL = 4;

	/**
	 * Map for week references.
	 */
	private Map<Long, CustomWeakReference<E>> map;

	/**
	 * Reference queue where cleared Weak references are queued by garbage collection.
	 */
	private ReferenceQueue<E> referenceQueue;

	/**
	 * Clear runnable for this Leaf.
	 */
	private Runnable clearRunnable = new Runnable() {
		public void run() {
			Leaf.this.clean();
		}
	};

	/**
	 * Future that holds state of clear runnable.
	 */
	private Future<?> clearFuture;

	/**
	 * Default constructor.
	 */
	public Leaf() {
		map = new ConcurrentHashMap<Long, CustomWeakReference<E>>(1, 0.75f, CONCURRENCY_LEVEL);
		referenceQueue = new ReferenceQueue<E>();
	}

	/**
	 * {@inheritDoc}
	 */
	public E put(E element) {
		CustomWeakReference<E> weakReference = new CustomWeakReference<E>(element, referenceQueue);
		map.put(element.getId(), weakReference);
		return element;
	}

	/**
	 * {@inheritDoc}
	 */
	public E get(E template) {
		long id = template.getId();
		WeakReference<E> weakReference = map.get(id);
		if (null != weakReference) {
			if (null == weakReference.get()) {
				map.remove(id);
				return null;
			}
			return weakReference.get();
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public E getAndRemove(E template) {
		long id = template.getId();
		WeakReference<E> weakReference = map.get(id);
		if (null != weakReference) {
			if (null == weakReference.get()) {
				map.remove(id);
				return null;
			} else {
				E result = weakReference.get();
				map.remove(id);
				return result;
			}
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<E> query(IIndexQuery query) {
		List<E> results = new ArrayList<E>();
		Iterator<CustomWeakReference<E>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			WeakReference<E> weakReference = iterator.next();
			if (null != weakReference) {
				E element = weakReference.get();
				if (null != element && element.isQueryComplied(query)) {
					results.add(weakReference.get());
				}
			}
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getComponentSize(IObjectSizes objectSizes) {
		int mapSize = map.size();
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size += objectSizes.getSizeOfConcurrentHashMap(mapSize, CONCURRENCY_LEVEL);
		// for Long and CustomWeekReference in a Map.Entry
		size += map.size() * (objectSizes.getSizeOfLongObject() + 24);
		return size;
		// the size of the reference queue, runnable and future and not included, because they are
		// simply to small and its size is constant and does not depend on the number of elements in
		// the leaf
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean clean() {
		List<Long> toClean = new ArrayList<Long>();
		CustomWeakReference<E> customWeakReference = (CustomWeakReference<E>) referenceQueue.poll();
		while (customWeakReference != null) {
			toClean.add(customWeakReference.getReferentId());
			customWeakReference = (CustomWeakReference<E>) referenceQueue.poll();
		}
		for (Object key : toClean) {
			map.remove(key);
		}
		if (map.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getNumberOfElements() {
		return map.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAll() {
		map.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public void cleanWithRunnable(ExecutorService executorService) {
		if (clearFuture == null || clearFuture.isDone()) {
			clearFuture = executorService.submit(clearRunnable);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean clearEmptyComponents() {
		return map.isEmpty();
	}

	/**
	 * Custom extension of {@link WeakReference} that will additionally hold the id of the referent
	 * {@link DefaultData} object.
	 * 
	 * @author Ivan Senic
	 * 
	 * @param <E>
	 */
	private class CustomWeakReference<T extends DefaultData> extends WeakReference<T> {

		/**
		 * Id of refering object.
		 */
		private long referentId;

		/**
		 * Default constructor.
		 * 
		 * @param referent
		 *            Object to refer to.
		 * @param q
		 *            Reference queue to register Weak reference with.
		 * @see WeakReference#WeakReference(Object, ReferenceQueue)
		 */
		public CustomWeakReference(T referent, ReferenceQueue<? super T> q) {
			super(referent, q);
			referentId = referent.getId();
		}

		/**
		 * @return The ID of the {@link DefaultData} object {@link WeakReference} is refering to.
		 */
		public long getReferentId() {
			return referentId;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("elementsMap", map);
		return toStringBuilder.toString();
	}
}
