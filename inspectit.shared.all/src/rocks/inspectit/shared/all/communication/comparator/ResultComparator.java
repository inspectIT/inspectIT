package info.novatec.inspectit.communication.comparator;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.DefaultData;

import java.util.Comparator;

/**
 * Result comparator implements the {@link Comparator} interface and serves to keep track on sorting
 * type (ascending/descending) and to provide {@link CachedDataService} to the delegating
 * comparator.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type that can be sorted with this comparator.
 */
public class ResultComparator<T extends DefaultData> implements Comparator<T> {

	/**
	 * Delegating comparator.
	 */
	private IDataComparator<? super T> comparator;

	/**
	 * {@link CachedDataService}.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * True if ascending sorting is on, false if descending sorting is on.
	 */
	private boolean ascending = true;

	/**
	 * No-arg constructor.
	 */
	public ResultComparator() {
	}

	/**
	 * Constructor that sets only delegating constructor.
	 * 
	 * @param comparator
	 *            Delegating comparator.
	 */
	public ResultComparator(IDataComparator<? super T> comparator) {
		this(comparator, null, true);
	}

	/**
	 * Constructor that sets only comparator and ascending.
	 * 
	 * @param comparator
	 *            Delegating comparator.
	 * @param ascending
	 *            True if ascending sorting is on, false if descending sorting is on.
	 */
	public ResultComparator(IDataComparator<? super T> comparator, boolean ascending) {
		this(comparator, null, ascending);
	}

	/**
	 * Secondary constructor. Initializes the {@link #ascending} with <code>true</code>.
	 * 
	 * @param comparator
	 *            Delegating comparator.
	 * @param cachedDataService
	 *            {@link CachedDataService}.
	 */
	public ResultComparator(IDataComparator<? super T> comparator, ICachedDataService cachedDataService) {
		this(comparator, cachedDataService, true);
	}

	/**
	 * Construct that allows everything to be set.
	 * 
	 * @param comparator
	 *            Delegating comparator.
	 * @param cachedDataService
	 *            {@link CachedDataService}.
	 * @param ascending
	 *            True if ascending sorting is on, false if descending sorting is on.
	 * 
	 */
	public ResultComparator(IDataComparator<? super T> comparator, ICachedDataService cachedDataService, boolean ascending) {
		this.comparator = comparator;
		this.ascending = ascending;
		this.cachedDataService = cachedDataService;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(T o1, T o2) {
		int res = comparator.compare(o1, o2, cachedDataService);
		if (!ascending && res != 0) {
			return res * -1;
		} else {
			return res;
		}
	}

	/**
	 * Gets {@link #cachedDataService}.
	 * 
	 * @return {@link #cachedDataService}
	 */
	public ICachedDataService getCachedDataService() {
		return cachedDataService;
	}

	/**
	 * Sets {@link #cachedDataService}.
	 * 
	 * @param cachedDataService
	 *            New value for {@link #cachedDataService}
	 */
	public void setCachedDataService(ICachedDataService cachedDataService) {
		this.cachedDataService = cachedDataService;
	}

	/**
	 * Gets {@link #comparator}.
	 * 
	 * @return {@link #comparator}
	 */
	public IDataComparator<? super T> getComparator() {
		return comparator;
	}

	/**
	 * Sets {@link #comparator}.
	 * 
	 * @param comparator
	 *            New value for {@link #comparator}
	 */
	public void setComparator(IDataComparator<? super T> comparator) {
		this.comparator = comparator;
	}

	/**
	 * Gets {@link #ascending}.
	 * 
	 * @return {@link #ascending}
	 */
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * Sets {@link #ascending}.
	 * 
	 * @param ascending
	 *            New value for {@link #ascending}
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

}
