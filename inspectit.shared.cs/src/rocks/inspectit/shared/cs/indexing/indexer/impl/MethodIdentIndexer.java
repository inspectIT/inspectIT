package info.novatec.inspectit.indexing.indexer.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

/**
 * {@link IBranchIndexer} that indexes on the method idents of {@link MethodSensorData}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class MethodIdentIndexer<E extends DefaultData> extends AbstractSharedInstanceBranchIndexer<E> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	public Object getKey(E element) {
		if (element instanceof MethodSensorData) {
			return ((MethodSensorData) element).getMethodIdent();
		} else {
			return 0;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getKeys(IIndexQuery query) {
		if (0 == query.getMethodIdent()) {
			return new Object[0];
		}
		Object[] keys = new Object[1];
		keys[0] = query.getMethodIdent();
		return keys;
	}

}
