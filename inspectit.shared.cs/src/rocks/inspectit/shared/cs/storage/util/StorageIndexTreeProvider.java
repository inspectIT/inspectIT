package info.novatec.inspectit.storage.util;

import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

/**
 * Abstract class that is enhanced from Spring to provide the storage indexing tree.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of elements in the tree.
 */
public abstract class StorageIndexTreeProvider<E> {

	/**
	 * Returns the storage indexing tree.
	 * 
	 * @return Returns the storage indexing tree.
	 */
	public abstract IStorageTreeComponent<E> getStorageIndexingTree();
}
