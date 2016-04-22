package rocks.inspectit.shared.cs.indexing.indexer.impl;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.IBranchIndexer;

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
	@Override
	public Object getKey(E element) {
		if (0 == element.getPlatformIdent()) {
			return null;
		}
		return element.getPlatformIdent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getKeys(IIndexQuery query) {
		if (0 == query.getPlatformIdent()) {
			return new Object[0];
		}
		Object[] keys = new Object[1];
		keys[0] = query.getPlatformIdent();
		return keys;
	}

}
