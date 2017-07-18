package rocks.inspectit.ui.rcp.editor.table.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
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
import org.eclipse.swt.widgets.TableColumn;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.cs.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.comparator.IDataComparator;
import rocks.inspectit.shared.cs.communication.comparator.MethodSensorDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.comparator.TimerDataComparatorEnum;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.AggregationPerformer;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.TimerDataAggregator;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.IPreferenceGroup;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.table.TableViewerComparator;
import rocks.inspectit.ui.rcp.editor.tree.util.TraceTreeData;
import rocks.inspectit.ui.rcp.editor.viewers.RawAggregatedResultComparator;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.handlers.ShowHideColumnsHandler;

/**
 * This input controller displays details of all methods involved in an invocation sequence.
 *
 * @author Patrice Bouillet
 *
 */
public class MethodInvocInputController extends AbstractTableInputController {

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private static enum Column {
		/** The timestamp column. */
		TIMESTAMP("Timestamp", 130, InspectITImages.IMG_TIMESTAMP, false, true, DefaultDataComparatorEnum.TIMESTAMP),
		/** The package column. */
		PACKAGE("Package", 200, InspectITImages.IMG_PACKAGE, true, true, MethodSensorDataComparatorEnum.PACKAGE),
		/** The class column. */
		CLASS("Class", 200, InspectITImages.IMG_CLASS, true, true, MethodSensorDataComparatorEnum.CLASS),
		/** The method column. */
		METHOD("Method", 300, InspectITImages.IMG_METHOD, true, true, MethodSensorDataComparatorEnum.METHOD),
		/** The count column. */
		COUNT("Count", 60, null, true, false, TimerDataComparatorEnum.COUNT),
		/** The average column. */
		AVERAGE("Avg (ms)", 60, null, true, false, TimerDataComparatorEnum.AVERAGE),
		/** The minimum column. */
		MIN("Min (ms)", 60, null, true, false, TimerDataComparatorEnum.MIN),
		/** The maximum column. */
		MAX("Max (ms)", 60, null, true, false, TimerDataComparatorEnum.MAX),
		/** The duration column. */
		DURATION("Duration (ms)", 70, null, true, true, TimerDataComparatorEnum.DURATION),
		/** The average exclusive duration column. */
		EXCLUSIVEAVERAGE("Exc. Avg (ms)", 80, null, true, false, TimerDataComparatorEnum.EXCLUSIVEAVERAGE),
		/** The min exclusive duration column. */
		EXCLUSIVEMIN("Exc. Min (ms)", 80, null, true, false, TimerDataComparatorEnum.EXCLUSIVEMIN),
		/** The max exclusive duration column. */
		EXCLUSIVEMAX("Exc. Max (ms)", 80, null, true, false, TimerDataComparatorEnum.EXCLUSIVEMAX),
		/** The total exclusive duration column. */
		EXCLUSIVESUM("Exc. duration (ms)", 80, null, true, true, TimerDataComparatorEnum.EXCLUSIVEDURATION),
		/** The cpu average column. */
		CPUAVERAGE("Cpu Avg (ms)", 60, null, true, false, TimerDataComparatorEnum.CPUAVERAGE),
		/** The cpu minimum column. */
		CPUMIN("Cpu Min (ms)", 60, null, true, false, TimerDataComparatorEnum.CPUMIN),
		/** The cpu maximum column. */
		CPUMAX("Cpu Max (ms)", 60, null, true, false, TimerDataComparatorEnum.CPUMAX),
		/** The cpu duration column. */
		CPUDURATION("Cpu Duration (ms)", 70, null, true, true, TimerDataComparatorEnum.CPUDURATION);

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;
		/** If the column should be shown in aggregated mode. */
		private boolean showInAggregatedMode;
		/** If the column should be shown in raw mode. */
		private boolean showInRawMode;
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
		 * @param showInAggregatedMode
		 *            If the column should be shown in aggregated mode.
		 * @param showInRawMode
		 *            If the column should be shown in raw mode.
		 * @param dataComparator
		 *            Comparator for the column.
		 *
		 */
		private Column(String name, int width, String imageName, boolean showInAggregatedMode, boolean showInRawMode, IDataComparator<? super TimerData> dataComparator) {
			this.name = name;
			this.width = width;
			this.image = InspectIT.getDefault().getImage(imageName);
			this.showInAggregatedMode = showInAggregatedMode;
			this.showInRawMode = showInRawMode;
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
			if ((i < 0) || (i >= Column.values().length)) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}
	}

	/**
	 * The cached service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * Empty styled string.
	 */
	private final StyledString emptyStyledString = new StyledString();

	/**
	 * List that is displayed after processing the invocation.
	 */
	private List<TimerData> timerDataList;

