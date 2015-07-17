package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.ITreeComponent;
import info.novatec.inspectit.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

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
	public Object getKey(E element) {
		return element.getClass();
	}

	/**
	 * {@inheritDoc}
	 */

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
	public boolean sharedInstance() {
		return true;
	}

}
