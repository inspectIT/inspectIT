package info.novatec.inspectit.indexing.buffer;

import info.novatec.inspectit.indexing.ITreeComponent;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import java.util.concurrent.ExecutorService;

/**
 * Branch indexer for the {@link IStorageTreeComponent}.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of the elements indexed.
 */
public interface IBufferTreeComponent<E> extends ITreeComponent<E, E> {

	/**
	 * Cleans the indexing tree by submitting the {@link Runnable} to the provided
	 * {@link ExecutorService}.
	 * 
	 * @param executorService
	 *            Executor service that will run the {@link Runnable}.
	 */
	void cleanWithRunnable(ExecutorService executorService);

	/**
	 * Deletes all tree child tree components that have no indexing object any more.
	 * 
	 * @return True if this tree component has no indexed objects any more (thus it is available for
	 *         deletion) or false otherwise.
	 */
	boolean clearEmptyComponents();

	/**
	 * Removes all indexing objects from this tree component. After calling this method tree
	 * component will have zero indexed elements in it.
	 */
	void clearAll();

	/**
	 * Cleans the tree component and its "children" from any weak references whose referenced
	 * objects has been garbage collected.
	 * 
	 * @return True if this tree component has no indexed objects any more (thus it is available for
	 *         deletion) or false otherwise.
	 */
	boolean clean();

	/**
	 * Returns number of elements that are indexed in this tree component.
	 * 
	 * @return Number of indexed elements.
	 */
	long getNumberOfElements();

}
