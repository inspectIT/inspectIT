package info.novatec.inspectit.indexing.indexer;

import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.impl.Branch;

/**
 * {@link IBranchIndexer} supplies the {@link Branch} with information about mapping keys.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Element type that indexer can return keys for.
 */
public interface IBranchIndexer<E> {

	/**
	 * Returns the key for one element.
	 * 
	 * @param element
	 *            the element.
	 * @return Key or null if passed element is null, or indexing value is not set.
	 */
	Object getKey(E element);

	/**
	 * Return arrays of mapping keys that correspond to the passed query.
	 * 
	 * @param query
	 *            the query.
	 * @return Keys or null if no keys are associated with query.
	 */
	Object[] getKeys(IIndexQuery query);

	/**
	 * Returns if the implementation of the {@link IBranchIndexer} can be used as shared instance,
	 * meaning if all branches on the level where this indexer is used can use one and same
	 * instance.
	 * 
	 * @return If instance of indexer is shared between all indexers of same type.
	 */
	boolean sharedInstance();

	/**
	 * Returns the new instance of the same {@link IBranchIndexer} with correct relationship to its
	 * child indexer. This method will be used only if {@link #sharedInstance()} returns false, thus
	 * with indexer that can not have same instance for all branches on the level where it is used.
	 * 
	 * @return New instance of indexer of same type.
	 */
	IBranchIndexer<E> getNewInstance();

}
