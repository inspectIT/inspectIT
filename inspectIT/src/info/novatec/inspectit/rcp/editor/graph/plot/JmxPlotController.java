package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.JmxSensorValueDataAggregator;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.AbstractPlotDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.PlotDataSolver;
import info.novatec.inspectit.rcp.editor.graph.plot.datasolver.impl.PlotDataSolverFactory;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.widgets.Display;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * This input controller displays the JMX sensor data in a chart.
 * 
 * @author Marius Oehler
 * 
 */
public class JmxPlotController extends AbstractPlotController {

	/**
	 * The {@link YIntervalSeriesImproved}.
	 */
	private YIntervalSeriesImproved jmxChart;

	/**
	 * The template of the {@link JmxSensorValueData} object.
	 */
	private JmxSensorValueData template;

	/**
	 * The old from date.
	 */
	private Date loadedFromDate = new Date(Long.MAX_VALUE);

	/**
	 * The old to date.
	 */
	private Date loadedToDate = new Date(0);

	/**
	 * The date where the current plotted chart starts.
	 */
	private Date plottedFrom;

	/**
	 * The date where the current plotted chart ends.
	 */
	private Date plottedTo;

	/**
	 * This list contains the loaded JmxSensorValueData. It is like a cache of the
	 * {@link #update(Date, Date)} method.
	 */
	private List<JmxSensorValueData> loadedData;

	/**
	 * {@link CachedDataService}.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * The {@link AbstractPlotDataSolver} that is used to plot the current chart.
	 */
	private AbstractPlotDataSolver plotDataSolver;

	/**
	 * The plot itself.
	 */
	private XYPlot subplot;

	/**
	 * The current {@link JmxDefinitionDataIdent} of this plot.
	 */
	private JmxDefinitionDataIdent currentJmxIdent;

	/**
	 * The global data access service.
	 */
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Used aggregator.
	 */
	private IAggregator<JmxSensorValueData> aggregator = new JmxSensorValueDataAggregator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new JmxSensorValueData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setJmxSensorDefinitionDataIdentId(inputDefinition.getIdDefinition().getJmxDefinitionId());

		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
		globalDataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();

		currentJmxIdent = cachedDataService.getJmxDefinitionDataIdentForId(inputDefinition.getIdDefinition().getJmxDefinitionId());

