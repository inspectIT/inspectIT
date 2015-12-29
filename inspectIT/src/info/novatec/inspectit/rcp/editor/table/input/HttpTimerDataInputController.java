package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdentHelper;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.comparator.HttpTimerDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.InvocationAwareDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.comparator.TimerDataComparatorEnum;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.indexing.aggregation.impl.AggregationPerformer;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.util.data.RegExAggregatedHttpTimerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * InputController for <code>HttpTimerData</code> view.
 * 
 * @author Stefan Siegl
 */
public class HttpTimerDataInputController extends AbstractHttpInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.aggregatedhttptimerdata";

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Stefan Siegl
	 * 
	 */
	private static enum Column {
		/** The time column. */
		CHARTING("Charting", 20, null, TimerDataComparatorEnum.CHARTING),
		/** The package column. */
		URI("URI", 300, InspectITImages.IMG_HTTP_URL, HttpTimerDataComparatorEnum.URI),
		/** The http method. */
		HTTP_METHOD("Method", 80, null, HttpTimerDataComparatorEnum.HTTP_METHOD),
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 130, InspectITImages.IMG_INVOCATION, InvocationAwareDataComparatorEnum.INVOCATION_AFFILIATION),
		/** The count column. */
		COUNT("Count", 60, null, TimerDataComparatorEnum.COUNT),
		/** The average column. */
		AVERAGE("Avg (ms)", 60, null, TimerDataComparatorEnum.AVERAGE),
		/** The minimum column. */
		MIN("Min (ms)", 60, null, TimerDataComparatorEnum.MIN),
		/** The maximum column. */
		MAX("Max (ms)", 60, null, TimerDataComparatorEnum.MAX),
		/** The duration column. */
		DURATION("Duration (ms)", 70, null, TimerDataComparatorEnum.DURATION),
		/** The average exclusive duration column. */
		EXCLUSIVEAVERAGE("Exc. Avg (ms)", 80, null, TimerDataComparatorEnum.EXCLUSIVEAVERAGE),
		/** The min exclusive duration column. */
		EXCLUSIVEMIN("Exc. Min (ms)", 80, null, TimerDataComparatorEnum.EXCLUSIVEMIN),
		/** The max exclusive duration column. */
		EXCLUSIVEMAX("Exc. Max (ms)", 80, null, TimerDataComparatorEnum.EXCLUSIVEMAX),
		/** The total exclusive duration column. */
		EXCLUSIVESUM("Exc. duration (ms)", 80, null, TimerDataComparatorEnum.EXCLUSIVEDURATION),
		/** The cpu average column. */
		CPUAVERAGE("Cpu Avg (ms)", 60, null, TimerDataComparatorEnum.CPUAVERAGE),
		/** The cpu minimum column. */
		CPUMIN("Cpu Min (ms)", 60, null, TimerDataComparatorEnum.CPUMIN),
		/** The cpu maximum column. */
		CPUMAX("Cpu Max (ms)", 60, null, TimerDataComparatorEnum.CPUMAX),
		/** The cpu duration column. */
		CPUDURATION("Cpu Duration (ms)", 70, null, TimerDataComparatorEnum.CPUDURATION);

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;
		/** Comparator for the column. */
		private IDataComparator<? super HttpTimerData> dataComparator;

		/**
		 * Default constructor which creates a column enumeration object.
		 * 
		 * @param name
		 *            The name of the column.
		 * @param width
		 *            The width of the column.
		 * @param imageName
		 *            The name of the image. Names are defined in {@link InspectITImages}.
		 * @param dataComparator
		 *            Comparator for the column.
		 */
		private Column(String name, int width, String imageName, IDataComparator<? super HttpTimerData> dataComparator) {
			this.name = name;
			this.width = width;
			this.image = InspectIT.getDefault().getImage(imageName);
			this.dataComparator = dataComparator;
		}

		/**
		 * Converts an ordinal into a column.
		 * 
		 * @param i
		 *            The ordinal.
		 * @return The appropriate column.
		 */
		public static Column fromOrd(int i) {
			if (i < 0 || i >= Column.values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}

	}

	/**
	 * Defines if correct regular expression is defined in sensor.
	 */
	private boolean regExEnabledInSensor = false;

	/**
	 * If the regular expression transformation of the URI is active.
	 */
	private boolean regExActive = PreferenceId.HttpUriTransformation.DEFAULT;

	/**
	 * The active HTTP sensor type ident.
	 */
	private MethodSensorTypeIdent httpSensorTypeIdent;

	/**
	 * Cached data service.
	 */
	private ICachedDataService cachedDataService;

	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();

		if (0 != inputDefinition.getIdDefinition().getSensorTypeId()) {
			httpSensorTypeIdent = (MethodSensorTypeIdent) cachedDataService.getSensorTypeIdentForId(inputDefinition.getIdDefinition().getSensorTypeId());
			String regEx = MethodSensorTypeIdentHelper.getRegEx(httpSensorTypeIdent);
			if (null != regEx) {
				try {
					Pattern.compile(regEx);
					regExEnabledInSensor = true;
				} catch (PatternSyntaxException e) {
					InspectIT.getDefault().createInfoDialog(
							"The HTTP sensor defines the Regular expression " + regEx
									+ " for URI transformation that can not be compiled. The transformation option will not be available.\n\n Reason: " + e.getMessage(), -1);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = super.getPreferenceIds();
		if (regExEnabledInSensor) {
			preferences.add(PreferenceId.HTTP_URI_TRANSFORMING);
		}
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		super.preferenceEventFired(preferenceEvent);
		switch (preferenceEvent.getPreferenceId()) {
		case HTTP_URI_TRANSFORMING:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.HttpUriTransformation.URI_TRANSFORMATION_ACTIVE)) {
				regExActive = (Boolean) preferenceEvent.getPreferenceMap().get(PreferenceId.HttpUriTransformation.URI_TRANSFORMATION_ACTIVE);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Getting HTTP timer data information", IProgressMonitor.UNKNOWN);
		List<HttpTimerData> aggregatedHttpData;

		if (autoUpdate) {
			aggregatedHttpData = httptimerDataAccessService.getAggregatedTimerData(template, httpCatorizationOnRequestMethodActive);
		} else {
			aggregatedHttpData = httptimerDataAccessService.getAggregatedTimerData(template, httpCatorizationOnRequestMethodActive, fromDate, toDate);
		}

		if (regExActive && CollectionUtils.isNotEmpty(aggregatedHttpData)) {
			AggregationPerformer<HttpTimerData> aggregationPerformer = new AggregationPerformer<HttpTimerData>(new RegExHttpAggregator(httpSensorTypeIdent, httpCatorizationOnRequestMethodActive));
			aggregationPerformer.processCollection(aggregatedHttpData);
			aggregatedHttpData = aggregationPerformer.getResultList();
		}

		timerDataList.clear();
		if (CollectionUtils.isNotEmpty(aggregatedHttpData)) {
			timerDataList.addAll(aggregatedHttpData);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createColumns(TableViewer tableViewer) {
		for (Column column : Column.values()) {
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText(column.name);
			viewerColumn.getColumn().setWidth(column.width);
			if (null != column.image) {
				viewerColumn.getColumn().setImage(column.image);
			}
			mapTableViewerColumn(column, viewerColumn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new StyledCellIndexLabelProvider() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public StyledString getStyledText(Object element, int index) {
				HttpTimerData data = (HttpTimerData) element;
				Column enumId = Column.fromOrd(index);

				StyledString styledString = getStyledTextForColumn(data, enumId);
				if (addWarnSign(data, enumId)) {
					styledString.append(TextFormatter.getWarningSign());
				}
				return styledString;
			}

			/**
			 * Decides if the warn sign should be added for the specific column.
			 * 
			 * @param data
			 *            TimerData
			 * @param column
			 *            Column to check.
			 * @return True if warn sign should be added.
			 */
			private boolean addWarnSign(TimerData data, Column column) {
				switch (column) {
				case EXCLUSIVEAVERAGE:
				case EXCLUSIVEMAX:
				case EXCLUSIVEMIN:
				case EXCLUSIVESUM:
					int affPercentage = (int) (data.getInvocationAffiliationPercentage() * 100);
					return data.isExclusiveTimeDataAvailable() && affPercentage < 100;
				default:
					return false;
				}
			}

			/**
			 * 
			 * {@inheritDoc}
			 */
			@Override
			protected Image getColumnImage(Object element, int index) {
				HttpTimerData data = (HttpTimerData) element;
				Column enumId = Column.fromOrd(index);

				switch (enumId) {
				case CHARTING:
					if (data.isCharting()) {
						return InspectIT.getDefault().getImage(InspectITImages.IMG_CHART_PIE);
					}
				default:
					return super.getColumnImage(element, index);
				}

			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getToolTipText(Object element, int index) {
				HttpTimerData data = (HttpTimerData) element;
				Column enumId = Column.fromOrd(index);

				switch (enumId) {
				case CHARTING:
					if (data.isCharting()) {
						return "Duration chart can be displayed for this HTTP data.";
					}
				default:
					return super.getToolTipText(element, index);
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		ICachedDataService cachedDataService = getInputDefinition().getRepositoryDefinition().getCachedDataService();
		TableViewerComparator<HttpTimerData> httpTimerDataViewerComparator = new TableViewerComparator<HttpTimerData>();
		for (Column column : Column.values()) {
			ResultComparator<HttpTimerData> resultComparator;
			if (Column.URI.equals(column)) {
				resultComparator = new ResultComparator<HttpTimerData>(new UriOrRegExComparator(column.dataComparator), cachedDataService);
			} else {
				resultComparator = new ResultComparator<HttpTimerData>(column.dataComparator, cachedDataService);
			}
			httpTimerDataViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return httpTimerDataViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReadableString(Object object) {
		if (object instanceof HttpTimerData) {
			HttpTimerData data = (HttpTimerData) object;
			StringBuilder sb = new StringBuilder();
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, column).toString());
				sb.append('\t');
			}
			return sb.toString();
		} else {
			throw new RuntimeException("Could not create the human readable string! Class is: " + object.getClass().getCanonicalName());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getColumnValues(Object object) {
		if (object instanceof HttpTimerData) {
			HttpTimerData data = (HttpTimerData) object;
			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * Returns the styled text for a specific column.
	 * 
	 * @param data
	 *            The data object to extract the information from.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(HttpTimerData data, Column enumId) {
		switch (enumId) {
		case CHARTING:
			return emptyStyledString;
		case URI:
			if (data instanceof RegExAggregatedHttpTimerData) {
				return new StyledString(((RegExAggregatedHttpTimerData) data).getTransformedUri());
			} else {
				return new StyledString(data.getHttpInfo().getUri());
			}
		case HTTP_METHOD:
			return new StyledString(data.getHttpInfo().getRequestMethod());
		case INVOCATION_AFFILLIATION:
			int percentage = (int) (data.getInvocationAffiliationPercentage() * 100);
			int invocations = 0;
			if (null != data.getInvocationParentsIdSet()) {
				invocations = data.getInvocationParentsIdSet().size();
			}
			return TextFormatter.getInvocationAffilliationPercentageString(percentage, invocations);
		case COUNT:
			return new StyledString(String.valueOf(data.getCount()));
		case AVERAGE:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getAverage(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case MIN:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMin(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case MAX:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMax(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case DURATION:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getDuration(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUAVERAGE:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuAverage(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUMIN:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMin(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUMAX:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMax(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case CPUDURATION:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuDuration(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEAVERAGE:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveAverage(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMAX:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMax(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMIN:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMin(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVESUM:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveDuration(), timeDecimalPlaces));
			} else {
				return emptyStyledString;
			}
		default:
			return new StyledString("error");
		}
	}

	/**
	 * The RegEx aggregator.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	@SuppressWarnings("serial")
	private static final class RegExHttpAggregator extends HttpTimerDataAggregator {

		/**
		 * HTTP sensor type ident.
		 */
		private MethodSensorTypeIdent httpSensorTypeIdent;

		/**
		 * Default constructor.
		 * 
		 * @param httpSensorTypeIdent
		 *            HTTP sensor type ident.
		 * @param includeRequestMethod
		 *            If request method should be included.
		 */
		public RegExHttpAggregator(MethodSensorTypeIdent httpSensorTypeIdent, boolean includeRequestMethod) {
			super(true, includeRequestMethod);
			this.httpSensorTypeIdent = httpSensorTypeIdent;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void aggregate(IAggregatedData<HttpTimerData> aggregatedObject, HttpTimerData objectToAdd) {
			super.aggregate(aggregatedObject, objectToAdd);
			((RegExAggregatedHttpTimerData) aggregatedObject).getAggregatedDataList().add(objectToAdd);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IAggregatedData<HttpTimerData> getClone(HttpTimerData httpData) {
			RegExAggregatedHttpTimerData clone = new RegExAggregatedHttpTimerData();
			clone.setPlatformIdent(httpData.getPlatformIdent());
			clone.setSensorTypeIdent(httpData.getSensorTypeIdent());
			clone.setMethodIdent(httpData.getMethodIdent());
			clone.setCharting(httpData.isCharting());
			clone.getHttpInfo().setRequestMethod(httpData.getHttpInfo().getRequestMethod());
			clone.setTransformedUri(RegExAggregatedHttpTimerData.getTransformedUri(httpData, httpSensorTypeIdent));
			return clone;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getAggregationKey(HttpTimerData httpData) {
			final int prime = 31;
			int result = 0;
			String transformed = RegExAggregatedHttpTimerData.getTransformedUri(httpData, httpSensorTypeIdent);
			result = prime * result + ((transformed == null) ? 0 : transformed.hashCode());

			if (includeRequestMethod) {
				result = prime * result + ((httpData.getHttpInfo().getRequestMethod() == null) ? 0 : httpData.getHttpInfo().getRequestMethod().hashCode());
			}
			return result;
		}

	}

	/**
	 * Comparator that is needed for the column where URI or regular expression transformation can
	 * be displayed.
	 * 
	 * @author Ivan Senic
	 */
	private static final class UriOrRegExComparator implements IDataComparator<HttpTimerData> {

		/**
		 * The comparator that will be used if reg ex is not active.
		 */
		private final IDataComparator<? super HttpTimerData> comparator;

		/**
		 * @param dataComparator
		 *            The comparator that will be used if reg ex is not active.
		 */
		public UriOrRegExComparator(IDataComparator<? super HttpTimerData> dataComparator) {
			this.comparator = dataComparator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(HttpTimerData o1, HttpTimerData o2, ICachedDataService cachedDataService) {
			if (o1 instanceof RegExAggregatedHttpTimerData && o2 instanceof RegExAggregatedHttpTimerData) {
				return ((RegExAggregatedHttpTimerData) o1).getTransformedUri().compareToIgnoreCase(((RegExAggregatedHttpTimerData) o2).getTransformedUri());
			} else {
				return comparator.compare(o1, o2, cachedDataService);
			}
		}

	}

}
