package info.novatec.inspectit.rcp.editor.viewers;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.ResultComparator;

/**
 * Extension of {@link ResultComparator} to solve problems with raw/aggregated comparison on the UI.
 * 
 * @author Ivan Senic
 * 
 * @param <T>
 *            Type that can be sorted with this comparator.
 */
public abstract class RawAggregatedResultComparator<T extends DefaultData> extends ResultComparator<T> {

	/**
	 * If compare should be execute in raw mode.
	 */
	private boolean compareInRawMode;

	/**
	 * If compare should be executed in aggregated mode.
	 */
	private boolean compareInAggregatedMode;

	/**
	 * 
	 * @param comparator
	 *            Delegating comparator.
	 * @param cachedDataService
	 *            {@link CachedDataService}
	 * @param compareInRawMode
	 *            If compare should be execute in raw mode.
	 * @param compareInAggregatedMode
	 *            If compare should be execute in aggregated mode.
	 */
	public RawAggregatedResultComparator(IDataComparator<? super T> comparator, ICachedDataService cachedDataService, boolean compareInRawMode, boolean compareInAggregatedMode) {
		this(comparator, cachedDataService, compareInRawMode, compareInAggregatedMode, true);
	}

	/**
	 * 
	 * @param comparator
	 *            Delegating comparator.
	 * @param cachedDataService
	 *            {@link CachedDataService}
	 * @param compareInRawMode
	 *            If compare should be execute in raw mode.
	 * @param compareInAggregatedMode
	 *            If compare should be execute in aggregated mode.
	 * @param ascending
	 *            True if ascending sorting is on, false if descending sorting is on.
	 */
	public RawAggregatedResultComparator(IDataComparator<? super T> comparator, ICachedDataService cachedDataService, boolean compareInRawMode, boolean compareInAggregatedMode, boolean ascending) {
		super(comparator, cachedDataService, ascending);
		this.compareInRawMode = compareInRawMode;
		this.compareInAggregatedMode = compareInAggregatedMode;
	}

	/**
	 * Returns if the raw mode for the table where comparing should be done is on. Sub-classes
	 * should provide implementations for this.
	 * 
	 * @return Returns if the raw mode is on.
	 */
	protected abstract boolean isRawMode();

	/**
	 * {@inheritDoc}
	 * <p>
	 * Will only compare if the conditions are met.
	 */
	public int compare(T o1, T o2) {
		if ((compareInRawMode && isRawMode()) || (compareInAggregatedMode && !isRawMode())) {
			return super.compare(o1, o2);
		} else {
			return 0;
		}

	};

}