		plotDataSolver = getCurrentDataSolver();
	}

	/**
	 * Returns the selected {@link AbstractPlotDataSolver} of this view.
	 * 
	 * @return the {@link AbstractPlotDataSolver}
	 */
	private AbstractPlotDataSolver getCurrentDataSolver() {
		Map<String, String> dataSolverMap = PreferencesUtils.getObject(PreferencesConstants.JMX_PLOT_DATA_SOLVER);
		if (dataSolverMap.containsKey(currentJmxIdent.getDerivedFullName())) {
			String solver = dataSolverMap.get(currentJmxIdent.getDerivedFullName());
			return PlotDataSolverFactory.getDataSolver(PlotDataSolver.valueOf(solver));
		} else {
			return PlotDataSolverFactory.getDefaultDataSolver();
		}
	}

	/**
	 * Initializes the upper plot.
	 * 
	 * @return An instance of {@link XYPlot}.
	 */
	private XYPlot initializePlot() {
		jmxChart = new YIntervalSeriesImproved("jmx value");

		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		yintervalseriescollection.addSeries(jmxChart);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		renderer.setSeriesFillPaint(0, new Color(255, 200, 200));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		NumberAxis rangeAxis = plotDataSolver.getAxis();

		subplot = new XYPlot(yintervalseriescollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<XYPlot> getPlots() {
		List<XYPlot> plots = new ArrayList<>();
		plots.add(initializePlot());
		return plots;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWeight(XYPlot subPlot) {
		return 2;
	}

	/**
	 * Loads the {@link JmxSensorValueData} objects which were created between the given
	 * {@link Date} objects.
	 * 
	 * @param from
	 *            the start date
	 * @param to
	 *            the end date
	 * @return list of {@link JmxSensorValueData} objects
	 */
	@SuppressWarnings("unchecked")
	private List<JmxSensorValueData> loadJmxData(Date from, Date to) {
		return (List<JmxSensorValueData>) globalDataAccessService.getDataObjectsFromToDate(template, from, to);
	}

	/**
	 * Returns the date of the latest element in the {@link #loadedData} list.
	 * 
	 * @return Date of the latest loaded {@link JmxSensorValueData}
	 */
	private Date getLatestLoadedElementDate() {
		if (CollectionUtils.isEmpty(loadedData)) {
			return null;
		}
		long max = Long.MIN_VALUE;
		for (JmxSensorValueData data : loadedData) {
			if (data.getTimeStamp().getTime() > max) {
				max = data.getTimeStamp().getTime();
			}
		}
		return new Date(max);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(Date from, Date toUnchecked) {
		// Prevent loading into the future
		Date to = new Date(Math.min(System.currentTimeMillis(), toUnchecked.getTime()));

		if (loadedData == null || loadedData.isEmpty()) {
			loadedData = loadJmxData(from, to);
			loadedFromDate = from;
			loadedToDate = getLatestLoadedElementDate();
		} else {
			// Only load the date which was not loaded before
			boolean leftAppend = from.before(loadedFromDate);
			boolean rightAppend = to.after(loadedToDate);

			if (leftAppend && rightAppend) {
				loadedData.addAll(loadJmxData(from, loadedFromDate));
				loadedData.addAll(loadJmxData(new Date(loadedToDate.getTime() + 1), to));

				loadedFromDate = from;
				loadedToDate = getLatestLoadedElementDate();
			} else if (leftAppend) {

				if (to.before(loadedFromDate)) {
					// Clear all loaded data to prevent a fragmentary loaded data list
					loadedData.clear();

					loadedData.addAll(loadJmxData(from, to));
				} else {
					loadedData.addAll(loadJmxData(from, loadedFromDate));
				}

				loadedFromDate = from;
			} else if (rightAppend) {

				if (from.after(loadedToDate)) {
					// Clear all loaded data to prevent a fragmentary loaded data list
					loadedData.clear();
					loadedFromDate = from;

					loadedData.addAll(loadJmxData(from, to));
				} else {
					loadedData.addAll(loadJmxData(new Date(loadedToDate.getTime() + 1), to));
				}

				loadedToDate = getLatestLoadedElementDate();
			}
		}

		plottedFrom = from;
		plottedTo = to;

		final List<JmxSensorValueData> chartingDataList = selectChartingData(from, to);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				jmxChart.clear();

				for (JmxSensorValueData jmxData : chartingDataList) {
					double avg = plotDataSolver.valueConvert(jmxData.getAverageValue());
					double min = plotDataSolver.valueConvert(jmxData.getMinValue());
					double max = plotDataSolver.valueConvert(jmxData.getMaxValue());
					jmxChart.add(jmxData.getTimeStamp().getTime(), avg, min, max);
				}

				jmxChart.fireSeriesChanged();

				if (chartingDataList.isEmpty()) {
					getRootEditor().setDataInput(Collections.<DefaultData> emptyList());
				} else {
					getRootEditor().setDataInput(chartingDataList);
				}
			}
		});
	}

	/**
	 * Returns a list containing {@link JmxSensorValueData} objects which were created between the
	 * {@code from} and {@code to} date.
	 * 
	 * @param from
	 *            Objects have to be created after this date
	 * @param to
	 *            Objects have to be created before this date
	 * @return List containing {@link JmxSensorValueData} objects
	 */
	private List<JmxSensorValueData> selectChartingData(Date from, Date to) {
		List<JmxSensorValueData> resultList = new ArrayList<JmxSensorValueData>();

		Date fromShifted = new Date(from.getTime() - 1);
		Date toShifted = new Date(to.getTime() + 1);

		for (JmxSensorValueData jmxData : loadedData) {
			if (jmxData.getTimeStamp().after(fromShifted) && jmxData.getTimeStamp().before(toShifted)) {
				resultList.add(jmxData);
			}
		}

		return adjustSamplingRate(resultList, fromShifted, toShifted, aggregator);
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
		preferenceList.add(PreferenceId.UPDATE);
		preferenceList.add(PreferenceId.TIMELINE);
		preferenceList.add(PreferenceId.SAMPLINGRATE);
		preferenceList.add(PreferenceId.JMX_PLOTDATASOLVER);
		return preferenceList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		super.preferenceEventFired(preferenceEvent);

		switch (preferenceEvent.getPreferenceId()) {
		case JMX_PLOTDATASOLVER:
			processJmxPlotDataSolverEvent(preferenceEvent);
			break;
		default:
			break;
		}
	}

	/**
	 * The logic which handles the {@link PreferenceId#JMX_PLOTDATASOLVER} events.
	 * 
	 * @param preferenceEvent
	 *            the event object
	 */
	private void processJmxPlotDataSolverEvent(PreferenceEvent preferenceEvent) {
		PlotDataSolver eDataSolver = (PlotDataSolver) preferenceEvent.getPreferenceMap().get(PreferenceId.JmxPlotDataSolver.DATA_SOLVER);
		// Update view
		plotDataSolver = PlotDataSolverFactory.getDataSolver(eDataSolver);
		subplot.setRangeAxis(plotDataSolver.getAxis());

		// Update preferenceStore
		Map<String, String> dataSolverMap = PreferencesUtils.getObject(PreferencesConstants.JMX_PLOT_DATA_SOLVER);
		dataSolverMap.put(currentJmxIdent.getDerivedFullName(), eDataSolver.toString());
		PreferencesUtils.saveObject(PreferencesConstants.JMX_PLOT_DATA_SOLVER, dataSolverMap, false);
	}

	/**
	 * Returns the {@link Date} where the plot starts.
	 * 
	 * @return the {@link #plottedFrom} variable
	 */
	public Date getPlottedFrom() {
		return plottedFrom;
	}

	/**
	 * Returns the {@link Date} where the plot ends.
	 * 
	 * @return the {@link #plottedTo} variable
	 */
	public Date getPlottedTo() {
		return plottedTo;
	}
}
