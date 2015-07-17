package info.novatec.inspectit.indexing;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.indexing.buffer.impl.Branch;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Abstract class for all {@link ITreeComponent}s that are a branch.
 * 
 * @author Ivan Senic
 * 
 * @param <R>
 *            Type of the element returned by the branch when querying.
 * @param <E>
 *            Type of the element that can be indexed by the branch.
 */
public abstract class AbstractBranch<R, E> {

	/**
	 * Initial concurrency level for the {@link ConcurrentHashMap}.
	 */
	private static final int CONCURRENCY_LEVEL = 4;

	/**
	 * Branch indexer.
	 */
	private IBranchIndexer<E> branchIndexer;

	/**
	 * Map for holding references.
	 */
	private ConcurrentHashMap<Object, ITreeComponent<R, E>> map;

	/**
	 * Default constructor. {@link Branch} can only be initialized with proper branch indexer
	 * supplied. If null is passed, {@link IllegalArgumentException} will be thrown.
	 * 
	 * @param branchIndexer
	 *            Branch indexer used in this branch.
	 */
	public AbstractBranch(IBranchIndexer<E> branchIndexer) {
		this.branchIndexer = branchIndexer;
		map = new ConcurrentHashMap<Object, ITreeComponent<R, E>>(1, 0.75f, CONCURRENCY_LEVEL);
	}

	/**
	 * Returns branch indexer.
	 * 
	 * @return Branch indexer
	 */
	protected IBranchIndexer<E> getBranchIndexer() {
		return branchIndexer;
	}

	/**
	 * Each concrete implementation should create new component and put the element in it.
	 * 
	 * @param element
	 *            Element to put in new component.
	 * @return Return type by the tree component.
	 */
	protected abstract ITreeComponent<R, E> getNextTreeComponent(E element);

