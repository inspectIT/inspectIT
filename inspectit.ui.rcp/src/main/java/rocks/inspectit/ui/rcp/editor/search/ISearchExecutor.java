package rocks.inspectit.ui.rcp.editor.search;

import rocks.inspectit.ui.rcp.editor.search.criteria.SearchCriteria;
import rocks.inspectit.ui.rcp.editor.search.criteria.SearchResult;

/**
 * Interface for components that can execute the search.
 *
 * @author Ivan Senic
 *
 */
public interface ISearchExecutor {

	/**
	 * Executes the search.
	 *
	 * @param searchCriteria
	 *            {@link SearchCriteria}
	 * @return {@link SearchResult} after the action.
	 */
	SearchResult executeSearch(SearchCriteria searchCriteria);

	/**
	 * Executes show next element.
	 *
	 * @return {@link SearchResult} after the action.
	 */
	SearchResult next();

	/**
	 * Executes show next element.
	 *
	 * @return {@link SearchResult} after the action.
	 */
	SearchResult previous();

	/**
	 * Signals that all search related changes should be cleared.
	 */
	void clearSearch();
}
