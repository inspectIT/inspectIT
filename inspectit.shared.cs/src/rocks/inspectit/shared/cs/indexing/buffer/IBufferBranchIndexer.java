package info.novatec.inspectit.indexing.buffer;

import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

/**
 * Branch indexer for the {@link IBufferTreeComponent}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of the elements indexed.
 */
public interface IBufferBranchIndexer<E> extends IBranchIndexer<E> {

	/**
	 * Returns the correct {@link IBufferTreeComponent} for the next level.
	 * 
	 * @return Next tree component.
	 */
	IBufferTreeComponent<E> getNextTreeComponent();

	/**
	 * Returns the child indexer.
	 * 
	 * @return Returns the child indexer.
	 */
	IBufferBranchIndexer<E> getChildIndexer();

}
