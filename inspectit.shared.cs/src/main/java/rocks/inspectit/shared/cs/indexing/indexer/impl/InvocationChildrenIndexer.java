package rocks.inspectit.shared.cs.indexing.indexer.impl;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.indexer.AbstractSharedInstanceBranchIndexer;
import rocks.inspectit.shared.cs.indexing.indexer.IBranchIndexer;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;

/**
 * Special indexer, that indexes invocations without children in one branch and every other object,
 * including invocations with children, in second one.
 *
 * @author Ivan Senic
 *
 * @param <E>
 */
public class InvocationChildrenIndexer<E extends DefaultData> extends AbstractSharedInstanceBranchIndexer<E> implements IBranchIndexer<E> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getKey(E element) {
		if (element instanceof InvocationSequenceData) {
			InvocationSequenceData invoc = (InvocationSequenceData) element;
			if ((invoc.getNestedSequences() == null) || invoc.getNestedSequences().isEmpty()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getKeys(IIndexQuery query) {
		Object[] keys = new Object[1];
		if (query instanceof StorageIndexQuery) {
			if (((StorageIndexQuery) query).isOnlyInvocationsWithoutChildren()) {
				keys[0] = Boolean.TRUE;
			} else {
				keys[0] = Boolean.FALSE;
			}
		} else {
			keys[0] = Boolean.FALSE;
		}

		return keys;
	}

}
