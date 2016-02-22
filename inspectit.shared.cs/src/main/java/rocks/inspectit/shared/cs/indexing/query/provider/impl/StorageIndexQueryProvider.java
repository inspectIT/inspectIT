package rocks.inspectit.shared.cs.indexing.query.provider.impl;

import rocks.inspectit.shared.cs.indexing.query.provider.IIndexQueryProvider;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

/**
 * Abstract class for providing the {@link StorageIndexQuery} by Spring.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class StorageIndexQueryProvider implements IIndexQueryProvider<StorageIndexQuery> {

	/**
	 * Creates properly initialized {@link StorageIndexQuery}.
	 * 
	 * @return Returns properly initialized {@link StorageIndexQuery}.
	 */
	public abstract StorageIndexQuery createNewStorageIndexQuery();

	/**
	 * {@inheritDoc}
	 */
	public StorageIndexQuery getIndexQuery() {
		return createNewStorageIndexQuery();
	}
}
