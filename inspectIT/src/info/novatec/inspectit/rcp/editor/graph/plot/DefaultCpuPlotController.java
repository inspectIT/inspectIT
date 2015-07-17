package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.CpuInformationDataAggregator;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.text.DateFormat;
import java.text.DecimalFormat;
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
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.Range;
import org.jfree.data.RangeType;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * This class creates a {@link XYPlot} containing the {@link CpuInformationData} informations.
 * 
 * @author Eduard Tudenhoefner
 * @author Patrice Bouillet
 * 
 */
public class DefaultCpuPlotController extends AbstractPlotController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.graph.cpu";

	/**
	 * The template of the {@link CpuInformationData} object.
	 */
	private CpuInformationData template;

	/**
	 * Indicates the weight of the upper {@link XYPlot}.
	 */
	private static final int WEIGHT_UPPER_PLOT = 2;

	/**
	 * The upper {@link XYPlot} containing the graphical view.
	 */
	private XYPlot upperPlot;

	/**
	 * The map containing the weights of the {@link XYPlot}s.
	 */
	private Map<XYPlot, Integer> weights = new HashMap<XYPlot, Integer>();

	/**
	 * The {@link YIntervalSeriesImproved}.
	 */
	private YIntervalSeriesImproved cpuUsage;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * Old list containing some data objects which could be reused.
	 */
	private List<CpuInformationData> oldData = Collections.emptyList();

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
	private IAggregator<CpuInformationData> aggregator = new CpuInformationDataAggregator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new CpuInformationData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setId(-1L);

		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<XYPlot> getPlots() {
		upperPlot = initializeUpperPlot();

		List<XYPlot> plots = new ArrayList<XYPlot>(1);
		plots.add(upperPlot);
		weights.put(upperPlot, WEIGHT_UPPER_PLOT);

		return plots;
	}

	/**
	 * Initializes the upper plot.
	 * 
	 * @return An instance of {@link XYPlot}.
	 */
	private XYPlot initializeUpperPlot() {
		cpuUsage = new YIntervalSeriesImproved("cpu usage");

		YIntervalSeriesCollection yintervalseriescollection = new YIntervalSeriesCollection();
		yintervalseriescollection.addSeries(cpuUsage);

		DeviationRenderer renderer = new DeviationRenderer(true, false);
		renderer.setBaseShapesVisible(true);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		renderer.setSeriesFillPaint(0, new Color(255, 200, 200));
		renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-2.5, -2.5, 5.0, 5.0));
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, DateFormat.getDateTimeInstance(), NumberFormat.getNumberInstance()));

		final NumberAxis rangeAxis = new NumberAxis("CPU usage of the VM");
		rangeAxis.setRange(new Range(0, 100), true, false);
		rangeAxis.setAutoRangeMinimumSize(100.0d, false);
		rangeAxis.setTickUnit(new NumberTickUnit(10.0d, new DecimalFormat("0")));
		rangeAxis.setRangeType(RangeType.POSITIVE);

		final XYPlot subplot = new XYPlot(yintervalseriescollection, null, rangeAxis, renderer);
		subplot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		subplot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		subplot.setRangeCrosshairVisible(true);

		return subplot;
	}

	/**
	 * Updates the upper plot with the given input data.
	 * 
	 * @param cpuData
	 *            The input data.
	 */
	private void addUpperPlotData(List<CpuInformationData> cpuData) {
		for (CpuInformationData data : cpuData) {
			float cpuAverage = data.getTotalCpuUsage() / data.getCount();
			cpuUsage.add(data.getTimeStamp().getTime(), cpuAverage, data.getMinCpuUsage(), data.getMaxCpuUsage(), false);
		}
		cpuUsage.fireSeriesChanged();
	}

	/**
	 * Removes all data from the upper plot and sets the {@link CpuInformationData} objects on the
	 * plot.
	 * 
	 * @param cpuInformationData
	 *            The data to set on the plot.
	 */
	private void setUpperPlotData(List<CpuInformationData> cpuInformationData) {
		cpuUsage.clear();
		addUpperPlotData(cpuInformationData);
	}

	/**
	 * {@inheritDoc}
	 */
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

		List<CpuInformationData> adjustedCpuData = Collections.emptyList();

		if (oldData.isEmpty() || to.before(oldFromDate) || from.after(dataNewestDate)) {
			// the old data is empty or the range does not fit, thus we need
			// to access the whole range
			List<CpuInformationData> data = (List<CpuInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, to);

			if (!data.isEmpty()) {
				adjustedCpuData = adjustSamplingRate(data, from, to, aggregator);

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

			List<CpuInformationData> rightData = (List<CpuInformationData>) dataAccessService.getDataObjectsFromToDate(template, rightDate, to);
			List<CpuInformationData> leftData = (List<CpuInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, leftDate);

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

			adjustedCpuData = adjustSamplingRate(oldData, from, to, aggregator);
		} else if (rightAppend) {
			// just append something on the right
			Date rightDate = new Date(newestDate.getTime() + 1);

			List<CpuInformationData> timerData = (List<CpuInformationData>) dataAccessService.getDataObjectsFromToDate(template, rightDate, to);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldToDate = (Date) to.clone();
				if (newestDate.before(timerData.get(timerData.size() - 1).getTimeStamp())) {
					newestDate = new Date(timerData.get(timerData.size() - 1).getTimeStamp().getTime());
				}
			}

			adjustedCpuData = adjustSamplingRate(oldData, from, to, aggregator);
		} else if (leftAppend) {
			// just append something on the left
			Date leftDate = new Date(oldFromDate.getTime() - 1);

			List<CpuInformationData> timerData = (List<CpuInformationData>) dataAccessService.getDataObjectsFromToDate(template, from, leftDate);

			if (!timerData.isEmpty()) {
				oldData.addAll(timerData);
				oldFromDate = (Date) from.clone();
			}

			adjustedCpuData = adjustSamplingRate(oldData, from, to, aggregator);
		} else {
			// No update is needed here because we already have all the
			// needed data
			adjustedCpuData = adjustSamplingRate(oldData, from, to, aggregator);
		}

		final List<CpuInformationData> finalAdjustedCpuData = adjustedCpuData;

		// updating the plots in the UI thread
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setUpperPlotData(finalAdjustedCpuData);
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
	public void dispose() {
	}

}
