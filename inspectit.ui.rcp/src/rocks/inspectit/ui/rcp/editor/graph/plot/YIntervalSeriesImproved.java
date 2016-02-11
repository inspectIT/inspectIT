package info.novatec.inspectit.rcp.editor.graph.plot;

import org.jfree.data.xy.YIntervalDataItem;
import org.jfree.data.xy.YIntervalSeries;

/**
 * @author Patrice Bouillet
 * 
 */
public class YIntervalSeriesImproved extends YIntervalSeries {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 4341007484583713423L;

	/**
	 * Creates a new empty series. By default, items added to the series will be sorted into
	 * ascending order by x-value, and duplicate x-values will be allowed (these defaults can be
	 * modified with another constructor.
	 * 
	 * @param key
	 *            the series key (<code>null</code> not permitted).
	 */
	public YIntervalSeriesImproved(Comparable<?> key) {
		this(key, true, true);
	}

	/**
	 * Constructs a new xy-series that contains no data. You can specify whether or not duplicate
	 * x-values are allowed for the series.
	 * 
	 * @param key
	 *            the series key (<code>null</code> not permitted).
	 * @param autoSort
	 *            a flag that controls whether or not the items in the series are sorted.
	 * @param allowDuplicateXValues
	 *            a flag that controls whether duplicate x-values are allowed.
	 */
	public YIntervalSeriesImproved(Comparable<?> key, boolean autoSort, boolean allowDuplicateXValues) {
		super(key, autoSort, allowDuplicateXValues);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param x
	 *            the x-value.
	 * @param y
	 *            the y-value.
	 * @param yLow
	 *            the lower bound of the y-interval.
	 * @param yHigh
	 *            the upper bound of the y-interval.
	 * @param notify
	 *            a flag that controls whether or not a
	 *            {@link org.jfree.data.general.SeriesChangeEvent} is sent to all registered
	 *            listeners.
	 */
	public void add(double x, double y, double yLow, double yHigh, boolean notify) {
		super.add(new YIntervalDataItem(x, y, yLow, yHigh), notify);
	}

}