	/**
	 * {@inheritDoc}
	 */
	public R put(E element) throws IndexingException {
		// get key for object
		Object key = branchIndexer.getKey(element);
		if (null == key) {
			throw new IndexingException("Branch indexer " + branchIndexer + " can not create the key for the object " + element + ".");
		}
		// get the tree component for key
		ITreeComponent<R, E> treeComponent = map.get(key);
		if (null != treeComponent) {
			// if component exists, put element into the component
			return treeComponent.put(element);
		} else {
			// check again if the new component already exists
			treeComponent = map.get(key);
			if (null == treeComponent) {
				// otherwise create new tree component, add it to the map and put element into
				treeComponent = this.getNextTreeComponent(element);
				ITreeComponent<R, E> existing = map.putIfAbsent(key, treeComponent);
				if (null != existing) {
					treeComponent = existing;
				}
			}
			return treeComponent.put(element);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public R get(E template) {
		// get key for template
		Object key = branchIndexer.getKey(template);
		// get the tree component for key
		ITreeComponent<R, E> treeComponent = null;
		if (null != key) {
			treeComponent = map.get(key);
		}
		if (null != treeComponent) {
			// if component exists, get element from the component
			return treeComponent.get(template);
		} else if (null == key) {
			// if key could not be created, search into all branches/leafs
			// keys can not be created when the template object does not carry the information that
			// branch indexer on this branch needs
			Iterator<ITreeComponent<R, E>> iterator = map.values().iterator();
			R result = null;
			while (iterator.hasNext()) {
				result = iterator.next().get(template);
				if (null != result) {
					return result;
				}
			}
			return null;
		} else {
			// finally return nothing cause temple object was never put before
			return null;
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public R getAndRemove(E template) {
		// get key for template
		Object key = getBranchIndexer().getKey(template);
		// get the tree component for key
		ITreeComponent<R, E> treeComponent = null;
		if (null != key) {
			treeComponent = map.get(key);
		}
		if (null != treeComponent) {
			// if component exists, get element from the component
			return treeComponent.getAndRemove(template);
		} else if (null == key) {
			// if key could not be created, search into all branches/leafs
			// keys can not be created when the template object does not carry the information that
			// branch indexer on this branch needs
			Iterator<ITreeComponent<R, E>> iterator = map.values().iterator();
			R result = null;
			while (iterator.hasNext()) {
				result = iterator.next().getAndRemove(template);
				if (null != result) {
					return result;
				}
			}
			return null;
		} else {
			// finally return nothing cause temple object was never put before
			return null;
		}
	};

	/**
	 * {@inheritDoc}
	 */
	public List<R> query(IIndexQuery query) {
		// get all keys for query
		Object[] keys = getBranchIndexer().getKeys(query);
		if (ArrayUtils.isEmpty(keys)) {
			// if key can not be created search in next level
			return queryAllTreeComponents(query);
		} else if (1 == keys.length) {
			// if only one key is returned, search in exactly this one
			return querySingleKey(query, keys[0]);
		} else {
			// combine results for all keys
			List<R> results = new ArrayList<R>();
			for (Object key : keys) {
				List<R> componentResult = querySingleKey(query, key);
				if (null != componentResult && !componentResult.isEmpty()) {
					results.addAll(componentResult);
				}
			}
			return results;
		}
	}

	/**
	 * Queries the single {@link ITreeComponent} that is mapped with key. If passed key is null, or
	 * if there is no component mapped with given key, result will be empty list.
	 * 
	 * @param query
	 *            Query to process.
	 * @param key
	 *            Mapping key value for {@link ITreeComponent}.
	 * @return Result from queried {@link ITreeComponent} or empty list if key is null or none of
	 *         {@link ITreeComponent} is mapped with given key.
	 */
	protected List<R> querySingleKey(IIndexQuery query, Object key) {
		// get tree component for key
		ITreeComponent<R, E> treeComponent = null;
		if (null != key) {
			treeComponent = map.get(key);
		}
		if (null != treeComponent) {
			// if it is found search in that one
			return treeComponent.query(query);
		} else {
			// finally this brunch did not find anything that matches the search
			return Collections.emptyList();
		}
	}

	/**
	 * Returns results from all branches for given query.
	 * 
	 * @param query
	 *            Query to process.
	 * @return Combined result from all {@link ITreeComponent}s that are mapped in this branch.
	 */
	protected List<R> queryAllTreeComponents(IIndexQuery query) {
		List<R> results = new ArrayList<R>();
		Iterator<ITreeComponent<R, E>> iterator = map.values().iterator();
		while (iterator.hasNext()) {
			List<R> componentResult = iterator.next().query(query);
			if (null != componentResult && !componentResult.isEmpty()) {
				results.addAll(componentResult);
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
		size += objectSizes.getPrimitiveTypesSize(2, 0, 0, 0, 0, 0);
		size += objectSizes.getSizeOfConcurrentHashMap(mapSize, CONCURRENCY_LEVEL);
		size += mapSize * objectSizes.getSizeOfLongObject(); // for a Long key in a Map.entry
		for (ITreeComponent<R, E> treeComponent : map.values()) {
			size += treeComponent.getComponentSize(objectSizes);
		}
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAll() {
		map.clear();
	}

	/**
	 * @return Returns component map, so that subclasses can handle specific tasks.
	 */
	public Map<Object, ITreeComponent<R, E>> getComponentMap() {
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branchIndexer == null) ? 0 : branchIndexer.hashCode());
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		AbstractBranch<R, E> other = (AbstractBranch<R, E>) obj;
		if (branchIndexer == null) {
			if (other.branchIndexer != null) {
				return false;
			}
		} else if (!branchIndexer.equals(other.branchIndexer)) {
			return false;
		}
		if (map == null) {
			if (other.map != null) {
				return false;
			}
		} else if (!map.equals(other.map)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("branchIndexer", branchIndexer);
		toStringBuilder.append("branchMap", map);
		return toStringBuilder.toString();
	}
}
