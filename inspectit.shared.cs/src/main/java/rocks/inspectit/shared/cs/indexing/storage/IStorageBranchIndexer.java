package rocks.inspectit.shared.cs.indexing.storage;

import rocks.inspectit.shared.cs.indexing.ITreeComponent;
import rocks.inspectit.shared.cs.indexing.buffer.impl.Branch;
import rocks.inspectit.shared.cs.indexing.buffer.impl.Leaf;
import rocks.inspectit.shared.cs.indexing.indexer.IBranchIndexer;

/**
 * Indexer for the {@link IStorageTreeComponent}.
 *
 * @author Ivan Senic
 *
 * @param <E>
 *            Type of the elements to be indexed.
 */
public interface IStorageBranchIndexer<E> extends IBranchIndexer<E> {

	/**
	 * Returns the correct {@link ITreeComponent} for the next level. If the
	 * {@link #getChildIndexer()} returned object is not null the tree will create new
	 * {@link Branch} (or its subclass), otherwise new {@link Leaf}.
	 *
	 * @param object
	 *            Object that the next component has to be build for.
	 * @return Next tree component.
	 */
	IStorageTreeComponent<E> getNextTreeComponent(E object);

	/**
	 * Sets the id of the IStorageBranchIndexer.
	 *
	 * @param id
	 *            Id.
	 */
	void setId(int id);

}
