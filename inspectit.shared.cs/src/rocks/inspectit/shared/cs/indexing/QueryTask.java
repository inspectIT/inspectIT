package info.novatec.inspectit.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Queries through the branches and creates new tasks for each branch.
 * 
 * @author Tobias Angerstein
 *
 * @param <R>
 *            Type of the element returned by the branch when querying.
 * @param <E>
 *            Type of the element that can be indexed by the branch.
 */
public class QueryTask<R, E> extends RecursiveTask<List<R>> {

	/**
	 * The given query.
	 */
	private IIndexQuery query;

	/**
	 * The branches, which have to be queried.
	 */
	private Collection<? extends ITreeComponent<R, E>> branchesToQuery;

	/**
	 * Default constructor. The current branch and the query is needed.
	 * 
	 * @param branchesToQuery
	 *            The branches, which have to be queried.
	 * @param query
	 *            the given query
	 */
	public QueryTask(Collection<? extends ITreeComponent<R, E>> branchesToQuery, IIndexQuery query) {
		this.query = query;
		this.branchesToQuery = branchesToQuery;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Forks and queries all subbranches.
	 */
	protected List<R> compute() {
		// List of Forks which will be started
		List<RecursiveTask<List<R>>> forks = new ArrayList<RecursiveTask<List<R>>>();
		List<R> resultList = new ArrayList<R>();
		for (ITreeComponent<R, E> component : branchesToQuery) {
			// New fork is being started
			RecursiveTask<List<R>> task = component.getTaskForForkJoinQuery(query);
			forks.add(task);
			task.fork();
		}
		for (RecursiveTask<List<R>> fork : forks) {
			resultList.addAll(fork.join());
		}
		return resultList;
	}
}
