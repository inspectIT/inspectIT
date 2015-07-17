package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

/**
 * {@link IBranchIndexer} that indexes on the platform idents of {@link DefaultData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class PlatformIdentIndexer<E extends DefaultData> extends AbstractSharedInstanceBranchIndexer<E> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (0 == element.getPlatformIdent()) {
			return null;
		}
		return element.getPlatformIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		if (0 == query.getPlatformIdent()) {
			return new Object[0];
		}
		Object[] keys = new Object[1];
		keys[0] = query.getPlatformIdent();
		return keys;
	}

}
