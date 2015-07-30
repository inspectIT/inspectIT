package info.novatec.inspectit.indexing;

import java.util.List;
import java.util.concurrent.RecursiveTask;
/**
 * Returns the objects of the commited leaf.
 * @author Tobias Angerstein
 *
 * @param <R>
 *            Type of the element returned by the branch when querying.
 * @param <E>
 *            Type of the element that can be indexed by the branch.
 */
public class LeafTask<R, E> extends RecursiveTask<List<R>> {
	
	/**
	 * The given leaf.
	 */
	private ITreeComponent<R, E> leaf;
	
	/**
	 * The given query.
	 */
	private IIndexQuery query;
	
	/**
	 * Default constructor.
	 * @param treeComponent
	 * 		leaf 
	 * @param query
	 * 		query
	 */
	public LeafTask(ITreeComponent<R, E> treeComponent, IIndexQuery query) {
		this.leaf = treeComponent;
		this.query = query;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Queries the Leaf.
	 */
	protected List<R> compute() {
		return leaf.query(query);
	}
}
