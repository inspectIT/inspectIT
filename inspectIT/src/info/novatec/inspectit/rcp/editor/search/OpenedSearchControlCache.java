package info.novatec.inspectit.rcp.editor.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches the information about the {@link ISearchExecutor}s that already have {@link SearchControl}
 * opened.
 * 
 * @author Ivan Senic
 * 
 */
public final class OpenedSearchControlCache {

	/**
	 * Private constructor.
	 */
	private OpenedSearchControlCache() {
	}

	/**
	 * Map for caching.
	 */
	private static Map<ISearchExecutor, SearchControl> openedSearchControlMap = new ConcurrentHashMap<ISearchExecutor, SearchControl>();

	/**
	 * Returns if the {@link ISearchExecutor} already has a {@link SearchControl} registered.
	 * 
	 * @param searchExecutor
	 *            {@link ISearchExecutor} to check.
	 * @return True if the {@link SearchControl} is already registered for a {@link ISearchExecutor}
	 *         .
	 */
	public static boolean hasSearchControlOpened(ISearchExecutor searchExecutor) {
		return openedSearchControlMap.containsKey(searchExecutor);
	}

	/**
	 * Registers the search control with search executor.
	 * 
	 * @param searchExecutor
	 *            {@link ISearchExecutor}
	 * @param searchControl
	 *            {@link SearchControl}
	 */
	public static void register(ISearchExecutor searchExecutor, SearchControl searchControl) {
		if (!hasSearchControlOpened(searchExecutor)) {
			openedSearchControlMap.put(searchExecutor, searchControl);
		}
	}

	/**
	 * Registers the search executor.
	 * 
	 * @param searchExecutor
	 *            {@link ISearchExecutor}
	 */
	public static void unregister(ISearchExecutor searchExecutor) {
		openedSearchControlMap.remove(searchExecutor);
	}

	/**
	 * Returns the search control.
	 * 
	 * @param searchExecutor
	 *            Search executor control is bounded to.
	 * @return {@link SearchControl}.
	 */
	public static SearchControl getSearchControl(ISearchExecutor searchExecutor) {
		return openedSearchControlMap.get(searchExecutor);
	}
}
