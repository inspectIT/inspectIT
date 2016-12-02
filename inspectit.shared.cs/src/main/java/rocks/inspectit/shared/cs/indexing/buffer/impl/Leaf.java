package rocks.inspectit.shared.cs.indexing.buffer.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.LeafTask;
import rocks.inspectit.shared.cs.indexing.buffer.IBufferTreeComponent;

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
	 * Map for weak references.
	 */
	private NonBlockingHashMapLong<CustomWeakReference<E>> map;

	/**
	 * Reference queue where cleared Weak references are queued by garbage collection.
	 */
	private ReferenceQueue<E> referenceQueue;

	/**
	 * Clear runnable for this Leaf.
	 */
	private Runnable clearRunnable = new Runnable() {
		@Override
		public void run() {
			Leaf.this.clean();
		}
	};

	/**
	 * RunnableFuture that holds state of clear runnable.
	 */
	private Future<?> clearFuture;

	/**
	 * Default constructor.
	 */
	public Leaf() {
		map = new NonBlockingHashMapLong<>();
		referenceQueue = new ReferenceQueue<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E put(E element) {
		CustomWeakReference<E> weakReference = new CustomWeakReference<>(element, referenceQueue);
		map.put(element.getId(), weakReference);
		return element;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
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
	@Override
	public List<E> query(IIndexQuery query) {
		List<E> results = new ArrayList<>();
		Iterator<CustomWeakReference<E>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			WeakReference<E> weakReference = iterator.next();
			if (null != weakReference) {
				E element = weakReference.get();
				if ((null != element) && element.isQueryComplied(query)) {
					results.add(weakReference.get());
				}
			}
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<E> query(IIndexQuery query, ForkJoinPool forkJoinPool) {
		return forkJoinPool.invoke(getTaskForForkJoinQuery(query));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getComponentSize(IObjectSizes objectSizes) {
		int mapSize = map.size();
		long size = objectSizes.getSizeOfObjectHeader();
		size += objectSizes.getPrimitiveTypesSize(1, 0, 0, 0, 0, 0);
		size = objectSizes.alignTo8Bytes(size);

		// map
		size += objectSizes.getSizeOfNonBlockingHashMapLong(mapSize);
		// for each CustomWeakReference in a map
		size += map.size() * objectSizes.getSizeOfCustomWeakReference();

		return size;
		// the size of the reference queue, runnable and future and not included, because they are
		// simply to small and its size is constant and does not depend on the number of elements in
		// the leaf
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean clean() {
		List<Long> toClean = new ArrayList<>();
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
	@Override
	public long getNumberOfElements() {
		return map.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAll() {
		map.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanWithRunnable(ExecutorService executorService) {
		if ((clearFuture == null) || clearFuture.isDone()) {
			clearFuture = executorService.submit(clearRunnable);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	private static class CustomWeakReference<T extends DefaultData> extends WeakReference<T> {

		/**
		 * Id of referring object.
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RecursiveTask<List<E>> getTaskForForkJoinQuery(IIndexQuery query) {
		return new LeafTask<>(this, query);
	}
}
