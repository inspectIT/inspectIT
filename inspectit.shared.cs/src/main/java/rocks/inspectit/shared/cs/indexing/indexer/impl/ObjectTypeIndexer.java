package rocks.inspectit.shared.cs.indexing.indexer.impl;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.ITreeComponent;
import rocks.inspectit.shared.cs.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.IBranchIndexer;

/**
 * {@link IBranchIndexer} that makes indexes based on a object class. Thus all same object types
 * will be in one {@link ITreeComponent}.
 *
 * @author Ivan Senic
 *
 * @param <E>
 */
public class ObjectTypeIndexer<E extends DefaultData> extends AbstractSharedInstanceBranchIndexer<E> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getKey(E element) {
		return element.getClass();
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public Object[] getKeys(IIndexQuery query) {
		if (null == query.getObjectClasses()) {
			return new Object[0];
		}
		Object[] keys = new Object[query.getObjectClasses().size()];
		int index = 0;
		for (Object key : query.getObjectClasses()) {
			keys[index++] = key;
		}
		return keys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean sharedInstance() {
		return true;
	}

}
