package info.novatec.inspectit.indexing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.RecursiveTask;
/**
 * 
 * @author TAN
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
	private Collection<ITreeComponent<R, E>> branchesToQuery;
	/**
	 * Default constructor. The current branch and the query is needed.
	 * 
	 * @param currentRoot the current Branch
	 * @param query the given query
	 */
	public QueryTask(AbstractBranch<R, E> currentRoot, IIndexQuery query) {
		this.query = query;
		branchesToQuery = currentRoot.getBranchesToQuery(query);
	}

	/**
	 * Forks and queries all subbranches.
	 * {@inheritDoc}
	 */
	protected List<R> compute() {
		// List of Forks which will be started
		List<RecursiveTask<List<R>>> forks = new ArrayList<RecursiveTask<List<R>>>();
		List<R> resultList = new ArrayList<R>();
		for (ITreeComponent<R, E> component : branchesToQuery) {
			// If Leaf, a LeafFork will be started
			// Else: Another QueryTask will be started
			if (component instanceof AbstractBranch) {
				QueryTask<R, E> queryTask = new QueryTask<R, E>((AbstractBranch<R, E>) component, query);
				forks.add(queryTask);
				queryTask.fork();
			} else {
				LeafTask<R, E> leafTask = new LeafTask<R, E>(component, query);
				forks.add(leafTask);
				leafTask.fork();
			}

		}
		for (RecursiveTask<List<R>> fork : forks) {
			resultList.addAll(fork.join());
		}
		return resultList;
	}
}
