package info.novatec.inspectit.rcp.editor.graph;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.AbstractSubView;
import info.novatec.inspectit.rcp.editor.ISubView;
import info.novatec.inspectit.rcp.editor.graph.plot.DateAxisZoomNotify;
import info.novatec.inspectit.rcp.editor.graph.plot.PlotController;
import info.novatec.inspectit.rcp.editor.graph.plot.ZoomListener;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.LiveMode;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.repository.StorageRepositoryDefinition;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.impl.DataTimeFrameLabelType;
import info.novatec.inspectit.util.TimeFrame;

import java.awt.Color;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.experimental.chart.swt.ChartComposite;

/**
 * This sub-view can create charts which can contain themselves some plots. The plots are defined by
 * {@link PlotController}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class GraphSubView extends AbstractSubView {

	/**
	 * The composite used to draw the items to.
	 */
	private Composite composite;

	/**
	 * The {@link JFreeChart} chart.
	 */
	private JFreeChart chart;

	/**
	 * The plot controller defines the visualized plots in the chart.
	 */
	private PlotController plotController;

	/**
	 * If we are in the auto update mode.
	 */
	private boolean autoUpdate = LiveMode.ACTIVE_DEFAULT;

	/**
	 * One minute in milliseconds.
	 */
	private static final long ONE_MINUTE = 60000L;

	/**
	 * Ten minutes in milliseconds.
	 */
	private static final long TEN_MINUTES = ONE_MINUTE * 10;

	/**
	 * The zoom listener.
	 */
	private ZoomListener zoomListener;

	/**
	 * Defines if a refresh job is currently already executing.
	 */
	private volatile boolean jobInSchedule = false;

	/**
	 * The constructor taking one parameter and creating a {@link PlotController}.
	 * 
	 * @param fqn
	 *            The fully-qualified-name of the corresponding sensor type.
	 */
	public GraphSubView(String fqn) {
		this.plotController = PlotFactory.createDefaultPlotController(fqn);
	}

	/**
	 * The constructor taking one parameter and creating a {@link PlotController}.
	 * 
	 * @param sensorTypeEnum
	 *            The sensor type enumeration of the corresponding sensor type.
	 */
	public GraphSubView(SensorTypeEnum sensorTypeEnum) {
		this.plotController = PlotFactory.createDefaultPlotController(sensorTypeEnum);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() {
		plotController.setInputDefinition(getRootEditor().getInputDefinition());
	}

	/**
	 * {@inheritDoc}
	 */
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		// set the input definition
		plotController.setRootEditor(getRootEditor());

		// create the composite
		composite = toolkit.createComposite(parent);
		composite.setLayout(new FillLayout());

		// create the chart
		chart = createChart();
		if (!plotController.showLegend()) {
			chart.removeLegend();
		}
		Color color = new Color(toolkit.getColors().getBackground().getRed(), toolkit.getColors().getBackground().getGreen(), toolkit.getColors().getBackground().getBlue());
		chart.setBackgroundPaint(color);

		new ChartComposite(composite, SWT.NONE, chart, ChartComposite.DEFAULT_WIDTH, ChartComposite.DEFAULT_HEIGHT, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true) {

			/**
			 * {@inheritDoc}
			 * <p>
			 * On print we want to set the title so it's visible in the print page.
			 */
			@Override
			public boolean print(GC gc) {
				chart.setTitle(getRootEditor().getTitle());
				boolean res = super.print(gc);
				chart.setTitle("");
				return res;
			}
		};
	}

	/**
	 * Creates and returns a {@link JFreeChart} chart.
	 * 
	 * @return The {@link JFreeChart} chart.
	 */
	private JFreeChart createChart() {
		DateAxisZoomNotify domainAxis = new DateAxisZoomNotify();
		domainAxis.setLowerMargin(0.0d);
		domainAxis.setAutoRangeMinimumSize(100000.0d);
		long now = System.currentTimeMillis();
		domainAxis.setRange(new Range(now - TEN_MINUTES, now + ONE_MINUTE), true, false);

		// set the ticks to display in the date axis
		TickUnits source = new TickUnits();
		source.add(new DateTickUnit(DateTickUnitType.MINUTE, 1, DateFormat.getTimeInstance(DateFormat.SHORT)));
		source.add(new DateTickUnit(DateTickUnitType.MINUTE, 5, DateFormat.getTimeInstance(DateFormat.SHORT)));
		source.add(new DateTickUnit(DateTickUnitType.MINUTE, 30, DateFormat.getTimeInstance(DateFormat.SHORT)));
		source.add(new DateTickUnit(DateTickUnitType.HOUR, 1, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)));
		source.add(new DateTickUnit(DateTickUnitType.HOUR, 3, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)));
		source.add(new DateTickUnit(DateTickUnitType.DAY, 1, DateFormat.getDateInstance(DateFormat.SHORT)));
		source.add(new DateTickUnit(DateTickUnitType.DAY, 7, DateFormat.getDateInstance(DateFormat.MEDIUM)));
		source.add(new DateTickUnit(DateTickUnitType.MONTH, 1, DateFormat.getDateInstance(DateFormat.MEDIUM)));
		source.add(new DateTickUnit(DateTickUnitType.YEAR, 1, DateFormat.getDateInstance(DateFormat.MEDIUM)));
		source.add(new DateTickUnit(DateTickUnitType.YEAR, 10, DateFormat.getDateInstance(DateFormat.MEDIUM)));
		domainAxis.setStandardTickUnits(source);
		domainAxis.setAutoTickUnitSelection(true);

		addZoomListener(domainAxis);

		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);
		plot.setGap(10.0);

		// add the subplots...
		List<XYPlot> subPlots = plotController.getPlots();
		for (XYPlot subPlot : subPlots) {
			plot.add(subPlot, plotController.getWeight(subPlot));
		}

		plot.setOrientation(PlotOrientation.VERTICAL);

		TimeFrame timeFrame = getInitialDataTimeFrame();
		if (null != timeFrame) {
			// set min/max dates
			((DateAxis) plot.getDomainAxis()).setMinimumDate(timeFrame.getFrom());
			((DateAxis) plot.getDomainAxis()).setMaximumDate(timeFrame.getTo());

			// inform the time-line widget via event
			PreferenceEvent preferenceEvent = new PreferenceEvent(PreferenceId.TIMELINE);
			Map<IPreferenceGroup, Object> map = new HashMap<>();
			map.put(PreferenceId.TimeLine.FROM_DATE_ID, timeFrame.getFrom());
			map.put(PreferenceId.TimeLine.TO_DATE_ID, timeFrame.getTo());
			preferenceEvent.setPreferenceMap(map);
			getRootEditor().getPreferencePanel().fireEvent(preferenceEvent);
		}

		// return a new chart containing the overlaid plot...
		return new JFreeChart(plot);
	}

	/**
	 * Adds the zoom listener to the domain axis.
	 * 
	 * @param domainAxis
	 *            The domain axis.
	 */
	private void addZoomListener(DateAxisZoomNotify domainAxis) {
		if (null == zoomListener) {
			zoomListener = new ZoomListener() {
				public void zoomOccured() {
					if (autoUpdate) {
						autoUpdate = false;
						getRootEditor().getPreferencePanel().disableLiveMode();
					}
					doRefresh();
				}
			};
		}
		domainAxis.addZoomListener(zoomListener);
	}

	/**
	 * Returns initial data time frame if one is defined.
	 * 
	 * @return Returns initial data time frame if one is defined.
	 */
	private TimeFrame getInitialDataTimeFrame() {
		RepositoryDefinition repositoryDefinition = getRootEditor().getInputDefinition().getRepositoryDefinition();
		if (repositoryDefinition instanceof StorageRepositoryDefinition) {
			StorageRepositoryDefinition storageRepositoryDefinition = (StorageRepositoryDefinition) repositoryDefinition;
			List<AbstractStorageLabel<TimeFrame>> labels = storageRepositoryDefinition.getLocalStorageData().getLabels(new DataTimeFrameLabelType());
			if (CollectionUtils.isNotEmpty(labels)) {
				return labels.get(0).getValue();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Control getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDataInput(List<? extends DefaultData> data) {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PreferenceId> getPreferenceIds() {
		return plotController.getPreferenceIds();
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.TIMELINE.equals(preferenceEvent.getPreferenceId())) {
			XYPlot plot = (XYPlot) chart.getPlot();
			DateAxis axis = (DateAxis) plot.getDomainAxis();

			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if (preferenceMap.containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				Date toDate = (Date) preferenceMap.get(PreferenceId.TimeLine.TO_DATE_ID);
				axis.setMaximumDate(toDate);
			}
			if (preferenceMap.containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				Date fromDate = (Date) preferenceMap.get(PreferenceId.TimeLine.FROM_DATE_ID);
				axis.setMinimumDate(fromDate);
			}

			doRefresh();
		}

		if (PreferenceId.LIVEMODE.equals(preferenceEvent.getPreferenceId())) {
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if (preferenceMap.containsKey(PreferenceId.LiveMode.BUTTON_LIVE_ID)) {
				autoUpdate = (Boolean) preferenceMap.get(PreferenceId.LiveMode.BUTTON_LIVE_ID);
				if (autoUpdate) {
					doRefresh();
				}
			}
		}

		plotController.preferenceEventFired(preferenceEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void doRefresh() {
		if (checkDisposed()) {
			return;
		}
		if (!jobInSchedule) {
			jobInSchedule = true;

			XYPlot plot = (XYPlot) chart.getPlot();
			DateAxis axis = (DateAxis) plot.getDomainAxis();
			final Date minDate = axis.getMinimumDate();
			final Date maxDate = autoUpdate ? new Date(System.currentTimeMillis()) : axis.getMaximumDate();
			if (autoUpdate) {
				axis.setMaximumDate(maxDate);
			}

			Job job = new Job(getDataLoadingJobName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						plotController.update(minDate, maxDate);
						return Status.OK_STATUS;
					} catch (Throwable throwable) { // NOPMD
						throw new RuntimeException("Unknown exception occurred trying to refresh the view.", throwable);
					} finally {
						jobInSchedule = false;
					}
				}
			};
			job.schedule();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ISubView getSubViewWithInputController(Class<?> inputControllerClass) {
		if (Objects.equals(inputControllerClass, plotController.getClass())) {
			return this;
		}
		return null;
	}

	/**
	 * Returns true if the composite holding the chart in the sub-view is disposed. False otherwise.
	 * 
	 * @return Returns true if the composite holding the chart in the sub-view is disposed. False
	 *         otherwise.
	 */
	private boolean checkDisposed() {
		return composite.isDisposed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		plotController.dispose();
	}
}
