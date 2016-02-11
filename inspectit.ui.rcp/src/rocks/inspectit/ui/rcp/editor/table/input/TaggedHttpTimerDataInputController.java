package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.comparator.HttpTimerDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.InvocationAwareDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.comparator.TimerDataComparatorEnum;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;

import java.util.ArrayList;
import java.util.List;

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
 * Input controller for the tagged http view.
 * 
 * @author Stefan Siegl
 */
public class TaggedHttpTimerDataInputController extends AbstractHttpInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.taggedhttptimerdata";

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
		TAG_VALUE("Tag Value", 300, InspectITImages.IMG_HTTP_TAGGED, HttpTimerDataComparatorEnum.TAG_VALUE),
		/** The request method. */
		HTTP_METHOD("Method", 80, null, HttpTimerDataComparatorEnum.HTTP_METHOD),
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 120, InspectITImages.IMG_INVOCATION, InvocationAwareDataComparatorEnum.INVOCATION_AFFILIATION),
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
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Getting HTTP data information", IProgressMonitor.UNKNOWN);
		List<HttpTimerData> aggregatedTimerData;

		if (autoUpdate) {
			aggregatedTimerData = httptimerDataAccessService.getTaggedAggregatedTimerData(template, httpCatorizationOnRequestMethodActive);
		} else {
			aggregatedTimerData = httptimerDataAccessService.getTaggedAggregatedTimerData(template, httpCatorizationOnRequestMethodActive, fromDate, toDate);
		}

		timerDataList.clear();
		if (CollectionUtils.isNotEmpty(aggregatedTimerData)) {
			timerDataList.addAll(aggregatedTimerData);
		}

		monitor.done();
	}

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
			ResultComparator<HttpTimerData> resultComparator = new ResultComparator<HttpTimerData>(column.dataComparator, cachedDataService);
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
		}
		throw new RuntimeException("Could not create the human readable string!");
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
		case TAG_VALUE:
			return new StyledString(data.getHttpInfo().getInspectItTaggingHeaderValue());
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
}
