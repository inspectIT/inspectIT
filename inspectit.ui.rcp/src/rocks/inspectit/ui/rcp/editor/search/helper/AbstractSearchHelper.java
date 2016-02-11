package info.novatec.inspectit.rcp.editor.search.helper;

import info.novatec.inspectit.rcp.editor.search.ISearchExecutor;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchCriteria;
import info.novatec.inspectit.rcp.editor.search.criteria.SearchResult;
import info.novatec.inspectit.rcp.editor.search.factory.SearchFactory;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.SelectionAdapter;

/**
 * Abstract search helper. Joins the search logics and delegates specific actions to the
 * sub-classes.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractSearchHelper implements ISearchExecutor {

	/**
	 * Current occurrence.
	 */
	private int currentOccurrence;

	/**
	 * Total occurrences.
	 */
	private int totalOccurrences;

	/**
	 * Last used {@link SearchCriteria}.
	 */
	private SearchCriteria lastSearchCriteria;

	/**
	 * All objects that should be searched.
	 */
	private Object[] allObjects;

	/**
	 * List of found objects by {@link #lastSearchCriteria}.
	 */
	private List<Object> foundObjects = Collections.emptyList();

	/**
	 * {@link RepositoryDefinition}. Needed for {@link SearchFactory}.
	 */
	private final RepositoryDefinition repositoryDefinition;

	/**
	 * Caching of the viewer's input hash. When the hash changes, we need to reload all objects to
	 * search.
	 */
	private int oldInputHash;

	/**
	 * The selection adapter that will clear the search on the new sorting of columns.
	 */
	private SelectionAdapter columnSortingListener = new SelectionAdapter() {
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
			clearSearch();
		};
	};

	/**
	 * If <code>true</code> singles that the {@link #clearSearch()} was executed and search should
	 * start over.
	 */
	private boolean cleared;

	/**
	 * @param repositoryDefinition
	 *            {@link RepositoryDefinition}. Needed for {@link SearchFactory}.
	 */
	public AbstractSearchHelper(RepositoryDefinition repositoryDefinition) {
		super();
		this.repositoryDefinition = repositoryDefinition;
	}

	/**
	 * Performs the selection of the element. The sub-classes need to implement this method, so that
	 * the correct selection by viewer type is performed.
	 * 
	 * @param element
	 *            Element to select.
	 */
	public abstract void selectElement(Object element);

	/**
	 * Returns all objects that should be searched.
	 * 
	 * @return Returns all objects that should be searched.
	 */
	public abstract Object[] getAllObjects();

	/**
	 * Returns the viewer the search is performed on. This is necessary for input change checking
	 * and filtering.
	 * 
	 * @return Returns the viewer the search is performed on. This is necessary for input change
	 *         checking and filtering.
	 */
	public abstract StructuredViewer getViewer();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult executeSearch(SearchCriteria searchCriteria) {
		if (!cleared && ObjectUtils.equals(lastSearchCriteria, searchCriteria)) {
			// we search with same criteria as last time
			// just execute next
			return this.next();
		} else {
			// we search with new criteria
			if (!checkInput()) {
				loadAllObjects();
			}
			updateFoundObjects(searchCriteria);
			if (totalOccurrences > 0) {
				currentOccurrence = 1;
				displayOccurence(currentOccurrence);
			} else {
				currentOccurrence = 0;
			}
		}

		lastSearchCriteria = searchCriteria;
		cleared = false;
		return getSearchResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult next() {
		if (cleared) {
			return executeSearch(lastSearchCriteria);
		}

		if (!checkInput()) {
			loadAllObjects();
			updateFoundObjects(lastSearchCriteria);
		} else {
			sortAsInViewer(foundObjects);
		}
		if (totalOccurrences > 1) {
			currentOccurrence++;
			if (currentOccurrence > totalOccurrences) {
				currentOccurrence = 1;
			}
			displayOccurence(currentOccurrence);
		} else {
			currentOccurrence = totalOccurrences;
			if (currentOccurrence > 0) {
				displayOccurence(currentOccurrence);
			}
		}

		return getSearchResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchResult previous() {
		if (cleared) {
			return executeSearch(lastSearchCriteria);
		}

		if (!checkInput()) {
			loadAllObjects();
			updateFoundObjects(lastSearchCriteria);
		} else {
			sortAsInViewer(foundObjects);
		}
		if (totalOccurrences > 1) {
			currentOccurrence--;
			if (currentOccurrence == 0) {
				currentOccurrence = totalOccurrences;
			}
			displayOccurence(currentOccurrence);
		} else {
			currentOccurrence = totalOccurrences;
			if (currentOccurrence > 0) {
				displayOccurence(currentOccurrence);
			}
		}

		return getSearchResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearSearch() {
		cleared = true;
	}

	/**
	 * 
	 * @return Returns the search result by examining the state of the {@link #currentOccurrence}
	 *         and {@link #totalOccurrences} values.
	 */
	private SearchResult getSearchResult() {
		if (totalOccurrences > 1) {
			return new SearchResult(currentOccurrence, totalOccurrences, true, true);
		} else {
			return new SearchResult(currentOccurrence, totalOccurrences, false, false);
		}
	}

	/**
	 * Checks if the input of the viewer changed since the last search. If so, the {@link #input}
	 * variable will be updated.
	 * 
	 * @return True if the input did not change, false otherwise.
	 */
	private boolean checkInput() {
		int inputHash = Arrays.hashCode(getAllObjects());
		if (oldInputHash != inputHash) {
			oldInputHash = inputHash;
			return false;
		} else {
			oldInputHash = inputHash;
			return true;
		}
	}

	/**
	 * Loads all objects that need to be searched.
	 */
	private void loadAllObjects() {
		allObjects = getAllObjects();
	}

	/**
	 * Updates the {@link #foundObjects} with given {@link SearchCriteria} against
	 * {@link #allObjects}. This method also checks if the filters of the viewer are satisfied.
	 * 
	 * @param searchCriteria
	 *            {@link SearchCriteria}.
	 */
	private void updateFoundObjects(SearchCriteria searchCriteria) {
		foundObjects = new ArrayList<Object>();
		for (Object object : allObjects) {
			if (SearchFactory.isSearchCompatible(object, searchCriteria, repositoryDefinition) && areFiltersPassed(object, getViewer().getFilters())) {
				foundObjects.add(object);
			}
		}
		sortAsInViewer(foundObjects);
		totalOccurrences = foundObjects.size();
	}

	/**
	 * Sorts the list of objects as they appear in the table.
	 * 
	 * @param objects
	 *            List of objects.
	 */
	private void sortAsInViewer(List<Object> objects) {
		if (null != getViewer().getComparator()) {
			Collections.sort(objects, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					return getViewer().getComparator().compare(getViewer(), o1, o2);
				}
			});
		}
	}

	/**
	 * Are all filers passed.
	 * 
	 * @param object
	 *            Object to check.
	 * @param filters
	 *            Filters.
	 * @return True if all filters are passed or no filter was given.
	 */
	private boolean areFiltersPassed(Object object, ViewerFilter[] filters) {
		if (null != filters) {
			for (ViewerFilter filter : filters) {
				if (!filter.select(getViewer(), null, object)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Displays the wanted occurrence.
	 * 
	 * @param occurrence
	 *            Occurrence to display.
	 */
	private void displayOccurence(int occurrence) {
		if (totalOccurrences >= occurrence && foundObjects.size() >= occurrence) {
			// select this element
			Object selected = foundObjects.get(occurrence - 1);
			this.selectElement(selected);
		}
	}

	/**
	 * Gets {@link #columnSortingListener}.
	 * 
	 * @return {@link #columnSortingListener}
	 */
	protected SelectionAdapter getColumnSortingListener() {
		return columnSortingListener;
	}

}
