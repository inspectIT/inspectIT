package rocks.inspectit.ui.rcp.editor.graph.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.ThreadInformationDataAggregator;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * This class creates a {@link XYPlot} containing the {@link ThreadInformationData} informations.
 *
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 *
 */
public class DefaultThreadsPlotController extends AbstractPlotController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.graph.threads";

	/**
	 * The template of the {@link ThreadInformationData} object.
	 */
	private ThreadInformationData template;

	/**
	 * Indicates the weight of the upper {@link XYPlot}.
	 */
	private static final int WEIGHT_UPPER_PLOT = 1;

	/**
	 * Indicates the weight of the lower {@link XYPlot}.
	 */
	private static final int WEIGHT_LOWER_PLOT = 1;

	/**
	 * The upper {@link XYPlot}.
	 */
	private XYPlot upperPlot;

	/**
	 * The lower {@link XYPlot}.
	 */
	private XYPlot lowerPlot;

	/**
	 * The map containing the weight of the {@link XYPlot}s.
	 */
	private Map<XYPlot, Integer> weights = new HashMap<>();

	/**
	 * The {@link YIntervalSeriesImproved} for live threads.
	 */
	private YIntervalSeriesImproved liveThreads;

	/**
	 * The {@link YIntervalSeriesImproved} for peak threads.
	 */
	private YIntervalSeriesImproved peakThreads;

	/**
	 * The {@link YIntervalSeriesImproved} for daemon threads.
	 */
	private YIntervalSeriesImproved daemonThreads;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * Old list containing some data objects which could be reused.
	 */
	private List<ThreadInformationData> oldData = Collections.emptyList();

	/**
	 * The old from date.
	 */
	private Date oldFromDate = new Date(Long.MAX_VALUE);

	/**
	 * The old to date.
	 */
	private Date oldToDate = new Date(0);

	/**
	 * This represents the date of one of the objects which was received at some time in the past
	 * but was the one with the newest date. This is needed for not requesting some data of the CMR
	 * sometimes.
	 */
	private Date newestDate = new Date(0);

	/**
	 * {@link IAggregator}.
	 */
	IAggregator<ThreadInformationData> aggregator = new ThreadInformationDataAggregator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new ThreadInformationData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setId(-1L);

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<XYPlot> getPlots() {
		upperPlot = initializeUpperPlot();
		lowerPlot = initializeLowerPlot();

		List<XYPlot> plots = new ArrayList<>(2);
		plots.add(upperPlot);
		plots.add(lowerPlot);
		weights.put(upperPlot, WEIGHT_UPPER_PLOT);
		weights.put(lowerPlot, WEIGHT_LOWER_PLOT);

		return plots;
	}

	/**
	 * Initializes the upper plot with the given input data.
	 *
	 * @return An instance of {@link XYPlot}
	 */
	private XYPlot initializeUpperPlot() {
		liveThreads = new YIntervalSeriesImproved("live");
		peakThreads = new YIntervalSeriesImproved("peak");

		YIntervalSeriesCollection yIntervalSeriesCollection = new YIntervalSeriesCollection();
		yIntervalSeriesCollection.addSeries(liveThreads);
		yIntervalSeriesCollection.addSeries(peakThreads);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		renderer.setSeriesFillPaint(0, new Color(255, 200, 200));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		final NumberAxis rangeAxis = new NumberAxis("Threads");
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRangeMinimumSize(10.0d, false);
		rangeAxis.setRangeType(RangeType.POSITIVE);

		final XYPlot subplot = new XYPlot(yIntervalSeriesCollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Updates the upper plot with the given input data.
	 *
	 * @param threadData
	 *            the input data.
	 */
	private void addUpperPlotData(List<ThreadInformationData> threadData) {
		for (ThreadInformationData data : threadData) {
			float liveThreadAverage = ((float) data.getTotalThreadCount()) / data.getCount();
			float peakThreadAverage = ((float) data.getTotalPeakThreadCount()) / data.getCount();
			liveThreads.add(data.getTimeStamp().getTime(), liveThreadAverage, data.getMinThreadCount(), data.getMaxThreadCount(), false);
			peakThreads.add(data.getTimeStamp().getTime(), peakThreadAverage, data.getMinPeakThreadCount(), data.getMaxPeakThreadCount(), false);
		}
		liveThreads.fireSeriesChanged();
		peakThreads.fireSeriesChanged();
	}

	/**
	 * Removes all data from the upper plot and sets the {@link ThreadInformationData} objects on
	 * the plot.
	 *
	 * @param threadData
	 *            The data to set on the plot.
	 */
	private void setUpperPlotData(List<ThreadInformationData> threadData) {
		liveThreads.clear();
		peakThreads.clear();
		addUpperPlotData(threadData);
	}

	/**
	 * Initializes the lower plot with the given input data.
	 *
	 * @return An instance of {@link XYPlot}.
	 */
	private XYPlot initializeLowerPlot() {
		daemonThreads = new YIntervalSeriesImproved("daemon");

		YIntervalSeriesCollection yIntervalSeriesCollection = new YIntervalSeriesCollection();
		yIntervalSeriesCollection.addSeries(daemonThreads);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		final NumberAxis rangeAxis = new NumberAxis("Daemon threads");
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRangeMinimumSize(10.0d, false);
		rangeAxis.setRangeType(RangeType.POSITIVE);

		final XYPlot subplot = new XYPlot(yIntervalSeriesCollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Updates the lower plot with the given input data.
	 *
	 * @param threadData
	 *            the input data.
	 */
	private void addLowerPlotData(List<ThreadInformationData> threadData) {
		for (ThreadInformationData data : threadData) {
			float daemonThreadAverage = ((float) data.getTotalDaemonThreadCount()) / data.getCount();
			daemonThreads.add(data.getTimeStamp().getTime(), daemonThreadAverage, data.getMinDaemonThreadCount(), data.getMaxDaemonThreadCount(), false);
		}
		daemonThreads.fireSeriesChanged();
	}

	/**
	 * Removes all data from the lower plot and sets the {@link ThreadInformationData} objects on
	 * the plot.
	 *
	 * @param threadData
	 *            The data to set on the plot.
	 */
	private void setLowerPlotData(List<ThreadInformationData> threadData) {
		daemonThreads.clear();
		addLowerPlotData(threadData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void update(Date from, Date to) {
		Date dataNewestDate = new Date(0);
		if (!oldData.isEmpty()) {
			dataNewestDate = oldData.get(oldData.size() - 1).getTimeStamp();
		}
		boolean leftAppend = from.before(oldFromDate);
		// boolean rightAppend = to.after(dataNewestDate) &&
		// (to.equals(newestDate) || to.after(newestDate));
		boolean rightAppend = to.after(newestDate) || oldToDate.before(to);

		List<ThreadInformationData> adjustedThreadData = Collections.emptyList();

		if (oldData.isEmpty() || to.before(oldFromDate) || from.after(dataNewestDate)) {
			// the old data is empty or the range does not fit, thus we need
			// to access the whole range
			List<ThreadInformationData> data = (List<ThreadInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, to);

			if (!data.isEmpty()) {
				adjustedThreadData = adjustSamplingRate(data, from, to, aggregator);

				// we got some data, thus we can set the date
				oldFromDate = (Date) from.clone();
				oldToDate = (Date) to.clone();
				if (newestDate.before(data.get(data.size() - 1).getTimeStamp())) {
					newestDate = new Date(data.get(data.size() - 1).getTimeStamp().getTime());
				}
			}
			oldData = data;
		} else if (leftAppend && rightAppend) {
			// we have some data in between, but we need to append something
			// to the start and to the end
			Date rightDate = new Date(newestDate.getTime() + 1);
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<ThreadInformationData> rightData = (List<ThreadInformationData>) dataAccessService.getDataObjectsFromToDate(template, rightDate, to);
			List<ThreadInformationData> leftData = (List<ThreadInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, leftDate);

			if (!leftData.isEmpty()) {
				oldData.addAll(0, leftData);
				oldFromDate = (Date) from.clone();
			}

			if (!rightData.isEmpty()) {
				oldData.addAll(rightData);
				oldToDate = (Date) to.clone();
				if (newestDate.before(rightData.get(rightData.size() - 1).getTimeStamp())) {
					newestDate = new Date(rightData.get(rightData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedThreadData = adjustSamplingRate(oldData, from, to, aggregator);
		} else if (rightAppend) {
			// just append something on the right
			Date rightDate = new Date(newestDate.getTime() + 1);

			List<ThreadInformationData> timerData = (List<ThreadInformationData>) dataAccessService.getDataObjectsFromToDate(template, rightDate, to);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldToDate = (Date) to.clone();
				if (newestDate.before(timerData.get(timerData.size() - 1).getTimeStamp())) {
					newestDate = new Date(timerData.get(timerData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedThreadData = adjustSamplingRate(oldData, from, to, aggregator);
		} else if (leftAppend) {
			// just append something on the left
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<ThreadInformationData> timerData = (List<ThreadInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, leftDate);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldFromDate = (Date) from.clone();
			}

			adjustedThreadData = adjustSamplingRate(oldData, from, to, aggregator);
		} else {
			// No update is needed here because we already have all the
			// needed data
			adjustedThreadData = adjustSamplingRate(oldData, from, to, aggregator);
		}

		final List<ThreadInformationData> finalAdjustedThreadData = adjustedThreadData;

		// updating the plots in the UI thread
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setUpperPlotData(finalAdjustedThreadData);
				setLowerPlotData(finalAdjustedThreadData);
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferenceList = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferenceList.add(PreferenceId.LIVEMODE);
		}
		preferenceList.add(PreferenceId.TIMELINE);
		preferenceList.add(PreferenceId.SAMPLINGRATE);
		preferenceList.add(PreferenceId.UPDATE);
		return preferenceList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWeight(XYPlot subPlot) {
		return weights.get(subPlot);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

}
