package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * Abstract plot controller for all graphs concerning the timer data and it's sub-classes.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of template
 */
public abstract class AbstractTimerDataPlotController<E extends TimerData> extends AbstractPlotController {

	/**
	 * Colors we will use for series.
	 */
	private static final int[] SERIES_COLORS = new int[] { SWT.COLOR_RED, SWT.COLOR_BLUE, SWT.COLOR_DARK_GREEN, SWT.COLOR_DARK_YELLOW, SWT.COLOR_DARK_GRAY, SWT.COLOR_BLACK, SWT.COLOR_DARK_CYAN,
			SWT.COLOR_DARK_BLUE };

	/**
	 * The map containing the weight of the {@link XYPlot}s.
	 */
	private Map<XYPlot, Integer> weights = new HashMap<XYPlot, Integer>();

	/**
	 * Plot used to display duration of the HTTP requests.
	 */
	private XYPlot durationPlot;

	/**
	 * Plot used to display the count of the HTTP requests.
	 */
	private XYPlot countPlot;

	/**
	 * Duration series.
	 */
	private List<YIntervalSeriesImproved> durationSeries;

	/**
	 * Count series.
	 */
	private List<TimeSeries> countSeries;

	/**
	 * Returns series key for given template.
	 * 
	 * @param template
	 *            Template
	 * @return {@link Comparable} representing series key.
	 */
	protected abstract Comparable<?> getSeriesKey(E template);

	/**
	 * Returns the template list. Each object in this list will represent one series.
	 * 
	 * @return Returns the template list.
	 */
	protected abstract List<E> getTemplates();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferenceIds = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferenceIds.add(PreferenceId.LIVEMODE);
		}
		preferenceIds.add(PreferenceId.TIMELINE);
		preferenceIds.add(PreferenceId.SAMPLINGRATE);
		preferenceIds.add(PreferenceId.UPDATE);
		return preferenceIds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<XYPlot> getPlots() {
		durationPlot = initializeDurationPlot();
		countPlot = initializeCountPlot();

		weights.put(durationPlot, 2);
		weights.put(countPlot, 1);

		List<XYPlot> list = new ArrayList<XYPlot>();
		Collections.addAll(list, durationPlot, countPlot);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWeight(XYPlot subPlot) {
		return weights.get(subPlot);
	}

	/**
	 * Removes all data from the upper plot and sets the {@link TimerData} objects on the plot.
	 * 
	 * @param map
	 *            The data to set on the plot.
	 */
	protected void setDurationPlotData(Map<Object, List<E>> map) {
		for (YIntervalSeriesImproved series : durationSeries) {
			series.clear();
			for (Entry<Object, List<E>> entry : map.entrySet()) {
				if (series.getKey().equals(entry.getKey())) {
					for (E data : entry.getValue()) {
						series.add(data.getTimeStamp().getTime(), data.getAverage(), data.getMin(), data.getMax(), false);
					}
					break;
				}
			}
			series.fireSeriesChanged();
		}
	}

	/**
	 * Removes all data from the upper plot and sets the {@link TimerData} objects on the plot.
	 * 
	 * @param map
	 *            The data to set on the plot.
	 */
	protected void setCountPlotData(Map<Object, List<E>> map) {
		for (TimeSeries series : countSeries) {
			series.clear();
			series.setNotify(false);
			for (Entry<Object, List<E>> entry : map.entrySet()) {
				if (series.getKey().equals(entry.getKey())) {
					for (E data : entry.getValue()) {
						series.addOrUpdate(new Millisecond(data.getTimeStamp()), data.getCount());
					}
					break;
				}
			}
			series.setNotify(true);
			series.fireSeriesChanged();
		}
	}

	/**
	 * Initializes the duration plot.
	 * 
	 * @return An instance of {@link XYPlot}.
	 */
	private XYPlot initializeDurationPlot() {
		Set<Comparable<?>> keys = new HashSet<>();
		durationSeries = new ArrayList<YIntervalSeriesImproved>();
		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		for (E template : getTemplates()) {
			Comparable<?> seriesKey = getSeriesKey(template);
			if (keys.add(seriesKey)) {
				YIntervalSeriesImproved yIntervalSeries = new YIntervalSeriesImproved(seriesKey);
				yintervalseriescollection.addSeries(yIntervalSeries);
				durationSeries.add(yIntervalSeries);
			}
		}

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));
		renderer.setBaseShapesVisible(true);
		renderer.setAlpha(0.1f);
		Display display = Display.getDefault();
		for (int i = 0; i < durationSeries.size(); i++) {
			int color = SERIES_COLORS[i % SERIES_COLORS.length];
			RGB rgb = display.getSystemColor(color).getRGB();
			renderer.setSeriesStroke(i, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			renderer.setSeriesFillPaint(i, new Color(rgb.red, rgb.green, rgb.blue));
			renderer.setSeriesOutlineStroke(i, new BasicStroke(2.0f));
			renderer.setSeriesShape(i, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		}
		NumberAxis rangeAxis = new NumberAxis("ms");
		rangeAxis.setAutoRangeMinimumSize(100.0d);
		rangeAxis.setRangeType(RangeType.POSITIVE);
		rangeAxis.setAutoRangeIncludesZero(true);

		XYPlot subplot = new XYPlot(yintervalseriescollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT, false);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Initializes the lower plot.
	 * 
	 * @return An instance of {@link XYPlot}
	 */
	private XYPlot initializeCountPlot() {
		Set<Comparable<?>> keys = new HashSet<>();
		countSeries = new ArrayList<TimeSeries>();
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (E template : getTemplates()) {
			Comparable<?> seriesKey = getSeriesKey(template);
			if (keys.add(seriesKey)) {
				TimeSeries timeSeries = new TimeSeries(seriesKey);
				countSeries.add(timeSeries);
				dataset.addSeries(timeSeries);
			}
		}

		// ISE: No idea why we have 30 here, used same value as in other charts
		XYBarDataset ds = new XYBarDataset(dataset, 30);

		XYBarRenderer renderer = new XYBarRenderer();
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));
		renderer.setShadowVisible(false);
		renderer.setMargin(0.1d);
		Display display = Display.getDefault();
		for (int i = 0; i < countSeries.size(); i++) {
			int color = SERIES_COLORS[i % SERIES_COLORS.length];
			RGB rgb = display.getSystemColor(color).getRGB();
			renderer.setSeriesPaint(i, new Color(rgb.red, rgb.green, rgb.blue));
			renderer.setSeriesVisibleInLegend(i, Boolean.FALSE);
		}

		NumberAxis rangeAxis = new NumberAxis("Count");
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRange(true);
		rangeAxis.setRangeType(RangeType.POSITIVE);

		XYPlot subplot = new XYPlot(ds, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

		return subplot;
	}
}
