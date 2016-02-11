package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.ITimerDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.InvocationAwareDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.MethodSensorDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.comparator.TimerDataComparatorEnum;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.LiveMode;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * Table input controller for the aggregated Timer data view.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.aggregatedtimerdata";

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * @author Ivan Senic
	 * 
	 */
	private static enum Column {
		/** The time column. */
		CHARTING("Charting", 20, null, TimerDataComparatorEnum.CHARTING),
		/** The package column. */
		PACKAGE("Package", 200, InspectITImages.IMG_PACKAGE, MethodSensorDataComparatorEnum.PACKAGE),
		/** The class column. */
		CLASS("Class", 200, InspectITImages.IMG_CLASS, MethodSensorDataComparatorEnum.CLASS),
		/** The method column. */
		METHOD("Method", 300, InspectITImages.IMG_METHOD, MethodSensorDataComparatorEnum.METHOD),
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
		private IDataComparator<? super TimerData> dataComparator;

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
		private Column(String name, int width, String imageName, IDataComparator<? super TimerData> dataComparator) {
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
	 * Timer data access service.
	 */
	private ITimerDataAccessService timerDataAccessService;

	/**
	 * Global data access service.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * List of Timer data to be displayed.
	 */
	private List<TimerData> timerDataList = new ArrayList<TimerData>();

	/**
	 * Template object used for querying.
	 */
	private TimerData template;

	/**
	 * Empty styled string.
	 */
	private final StyledString emptyStyledString = new StyledString();

	/**
	 * Date to display invocations from.
	 */
	private Date fromDate = null;

	/**
	 * Date to display invocations to.
	 */
	private Date toDate = null;

	/**
	 * Are we in live mode.
	 */
	private boolean autoUpdate = LiveMode.ACTIVE_DEFAULT;

	/**
	 * Decimal places.
	 */
	private int timeDecimalPlaces = PreferencesUtils.getIntValue(PreferencesConstants.DECIMAL_PLACES);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new TimerData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());

		timerDataAccessService = inputDefinition.getRepositoryDefinition().getTimerDataAccessService();
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
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
			if (Column.EXCLUSIVEAVERAGE.equals(column) || Column.EXCLUSIVESUM.equals(column) || Column.EXCLUSIVEMIN.equals(column) || Column.EXCLUSIVEMAX.equals(column)) {
				// TODO: Remove this tooltip and add it to the cell as soon as the image bug is
				// fixed in Eclipse.
				viewerColumn.getColumn().setToolTipText(
						"Exclusive times can only be calculated correctly if the timer is within an invocation sequence. "
								+ "A warning marker is provided if not all timers are run within an invocation sequence. Please be aware that "
								+ "avg, sum, min and max calculations are reflecting only the timers inside an invocation sequence.");
			}

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
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferences.add(PreferenceId.CLEAR_BUFFER);
			preferences.add(PreferenceId.LIVEMODE);
		}
		preferences.add(PreferenceId.UPDATE);
		preferences.add(PreferenceId.TIME_RESOLUTION);
		preferences.add(PreferenceId.TIMELINE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
		case TIMELINE:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				fromDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.FROM_DATE_ID);
			}
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				toDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.TO_DATE_ID);
			}
			break;
		case LIVEMODE:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.LiveMode.BUTTON_LIVE_ID)) {
				autoUpdate = (Boolean) preferenceEvent.getPreferenceMap().get(PreferenceId.LiveMode.BUTTON_LIVE_ID);
			}
			break;
		case TIME_RESOLUTION:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeResolution.TIME_DECIMAL_PLACES_ID)) {
				timeDecimalPlaces = (Integer) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeResolution.TIME_DECIMAL_PLACES_ID);
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
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (null == data) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTableInput() {
		return timerDataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Getting timer data information", IProgressMonitor.UNKNOWN);
		List<TimerData> aggregatedTimerData;
		if (autoUpdate) {
			aggregatedTimerData = timerDataAccessService.getAggregatedTimerData(template);
		} else {
			aggregatedTimerData = timerDataAccessService.getAggregatedTimerData(template, fromDate, toDate);
		}

		timerDataList.clear();
		if (CollectionUtils.isNotEmpty(aggregatedTimerData)) {
			timerDataList.addAll(aggregatedTimerData);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new TimerDataContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new TimerDataLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		TableViewerComparator<TimerData> timerDataViewerComparator = new TableViewerComparator<TimerData>();
		for (Column column : Column.values()) {
			ResultComparator<TimerData> resultComparator = new ResultComparator<TimerData>(column.dataComparator, cachedDataService);
			timerDataViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return timerDataViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReadableString(Object object) {
		if (object instanceof TimerData) {
			TimerData data = (TimerData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, methodIdent, column).toString());
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
		if (object instanceof TimerData) {
			TimerData data = (TimerData) object;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, methodIdent, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * Content provider for the view.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private static final class TimerDataContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<TimerData>) inputElement).toArray();
		}

	}

	/**
	 * Label provider for the view.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private final class TimerDataLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StyledString getStyledText(Object element, int index) {
			TimerData data = (TimerData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			StyledString styledString = getStyledTextForColumn(data, methodIdent, enumId);
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
			TimerData data = (TimerData) element;
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
			TimerData data = (TimerData) element;
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case CHARTING:
				if (data.isCharting()) {
					return "Duration chart can be displayed for this timer data.";
				}
			default:
				return super.getToolTipText(element, index);
			}
		}
	}

	/**
	 * Returns the styled text for a specific column.
	 * 
	 * @param data
	 *            The data object to extract the information from.
	 * @param methodIdent
	 *            The method ident object.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(TimerData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case CHARTING:
			return emptyStyledString;
		case PACKAGE:
			if (methodIdent.getPackageName() != null && !methodIdent.getPackageName().equals("")) {
				return new StyledString(methodIdent.getPackageName());
			} else {
				return new StyledString("(default)");
			}
		case CLASS:
			return new StyledString(methodIdent.getClassName());
		case METHOD:
			return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
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
	 * @return the timerDataList
	 */
	public List<TimerData> getTimerDataList() {
		return timerDataList;
	}

	/**
	 * @return the timerDataAccessService
	 */
	public ITimerDataAccessService getTimerDataAccessService() {
		return timerDataAccessService;
	}

}
