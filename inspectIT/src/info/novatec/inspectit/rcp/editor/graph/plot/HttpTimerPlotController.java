package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.cmr.service.IHttpTimerDataAccessService;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.HttpChartingInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.util.data.RegExAggregatedHttpTimerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.swt.widgets.Display;

/**
 * {@link PlotController} for displaying many Http requests in the graph.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpTimerPlotController extends AbstractTimerDataPlotController<HttpTimerData> {

	/**
	 * {@link IAggregator}.
	 */
	private static final IAggregator<HttpTimerData> AGGREGATOR = new SimpleHttpAggregator();

	/**
	 * Templates that will be used for data display. Every template is one line in line chart.
	 */
	private List<HttpTimerData> templates;

	/**
	 * List of {@link RegExAggregatedHttpTimerData} if regular expression is defined.
	 */
	private List<RegExAggregatedHttpTimerData> regExTemplates;

	/**
	 * If true tag values from templates will be used in plotting. Otherwise URI is used.
	 */
	private boolean plotByTagValue = false;

	/**
	 * If true than regular expression transformation will be performed on the template URIs.
	 */
	private boolean regExTransformation = false;

	/**
	 * {@link IHttpTimerDataAccessService}.
	 */
	private IHttpTimerDataAccessService dataAccessService;

	/**
	 * List of displayed data.
	 */
	List<HttpTimerData> displayedData = Collections.emptyList();

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

		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.HTTP_CHARTING_EXTRAS_MARKER)) {
			HttpChartingInputDefinitionExtra inputDefinitionExtra = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.HTTP_CHARTING_EXTRAS_MARKER);
			templates = inputDefinitionExtra.getTemplates();
			plotByTagValue = inputDefinitionExtra.isPlotByTagValue();
			regExTransformation = inputDefinitionExtra.isRegExTransformation();
			if (regExTransformation) {
				regExTemplates = inputDefinitionExtra.getRegExTemplates();
			}
		}

		dataAccessService = inputDefinition.getRepositoryDefinition().getHttpTimerDataAccessService();
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
	@Override
	public void update(Date from, Date to) {
		// complete load if we have no data, or wanted time range is completely outside the current
		boolean completeLoad = CollectionUtils.isEmpty(displayedData) || fromDate.after(to) || toDate.before(from);
		// left append if currently displayed from date is after the new from date
		boolean leftAppend = fromDate.after(from);
		// right append if the currently displayed to date is before new to date or the date of the
		// last data is before new date
		boolean rightAppend = toDate.before(to) || latestDataDate.before(to);

		if (completeLoad) {
			List<HttpTimerData> httpTimerDatas = dataAccessService.getChartingHttpTimerDataFromDateToDate(templates, from, to, plotByTagValue);
			if (CollectionUtils.isNotEmpty(httpTimerDatas)) {
				fromDate = (Date) from.clone();
				toDate = (Date) to.clone();
			}
			displayedData = httpTimerDatas;
		} else {
			if (rightAppend) {
				Date startingFrom = new Date(latestDataDate.getTime() + 1);
				List<HttpTimerData> httpTimerDatas = dataAccessService.getChartingHttpTimerDataFromDateToDate(templates, startingFrom, to, plotByTagValue);
				if (CollectionUtils.isNotEmpty(httpTimerDatas)) {
					displayedData.addAll(httpTimerDatas);
					toDate = (Date) to.clone();
				}
			}
			if (leftAppend) {
				Date endingTo = new Date(fromDate.getTime() - 1);
				List<HttpTimerData> httpTimerDatas = dataAccessService.getChartingHttpTimerDataFromDateToDate(templates, from, endingTo, plotByTagValue);
				if (CollectionUtils.isNotEmpty(httpTimerDatas)) {
					displayedData.addAll(0, httpTimerDatas);
					fromDate = (Date) from.clone();
				}
			}
		}

		// update the last displayed data
		if (CollectionUtils.isNotEmpty(displayedData)) {
			latestDataDate = new Date(displayedData.get(displayedData.size() - 1).getTimeStamp().getTime());
		}

		Map<Object, List<HttpTimerData>> map = new HashMap<Object, List<HttpTimerData>>();
		for (HttpTimerData data : displayedData) {
			HttpTimerData template = findTemplateForData(data);
			if (null == template) {
				continue;
			}
			Object seriesKey = getSeriesKey(template);
			List<HttpTimerData> list = map.get(seriesKey);
			if (null == list) {
				list = new ArrayList<HttpTimerData>();
				map.put(seriesKey, list);
			}
			list.add(data);
		}

		for (Entry<Object, List<HttpTimerData>> entry : map.entrySet()) {
			entry.setValue(adjustSamplingRate(entry.getValue(), from, to, AGGREGATOR));
		}

		final Map<Object, List<HttpTimerData>> finalMap = map;

		// update plots in UI thread
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setDurationPlotData(finalMap);
				setCountPlotData(finalMap);
			}
		});

	}

	/**
	 * Finds matching template for the given {@link HttpTimerData} based on if regular expression
	 * transformation is active or not.
	 * 
	 * @param httpTimerData
	 *            Data to find matching template.
	 * @return Matching template of <code>null</code> if one can not be found.
	 */
	private HttpTimerData findTemplateForData(HttpTimerData httpTimerData) {
		if (regExTransformation) {
			for (RegExAggregatedHttpTimerData regExTemplate : regExTemplates) {
				if (HttpTimerData.REQUEST_METHOD_MULTIPLE.equals(regExTemplate.getHttpInfo().getRequestMethod())
						|| Objects.equals(regExTemplate.getHttpInfo().getRequestMethod(), httpTimerData.getHttpInfo().getRequestMethod())) {
					if (null != findTemplateForUriData(httpTimerData, regExTemplate.getAggregatedDataList(), true)) {
						return regExTemplate;
					}
				}
			}
		} else if (plotByTagValue) {
			return findTemplateForTagData(httpTimerData, templates);
		} else {
			return findTemplateForUriData(httpTimerData, templates, false);
		}
		return null;
	}

	/**
	 * Finds matching template for the given {@link HttpTimerData} by uri.
	 * 
	 * @param httpTimerData
	 *            Data to find matching template.
	 * @param templates
	 *            List of templates to search.
	 * @param checkOnlyUri
	 *            If matching should be done only by uri.
	 * @return Matching template of <code>null</code> if one can not be found.
	 */
	private HttpTimerData findTemplateForUriData(HttpTimerData httpTimerData, List<HttpTimerData> templates, boolean checkOnlyUri) {
		for (HttpTimerData template : templates) {
			if (Objects.equals(template.getHttpInfo().getUri(), httpTimerData.getHttpInfo().getUri())) {
				if (!checkOnlyUri && HttpTimerData.REQUEST_METHOD_MULTIPLE.equals(template.getHttpInfo().getRequestMethod())) {
					return template;
				} else if (Objects.equals(template.getHttpInfo().getRequestMethod(), httpTimerData.getHttpInfo().getRequestMethod())) {
					return template;
				}
			}
		}
		return null;
	}

	/**
	 * Finds matching template for the given {@link HttpTimerData} by tag value.
	 * 
	 * @param httpTimerData
	 *            Data to find matching template.
	 * @param templates
	 *            List of templates to search.
	 * @return Matching template of <code>null</code> if one can not be found.
	 */
	private HttpTimerData findTemplateForTagData(HttpTimerData httpTimerData, List<HttpTimerData> templates) {
		for (HttpTimerData template : templates) {
			if (Objects.equals(template.getHttpInfo().getInspectItTaggingHeaderValue(), httpTimerData.getHttpInfo().getInspectItTaggingHeaderValue())) {
				if (HttpTimerData.REQUEST_METHOD_MULTIPLE.equals(template.getHttpInfo().getRequestMethod())
						|| Objects.equals(template.getHttpInfo().getRequestMethod(), httpTimerData.getHttpInfo().getRequestMethod())) {
					return template;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the series key for the {@link HttpTimerData} object.
	 * 
	 * @param httpTimerData
	 *            {@link HttpTimerData}.
	 * @return Key used to initialize the series and later on compare which series data should be
	 *         added to.
	 */
	protected Comparable<?> getSeriesKey(HttpTimerData httpTimerData) {
		if (regExTransformation && httpTimerData instanceof RegExAggregatedHttpTimerData) {
			return "Transformed URI: " + ((RegExAggregatedHttpTimerData) httpTimerData).getTransformedUri() + " [" + httpTimerData.getHttpInfo().getRequestMethod() + "]";
		} else {
			if (plotByTagValue) {
				return "Tag: " + httpTimerData.getHttpInfo().getInspectItTaggingHeaderValue() + " [" + httpTimerData.getHttpInfo().getRequestMethod() + "]";
			} else {
				return "URI: " + httpTimerData.getHttpInfo().getUri() + " [" + httpTimerData.getHttpInfo().getRequestMethod() + "]";
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<HttpTimerData> getTemplates() {
		if (regExTransformation) {
			return new ArrayList<HttpTimerData>(regExTemplates);
		} else {
			return templates;
		}
	}

	/**
	 * Simple {@link IAggregator} to use for {@link HttpTimerData} aggregation, since we separate
	 * the data correctly before aggregation.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static class SimpleHttpAggregator implements IAggregator<HttpTimerData> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void aggregate(IAggregatedData<HttpTimerData> aggregatedObject, HttpTimerData objectToAdd) {
			aggregatedObject.aggregate(objectToAdd);

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IAggregatedData<HttpTimerData> getClone(HttpTimerData object) {
			return new AggregatedHttpTimerData();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getAggregationKey(HttpTimerData object) {
			return 1;
		}

	}
}
