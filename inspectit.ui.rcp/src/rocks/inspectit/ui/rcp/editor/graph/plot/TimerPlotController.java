package rocks.inspectit.ui.rcp.editor.graph.plot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.widgets.Display;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.IHttpTimerDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.TimerDataAggregator;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.TimerDataChartingInputDefinitionExtra;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;

/**
 * {@link PlotController} for displaying many Http requests in the graph.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerPlotController extends AbstractTimerDataPlotController<TimerData> {

	/**
	 * Templates that will be used for data display. Every template is one line in line chart.
	 */
	private List<TimerData> templates;

	/**
	 * {@link IHttpTimerDataAccessService}.
	 */
	private IGlobalDataAccessService dataAccessService;

	/**
	 * {@link CachedDataService}.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * {@link IAggregator}.
	 */
	private IAggregator<TimerData> aggregator;

	/**
	 * List of displayed data.
	 */
	List<TimerData> displayedData = Collections.emptyList();

	/**
	 * Date to display data to.
	 */
	Date toDate = new Date(0);

	/**
	 * Date to display data from.
	 */
	Date fromDate = new Date(Long.MAX_VALUE);

	/**
	 * Date that mark the last displayed data on the graph.
	 */
	Date latestDataDate = new Date(0);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.TIMER_DATA_CHARTING_EXTRAS_MARKER)) {
			TimerDataChartingInputDefinitionExtra inputDefinitionExtra = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.TIMER_DATA_CHARTING_EXTRAS_MARKER);
			templates = inputDefinitionExtra.getTemplates();
		} else {
			TimerData template = new TimerData();
			template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
			template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
			template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());
			template.setId(-1L);
			templates = Collections.singletonList(template);
		}

		aggregator = new TimerDataAggregator();
		dataAccessService = inputDefinition.getRepositoryDefinition().getGlobalDataAccessService();
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean showLegend() {
		return templates.size() > 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void update(Date from, Date to) {
		List<DefaultData> templates = new ArrayList<DefaultData>(this.templates);
		// complete load if we have no data, or wanted time range is completely outside the current
		boolean completeLoad = CollectionUtils.isEmpty(displayedData) || fromDate.after(to) || toDate.before(from);
		// left append if currently displayed from date is after the new from date
		boolean leftAppend = fromDate.after(from);
		// right append if the currently displayed to date is before new to date or the date of the
		// last data is before new date
		boolean rightAppend = toDate.before(to) || latestDataDate.before(to);

		if (completeLoad) {
			List<TimerData> timerDatas = (List<TimerData>) dataAccessService.getTemplatesDataObjectsFromToDate(templates, from, to);
			if (CollectionUtils.isNotEmpty(timerDatas)) {
				fromDate = (Date) from.clone();
				toDate = (Date) to.clone();
			}
			displayedData = timerDatas;
		} else {
			if (rightAppend) {
				Date startingFrom = new Date(latestDataDate.getTime() + 1);
				List<TimerData> timerDatas = (List<TimerData>) dataAccessService.getTemplatesDataObjectsFromToDate(templates, startingFrom, to);
				if (CollectionUtils.isNotEmpty(timerDatas)) {
					displayedData.addAll(timerDatas);
					toDate = (Date) to.clone();
				}
			}
			if (leftAppend) {
				Date endingTo = new Date(fromDate.getTime() - 1);
				List<TimerData> timerDatas = (List<TimerData>) dataAccessService.getTemplatesDataObjectsFromToDate(templates, from, endingTo);
				if (CollectionUtils.isNotEmpty(timerDatas)) {
					displayedData.addAll(0, timerDatas);
					fromDate = (Date) from.clone();
				}
			}
		}

		// update the last displayed data
		if (CollectionUtils.isNotEmpty(displayedData)) {
			latestDataDate = new Date(displayedData.get(displayedData.size() - 1).getTimeStamp().getTime());
		}

		Map<Object, List<TimerData>> map = new HashMap<Object, List<TimerData>>();
		for (TimerData data : displayedData) {
			List<TimerData> list = map.get(getSeriesKey(data));
			if (null == list) {
				list = new ArrayList<TimerData>();
				map.put(getSeriesKey(data), list);
			}
			list.add(data);
		}

		for (Entry<Object, List<TimerData>> entry : map.entrySet()) {
			entry.setValue(adjustSamplingRate(entry.getValue(), from, to, aggregator));
		}

		// update plots in UI thread
		final Map<Object, List<TimerData>> finalMap = map;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				setDurationPlotData(finalMap);
				setCountPlotData(finalMap);

				// if we have only one list then we pass it to the root editor so that sub-views can
				// set it
				if (1 == finalMap.size()) {
					final List<? extends DefaultData> datas = finalMap.entrySet().iterator().next().getValue();
					getRootEditor().setDataInput(datas);
				} else {
					getRootEditor().setDataInput(Collections.<DefaultData> emptyList());
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Comparable<?> getSeriesKey(TimerData template) {
		MethodIdent methodIdent = cachedDataService.getMethodIdentForId(template.getMethodIdent());
		return TextFormatter.getMethodString(methodIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<TimerData> getTemplates() {
		return templates;
	}
}