	/**
	 * Should view display raw mode or not.
	 */
	private boolean rawMode = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

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
			if (column.showInAggregatedMode) {
				viewerColumn.getColumn().setWidth(column.width);
			} else {
				viewerColumn.getColumn().setWidth(0);
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
	public boolean canAlterColumnWidth(TableColumn tableColumn) {
		for (Column column : Column.values()) {
			if (Objects.equals(getMappedTableViewerColumn(column).getColumn(), tableColumn)) {
				return (column.showInRawMode && rawMode) || (column.showInAggregatedMode && !rawMode);
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.INVOCATION_SUBVIEW_MODE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.INVOCATION_SUBVIEW_MODE.equals(preferenceEvent.getPreferenceId())) {
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if ((null != preferenceMap) && preferenceMap.containsKey(PreferenceId.InvocationSubviewMode.RAW)) {
				Boolean isRawMode = (Boolean) preferenceMap.get(PreferenceId.InvocationSubviewMode.RAW);

				// first show/hide columns and then change the rawMode value
				handleRawAggregatedColumnVisibility(isRawMode.booleanValue());
				rawMode = isRawMode.booleanValue();
			}
		}
	}

	/**
	 * Handles the raw and aggregated columns hiding/showing.
	 *
	 * @param rawMode
	 *            Is raw mode active.
	 */
	private void handleRawAggregatedColumnVisibility(boolean rawMode) {
		for (Column column : Column.values()) {
			if (rawMode) {
				if (column.showInRawMode && !column.showInAggregatedMode && !ShowHideColumnsHandler.isColumnHidden(this.getClass(), column.name)) {
					Integer width = ShowHideColumnsHandler.getRememberedColumnWidth(this.getClass(), column.name);
					getMappedTableViewerColumn(column).getColumn().setWidth((null != width) ? width.intValue() : column.width);
				} else if (!column.showInRawMode && column.showInAggregatedMode) {
					getMappedTableViewerColumn(column).getColumn().setWidth(0);
				}
			} else {
				if (!column.showInRawMode && column.showInAggregatedMode && !ShowHideColumnsHandler.isColumnHidden(this.getClass(), column.name)) {
					Integer width = ShowHideColumnsHandler.getRememberedColumnWidth(this.getClass(), column.name);
					getMappedTableViewerColumn(column).getColumn().setWidth((null != width) ? width.intValue() : column.width);
				} else if (column.showInRawMode && !column.showInAggregatedMode) {
					getMappedTableViewerColumn(column).getColumn().setWidth(0);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new MethodInvocContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		TableViewerComparator<TimerData> methodInputViewerComparator = new TableViewerComparator<>();
		for (Column column : Column.values()) {
			RawAggregatedResultComparator<TimerData> comparator = new RawAggregatedResultComparator<TimerData>(column.dataComparator, cachedDataService, column.showInRawMode,
					column.showInAggregatedMode) {
				@Override
				protected boolean isRawMode() {
					return rawMode;
				}
			};
			methodInputViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), comparator);
		}

		return methodInputViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new MethodInvocLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends Object> data) {
		if (null == data) {
			return false;
		}

		if (data.isEmpty()) {
			return true;
		}

		// we accept invocation sequences
		if (data.get(0) instanceof InvocationSequenceData) {
			return true;
		}

		// or one trace data
		if (data.get(0) instanceof TraceTreeData) {
			return true;
		}

		return false;
	}

	/**
	 * The content provider for this view.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private final class MethodInvocContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<? extends Object> input = (List<? extends Object>) inputElement;

			if (CollectionUtils.isEmpty(input)) {
				return new Object[0];
			}

			List<InvocationSequenceData> invocationSequenceDataList;
			if (input.get(0) instanceof TraceTreeData) {
				invocationSequenceDataList = TraceTreeData.collectInvocations((TraceTreeData) input.get(0), new ArrayList<InvocationSequenceData>());
			} else {
				invocationSequenceDataList = (List<InvocationSequenceData>) inputElement;
			}

			timerDataList = getRawInputList(invocationSequenceDataList, new ArrayList<TimerData>());
			if (!rawMode) {
				AggregationPerformer<TimerData> aggregationPerformer = new AggregationPerformer<>(new TimerDataAggregator());
				aggregationPerformer.processCollection(timerDataList);
				timerDataList = aggregationPerformer.getResultList();
			} else {
				Collections.sort(timerDataList, new Comparator<TimerData>() {
					@Override
					public int compare(TimerData o1, TimerData o2) {
						return o1.getTimeStamp().compareTo(o2.getTimeStamp());
					}
				});
			}
			return timerDataList.toArray();
		}

		/**
		 * Creates the raw input list of timers from a list of invocations.
		 *
		 * @param invocationSequenceDataList
		 *            List of invocations to check.
		 * @param resultList
		 *            List where results will be stored. Needed because of reflection. Note that
		 *            this list will be returned as the result.
		 * @return List of raw order timer data.
		 */
		public List<TimerData> getRawInputList(List<InvocationSequenceData> invocationSequenceDataList, List<TimerData> resultList) {
			for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
				TimerData timerData = getTimerData(invocationSequenceData);
				if (null != timerData) {
					resultList.add(timerData);
				}

				getRawInputList(invocationSequenceData.getNestedSequences(), resultList);
			}

			return resultList;
		}

		/**
		 * Returns the extracted timer data from the invocation.
		 *
		 * @param invocationData
		 *            {@link InvocationSequenceData}.
		 * @return Timer data or null if it can not be created.
		 */
		private TimerData getTimerData(InvocationSequenceData invocationData) {
			TimerData timerData = null;
			if (null != invocationData.getTimerData()) {
				timerData = invocationData.getTimerData();
			} else if (null != invocationData.getSqlStatementData()) {
				timerData = invocationData.getSqlStatementData();
			} else if (null == invocationData.getParentSequence()) {
				timerData = createTimerDataForRootInvocation(invocationData);
			}
			return timerData;
		}

		/**
		 * Creates the timer data from a root invocation object.
		 *
		 * @param invocationData
		 *            Root invocation object.
		 * @return Timer data with set duration from the invocation and calculated exclusive
		 *         duration.
		 */
		private TimerData createTimerDataForRootInvocation(InvocationSequenceData invocationData) {
			TimerData timerData = new TimerData();
			timerData.setPlatformIdent(invocationData.getPlatformIdent());
			timerData.setMethodIdent(invocationData.getMethodIdent());
			timerData.setTimeStamp(invocationData.getTimeStamp());
			timerData.setDuration(invocationData.getDuration());
			timerData.calculateMax(invocationData.getDuration());
			timerData.calculateMin(invocationData.getDuration());
			timerData.increaseCount();
			double exclusiveTime = invocationData.getDuration() - computeNestedDuration(invocationData);
			timerData.setExclusiveDuration(exclusiveTime);
			timerData.calculateExclusiveMax(exclusiveTime);
			timerData.calculateExclusiveMin(exclusiveTime);
			timerData.increaseExclusiveCount();
			return timerData;
		}

		/**
		 * Computes the duration of the nested invocation elements.
		 *
		 * @param data
		 *            The data objects which is inspected for its nested elements.
		 * @return The duration of all nested sequences (with their nested sequences as well).
		 */
		private double computeNestedDuration(InvocationSequenceData data) {
			if (data.getNestedSequences().isEmpty()) {
				return 0;
			}

			double nestedDuration = 0d;
			boolean added = false;
			for (InvocationSequenceData nestedData : data.getNestedSequences()) {
				if (null == nestedData.getParentSequence()) {
					nestedDuration = nestedDuration + nestedData.getDuration();
					added = true;
				} else if (null != nestedData.getTimerData()) {
					nestedDuration = nestedDuration + nestedData.getTimerData().getDuration();
					added = true;
				} else if ((null != nestedData.getSqlStatementData()) && (1 == nestedData.getSqlStatementData().getCount())) {
					nestedDuration = nestedDuration + nestedData.getSqlStatementData().getDuration();
					added = true;
				}
				if (!added && !nestedData.getNestedSequences().isEmpty()) {
					// nothing was added, but there could be child elements with
					// time measurements
					nestedDuration = nestedDuration + computeNestedDuration(nestedData);
				}
				added = false;
			}

			return nestedDuration;
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
		@Override
		public void dispose() {
		}

	}

	/**
	 * The sql label provider used by this view.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private final class MethodInvocLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Creates the styled text.
		 *
		 * @param element
		 *            The element to create the styled text for.
		 * @param index
		 *            The index in the column.
		 * @return The created styled string.
		 */
		@Override
		public StyledString getStyledText(Object element, int index) {
			TimerData data = (TimerData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
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
		case TIMESTAMP:
			if (rawMode) {
				return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
			} else {
				return emptyStyledString;
			}
		case PACKAGE:
			if ((methodIdent.getPackageName() != null) && !methodIdent.getPackageName().equals("")) {
				return new StyledString(methodIdent.getPackageName());
			} else {
				return new StyledString("(default)");
			}
		case CLASS:
			return new StyledString(methodIdent.getClassName());
		case METHOD:
			return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
		case COUNT:
			return new StyledString(String.valueOf(data.getCount()));
		case AVERAGE:
			// check if it is a valid data (or if timer data was available)
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getAverage()));
			} else {
				return emptyStyledString;
			}
		case MIN:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMin()));
			} else {
				return emptyStyledString;
			}
		case MAX:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getMax()));
			} else {
				return emptyStyledString;
			}
		case DURATION:
			if (data.isTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getDuration()));
			} else {
				return emptyStyledString;
			}
		case CPUAVERAGE:
			// check if it is a valid data (or if timer data was available)
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuAverage()));
			} else {
				return emptyStyledString;
			}
		case CPUMIN:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMin()));
			} else {
				return emptyStyledString;
			}
		case CPUMAX:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuMax()));
			} else {
				return emptyStyledString;
			}
		case CPUDURATION:
			if (data.isCpuMetricDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getCpuDuration()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVESUM:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveDuration()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEAVERAGE:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveAverage()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMIN:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMin()));
			} else {
				return emptyStyledString;
			}
		case EXCLUSIVEMAX:
			if (data.isExclusiveTimeDataAvailable()) {
				return new StyledString(NumberFormatter.formatDouble(data.getExclusiveMax()));
			} else {
				return emptyStyledString;
			}
		default:
			return new StyledString("error");
		}
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
			List<String> values = new ArrayList<>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, methodIdent, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getObjectsToSearch(Object tableInput) {
		return timerDataList.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}

}
