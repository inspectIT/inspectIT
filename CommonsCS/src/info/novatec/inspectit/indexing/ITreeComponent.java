package info.novatec.inspectit.indexing;

import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.indexing.impl.IndexingException;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Interface that defines the operations that each component in indexed tree has to implement.
 * 
 * @author Ivan Senic
 * 
 * @param <R>
 *            Type of the element returned by the component when querying.
 * @param <E>
 *            Type of the element that can be indexed by the component.
 */
public interface ITreeComponent<R, E> {

	/**
	 * Put the element in the tree component.
	 * 
	 * @param element
	 *            Element to index.
	 * @return Returns the same element that will be returned when querying for the indexed element.
	 * @throws IndexingException
	 *             Exception is thrown if the element can not be properly indexed.
	 */
	R put(E element) throws IndexingException;

	/**
	 * Get the element from tree component by passing the template object. The template object
	 * should have as large as possible information set, because then the method will be performed
	 * much faster. If passed element is null, null is returned.
	 * 
	 * @param template
	 *            Template to get.
	 * @return Found element, or null if element does not exists in the tree.
	 */
	R get(E template);

	/**
	 * Get the element from tree component by passing the template object and removes it from tree
	 * component. The template object should have as large as possible information set, because then
	 * the method will be performed much faster. If passed element is null, null is returned.
	 * 
	 * @param template
	 *            Template to get and remove.
	 * @return Found element, or null if element does not exists in the tree.
	 */
	R getAndRemove(E template);

	/**
	 * Returns the list of elements that satisfies the query. The query object should define as
	 * large as possible information set, because then the search is performed faster.
	 * 
	 * @param query
	 *            Query.
	 * @return List of elements, or empty list if nothing is found.
	 */
	List<R> query(IIndexQuery query);
	
	/**
	 * Returns the list of elements that satisfies the query. The query object should define as
	 * large as possible information set, because then the search is performed faster. Uses Join&Fork and creates a new
	 * task for each child.
	 * @param query
	 *            Query.
	 * @param forkJoinPool
	 * 			  The Pool which starts and manages the forks
	 * @return List of elements, or empty list if nothing is found.
	 */
	List<R> query(IIndexQuery query, ForkJoinPool forkJoinPool);
	
	/**
	 * Computes the size of the {@link ITreeComponent} with underlined {@link ITreeComponent} sizes
	 * also, but without referenced elements.
	 * 
	 * @param objectSizes
	 *            Instance of {@link IObjectSizes}.
	 * @return Size of tree component in bytes.
	 */
	long getComponentSize(IObjectSizes objectSizes);
	
	/**
	 * Creates a fitting task.
	 * @param query
	 * 			Query.
	 * @return
	 * 			Task.
	 */		
	RecursiveTask<List<R>> getTaskForForkJoinQuery(IIndexQuery query);

}
