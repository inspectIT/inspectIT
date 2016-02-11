package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.MemoryInformationDataAggregator;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
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

/**
 * This class creates a {@link XYPlot} containing the {@link MemoryInformationData} informations.
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * 
 */
public class DefaultMemoryPlotController extends AbstractPlotController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.graph.memory";

	/**
	 * The template of the {@link MemoryInformationData} object.
	 */
	private MemoryInformationData memoryTemplate;

	/**
	 * The template of the {@link SystemInformationData} object.
	 */
	private SystemInformationData systemTemplate;

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
	private Map<XYPlot, Integer> weights = new HashMap<XYPlot, Integer>();

	/**
	 * The {@link YIntervalSeriesImproved} for heap memory.
	 */
	private YIntervalSeriesImproved heapMemory;

	/**
	 * the {@link YIntervalSeriesImproved} for non-heap memory.
	 */
	private YIntervalSeriesImproved nonHeapMemory;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * Old list containing some data objects which could be reused.
	 */
	private List<MemoryInformationData> oldData = Collections.emptyList();

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
	private IAggregator<MemoryInformationData> aggregator = new MemoryInformationDataAggregator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		memoryTemplate = new MemoryInformationData();
		memoryTemplate.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		memoryTemplate.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		memoryTemplate.setId(-1L);

		systemTemplate = new SystemInformationData();
		systemTemplate.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		systemTemplate.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<XYPlot> getPlots() {
		upperPlot = initializeUpperPlot();
		lowerPlot = initializeLowerPlot();

		List<XYPlot> plots = new ArrayList<XYPlot>(2);
		plots.add(upperPlot);
		plots.add(lowerPlot);
		weights.put(upperPlot, WEIGHT_UPPER_PLOT);
		weights.put(lowerPlot, WEIGHT_LOWER_PLOT);

		return plots;
	}

	/**
	 * Initializes the upper plot.
	 * 
	 * @return An instance of {@link XYPlot}
	 */
	private XYPlot initializeUpperPlot() {
		heapMemory = new YIntervalSeriesImproved("heap memory");

		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		yintervalseriescollection.addSeries(heapMemory);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		renderer.setSeriesFillPaint(0, new Color(255, 200, 200));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		final NumberAxis rangeAxis = new NumberAxis("Heap / kbytes");
		rangeAxis.setRangeType(RangeType.POSITIVE);

		SystemInformationData systemData = (SystemInformationData) dataAccessService.getLastDataObject(systemTemplate);

		// set the range of y-axis only when we have the systeminformation
		// sensor
		if (systemData != null) {
			// if the max heap size is not available set the range upper level to the double of
			// initial heap size, if this is also unavailable then set it to 728MB
			double maxHeapUpperRange;
			if (systemData.getMaxHeapMemorySize() != -1) {
				maxHeapUpperRange = systemData.getMaxHeapMemorySize() / 1024.0d;
			} else if (systemData.getInitHeapMemorySize() != -1) {
				maxHeapUpperRange = systemData.getInitHeapMemorySize() * 2 / 1024.0d;
			} else {
				maxHeapUpperRange = 728 * 1024 * 1024;
			}
			rangeAxis.setRange(0.0d, maxHeapUpperRange);
			rangeAxis.setAutoRangeMinimumSize(maxHeapUpperRange);
		}

		final XYPlot subplot = new XYPlot(yintervalseriescollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Updates the upper plot with the given input data.
	 * 
	 * @param memoryData
	 *            the input data.
	 */
	private void addUpperPlotData(List<MemoryInformationData> memoryData) {
		for (MemoryInformationData data : memoryData) {
			long usedHeapMemoryAvg = (data.getTotalUsedHeapMemorySize() / data.getCount()) / 1024;
			heapMemory.add(data.getTimeStamp().getTime(), usedHeapMemoryAvg, data.getMinUsedHeapMemorySize() / 1024.0d, data.getMaxUsedHeapMemorySize() / 1024.0d, false);
		}
		heapMemory.fireSeriesChanged();
	}

	/**
	 * Removes all data from the upper plot and sets the {@link MemoryInformationData} objects on
	 * the plot.
	 * 
	 * @param memoryData
	 *            The data to set on the plot.
	 */
	private void setUpperPlotData(List<MemoryInformationData> memoryData) {
		heapMemory.clear();
		addUpperPlotData(memoryData);
	}

	/**
	 * Initializes the lower plot.
	 * 
	 * @return An instance of {@link XYPlot}
	 */
	private XYPlot initializeLowerPlot() {
		nonHeapMemory = new YIntervalSeriesImproved("non-heap memory");

		YIntervalSeriesCollection yIntervalSeriesCollection = new YIntervalSeriesCollection();
		yIntervalSeriesCollection.addSeries(nonHeapMemory);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		final NumberAxis rangeAxis = new NumberAxis("Non-heap / kbytes");
		rangeAxis.setRangeType(RangeType.POSITIVE);

		SystemInformationData systemData = (SystemInformationData) dataAccessService.getLastDataObject(systemTemplate);
		// set the range of y-axis only when we have the systeminformation
		// sensor
		if (systemData != null) {
			// if the max non heap size is not available set the range upper level to the double of
			// initial non heap size, if this is also unavailable then set it to 128MB
			double maxNonHeapUpperRange;
			if (systemData.getMaxNonHeapMemorySize() != -1) {
				maxNonHeapUpperRange = systemData.getMaxNonHeapMemorySize() / 1024.0d;
			} else if (systemData.getInitNonHeapMemorySize() != -1) {
				maxNonHeapUpperRange = systemData.getInitNonHeapMemorySize() * 2 / 1024.0d;
			} else {
				maxNonHeapUpperRange = 128 * 1024 * 1024;
			}

			if (maxNonHeapUpperRange > 0) {
				rangeAxis.setRange(0, maxNonHeapUpperRange);
				rangeAxis.setAutoRangeMinimumSize(maxNonHeapUpperRange);
			}
		}

		final XYPlot subplot = new XYPlot(yIntervalSeriesCollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Updates the lower plot with the given input data.
	 * 
	 * @param memoryData
	 *            the input data.
	 */
	private void addLowerPlotData(List<MemoryInformationData> memoryData) {
		for (MemoryInformationData data : memoryData) {
			// TODO adjust the fractional part
			long usedNonHeapMemoryAvg = (data.getTotalUsedNonHeapMemorySize() / data.getCount()) / 1024;
			nonHeapMemory.add(data.getTimeStamp().getTime(), usedNonHeapMemoryAvg, data.getMinUsedNonHeapMemorySize() / 1024.0d, data.getMaxUsedNonHeapMemorySize() / 1024.0d, false);
		}
		nonHeapMemory.fireSeriesChanged();
	}

	/**
	 * Removes all data from the lower plot and sets the {@link MemoryInformationData} objects on
	 * the plot.
	 * 
	 * @param memoryData
	 *            The data to set on the plot.
	 */
	private void setLowerPlotData(List<MemoryInformationData> memoryData) {
		nonHeapMemory.clear();
		addLowerPlotData(memoryData);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void update(final Date from, final Date to) {
		Date dataNewestDate = new Date(0);
		if (!oldData.isEmpty()) {
			dataNewestDate = oldData.get(oldData.size() - 1).getTimeStamp();
		}
		boolean leftAppend = from.before(oldFromDate);
		// boolean rightAppend = to.after(dataNewestDate) &&
		// (to.equals(newestDate) || to.after(newestDate));
		boolean rightAppend = to.after(newestDate) || oldToDate.before(to);

		List<MemoryInformationData> adjustedMemoryInformationData = Collections.emptyList();

		if (oldData.isEmpty() || to.before(oldFromDate) || from.after(dataNewestDate)) {
			// the old data is empty or the range does not fit, thus we need
			// to access the whole range
			List<MemoryInformationData> data = (List<MemoryInformationData>) dataAccessService.getDataObjectsFromToDate(memoryTemplate, from, to);

			if (!data.isEmpty()) {
				adjustedMemoryInformationData = adjustSamplingRate(data, from, to, aggregator);

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

			List<MemoryInformationData> rightData = (List<MemoryInformationData>) dataAccessService.getDataObjectsFromToDate(memoryTemplate, rightDate, to);
			List<MemoryInformationData> leftData = (List<MemoryInformationData>) dataAccessService.getDataObjectsFromToDate(memoryTemplate, from, leftDate);

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

			adjustedMemoryInformationData = adjustSamplingRate(oldData, from, to, aggregator);
		} else if (rightAppend) {
			// just append something on the right
			Date rightDate = new Date(newestDate.getTime() + 1);

			List<MemoryInformationData> timerData = (List<MemoryInformationData>) dataAccessService.getDataObjectsFromToDate(memoryTemplate, rightDate, to);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldToDate = (Date) to.clone();
				if (newestDate.before(timerData.get(timerData.size() - 1).getTimeStamp())) {
					newestDate = new Date(timerData.get(timerData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedMemoryInformationData = adjustSamplingRate(oldData, from, to, aggregator);
		} else if (leftAppend) {
			// just append something on the left
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<MemoryInformationData> timerData = (List<MemoryInformationData>) dataAccessService.getDataObjectsFromToDate(memoryTemplate, from, leftDate);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldFromDate = (Date) from.clone();
			}

			adjustedMemoryInformationData = adjustSamplingRate(oldData, from, to, aggregator);
		} else {
			// No update is needed here because we already have all the
			// needed data
			adjustedMemoryInformationData = adjustSamplingRate(oldData, from, to, aggregator);
		}

		final List<MemoryInformationData> finalAdjustedMemoryInformationData = adjustedMemoryInformationData;

		// updating the plots in the UI thread
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setUpperPlotData(finalAdjustedMemoryInformationData);
				setLowerPlotData(finalAdjustedMemoryInformationData);
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public int getWeight(XYPlot subPlot) {
		return weights.get(subPlot);
	}

	/**
	 * {@inheritDoc}
	 */
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
	public void dispose() {
	}

}
