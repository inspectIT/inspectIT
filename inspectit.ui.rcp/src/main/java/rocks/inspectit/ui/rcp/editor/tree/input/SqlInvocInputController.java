package rocks.inspectit.ui.rcp.editor.tree.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.cs.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.comparator.IDataComparator;
import rocks.inspectit.shared.cs.communication.comparator.SqlStatementDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.comparator.TimerDataComparatorEnum;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.AggregationPerformer;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.SqlStatementDataAggregator;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.IPreferenceGroup;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.tree.TreeViewerComparator;
import rocks.inspectit.ui.rcp.editor.tree.util.DatabaseSqlTreeComparator;
import rocks.inspectit.ui.rcp.editor.tree.util.TraceTreeData;
import rocks.inspectit.ui.rcp.editor.viewers.RawAggregatedResultComparator;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.handlers.ShowHideColumnsHandler;
import rocks.inspectit.ui.rcp.util.data.DatabaseInfoHelper;

/**
 * This input controller displays the contents of {@link SqlStatementData} objects in an invocation
 * sequence.
 *
 * @author Patrice Bouillet
 *
 */
public class SqlInvocInputController extends AbstractTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.sqlinvoc";

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
		/** The column containing the name of the database. */
		DATABASE_URL("Database URL", 120, null, true, true, null),
		/** The statement column. */
		STATEMENT("Statement", 600, InspectITImages.IMG_DATABASE, true, true, SqlStatementDataComparatorEnum.SQL_AND_PARAMETERS),
		/** The count column. */
		COUNT("Count", 80, null, true, false, TimerDataComparatorEnum.COUNT),
		/** The average column. */
		AVERAGE("Avg (ms)", 80, null, true, false, TimerDataComparatorEnum.AVERAGE),
		/** The min column. */
		MIN("Min (ms)", 80, null, true, false, TimerDataComparatorEnum.MIN),
		/** The max column. */
		MAX("Max (ms)", 80, null, true, false, TimerDataComparatorEnum.MAX),
		/** The duration column. */
		DURATION("Duration (ms)", 80, null, true, true, TimerDataComparatorEnum.DURATION),
		/** The prepared column. */
		PREPARED("Prepared?", 60, null, false, true, SqlStatementDataComparatorEnum.IS_PREPARED_STATEMENT);

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
		private IDataComparator<? super SqlStatementData> dataComparator;

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
		private Column(String name, int width, String imageName, boolean showInAggregatedMode, boolean showInRawMode, IDataComparator<? super SqlStatementData> dataComparator) {
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
	 * List that is displayed after processing the invocation.
	 */
	private List<SqlStatementData> sqlStatementDataList;

	/**
	 * Map of SQls and the database they belong to.
	 */
	private Map<DatabaseInfoHelper, List<SqlStatementData>> databaseSqlMap;

	/**
	 * Empty styled string.
	 */
	private final StyledString emptyStyledString = new StyledString();

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
	public void createColumns(TreeViewer treeViewer) {
		for (Column column : Column.values()) {
			TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
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
			mapTreeViewerColumn(column, viewerColumn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canAlterColumnWidth(TreeColumn treeColumn) {
		for (Column column : Column.values()) {
			if (Objects.equals(getMappedTreeViewerColumn(column).getColumn(), treeColumn)) {
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
					getMappedTreeViewerColumn(column).getColumn().setWidth((null != width) ? width.intValue() : column.width);
				} else if (!column.showInRawMode && column.showInAggregatedMode) {
					getMappedTreeViewerColumn(column).getColumn().setWidth(0);
				}
			} else {
				if (!column.showInRawMode && column.showInAggregatedMode && !ShowHideColumnsHandler.isColumnHidden(this.getClass(), column.name)) {
					Integer width = ShowHideColumnsHandler.getRememberedColumnWidth(this.getClass(), column.name);
					getMappedTreeViewerColumn(column).getColumn().setWidth((null != width) ? width.intValue() : column.width);
				} else if (column.showInRawMode && !column.showInAggregatedMode) {
					getMappedTreeViewerColumn(column).getColumn().setWidth(0);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new SqlInvocContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		TreeViewerComparator<SqlStatementData> sqlInputViewerComparator = new DatabaseSqlTreeComparator();
		for (Column column : Column.values()) {
			RawAggregatedResultComparator<SqlStatementData> comparator = new RawAggregatedResultComparator<SqlStatementData>(column.dataComparator, cachedDataService, column.showInRawMode,
					column.showInAggregatedMode) {
				@Override
				protected boolean isRawMode() {
					return rawMode;
				}
			};
			sqlInputViewerComparator.addColumn(getMappedTreeViewerColumn(column).getColumn(), comparator);
		}

		return sqlInputViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new SqlInvocLabelProvider();
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

		// we accept one invocation sequence
		if (data.get(0) instanceof InvocationSequenceData) {
			return true;
		}

		// or one trace data
		if (data.get(0) instanceof TraceTreeData) {
			return true;
		}

		// or list of SQLs
		if (data.get(0) instanceof SqlStatementData) {
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(IProgressMonitor monitor) {
	}

	/**
	 * The content provider for this view.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private final class SqlInvocContentProvider implements ITreeContentProvider {

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

			if (input.get(0) instanceof InvocationSequenceData) {
				sqlStatementDataList = getRawInputList((List<InvocationSequenceData>) input, new ArrayList<SqlStatementData>());
			} else if (input.get(0) instanceof TraceTreeData) {
				TraceTreeData traceTreeData = (TraceTreeData) input.get(0);
				sqlStatementDataList = getRawInputList(TraceTreeData.collectInvocations(traceTreeData, new ArrayList<InvocationSequenceData>()), new ArrayList<SqlStatementData>());
			} else {
				sqlStatementDataList = (List<SqlStatementData>) input;
			}
			if (!rawMode) {
				AggregationPerformer<SqlStatementData> aggregationPerformer = new AggregationPerformer<>(new SqlStatementDataAggregator());
				aggregationPerformer.processCollection(sqlStatementDataList);
				sqlStatementDataList = aggregationPerformer.getResultList();
				databaseSqlMap = createInputMap(sqlStatementDataList);
				return databaseSqlMap.keySet().toArray();
			} else {
				Collections.sort(sqlStatementDataList, new Comparator<SqlStatementData>() {
					@Override
					public int compare(SqlStatementData o1, SqlStatementData o2) {
						return o1.getTimeStamp().compareTo(o2.getTimeStamp());
					}
				});
				return sqlStatementDataList.toArray();
			}

		}

		/**
		 * Create input map from list of {@link SqlStatementData}s.
		 *
		 * @param sqlStatementDatas
		 *            {@link SqlStatementData}s
		 * @return Input map
		 */
		private Map<DatabaseInfoHelper, List<SqlStatementData>> createInputMap(List<SqlStatementData> sqlStatementDatas) {
			Map<DatabaseInfoHelper, List<SqlStatementData>> map = new HashMap<>();
			for (SqlStatementData sqlStatementData : sqlStatementDatas) {
				DatabaseInfoHelper helper = new DatabaseInfoHelper(sqlStatementData);
				List<SqlStatementData> list = map.get(helper);
				if (null == list) {
					list = new ArrayList<>();
					map.put(helper, list);
				}
				list.add(sqlStatementData);
			}
			return map;
		}

		/**
		 * Returns the raw list, with no aggregation.
		 *
		 * @param invocationSequenceDataList
		 *            Input as list of invocations
		 * @param sqlStatementDataList
		 *            List where results will be stored. Needed because of reflection. Note that
		 *            this list will be returned as the result.
		 * @return List of raw order SQL data.
		 */
		private List<SqlStatementData> getRawInputList(List<InvocationSequenceData> invocationSequenceDataList, List<SqlStatementData> sqlStatementDataList) {
			for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
				if (null != invocationSequenceData.getSqlStatementData()) {
					sqlStatementDataList.add(invocationSequenceData.getSqlStatementData());
				}
				if ((null != invocationSequenceData.getNestedSequences()) && !invocationSequenceData.getNestedSequences().isEmpty()) {
					getRawInputList(invocationSequenceData.getNestedSequences(), sqlStatementDataList);
				}
			}

			return sqlStatementDataList;
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

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof DatabaseInfoHelper) {
				List<SqlStatementData> children = databaseSqlMap.get(parentElement);
				if (CollectionUtils.isNotEmpty(children)) {
					return children.toArray();
				}
			}
			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getParent(Object element) {
			if (!rawMode && (element instanceof SqlStatementData)) {
				return new DatabaseInfoHelper((SqlStatementData) element);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasChildren(Object element) {
			return element instanceof DatabaseInfoHelper;
		}

	}

	/**
	 * The sql label provider used by this view.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private final class SqlInvocLabelProvider extends StyledCellIndexLabelProvider {

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
			Column enumId = Column.fromOrd(index);

			if (element instanceof SqlStatementData) {
				return getStyledTextForColumn((SqlStatementData) element, enumId);
			} else if (element instanceof DatabaseInfoHelper) {
				return getDatabaseStyledTextForColumn((DatabaseInfoHelper) element, enumId);
			} else {
				return emptyStyledString;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getToolTipText(Object element, int index) {
			if (element instanceof DatabaseInfoHelper) {
				DatabaseInfoHelper helper = (DatabaseInfoHelper) element;
				return helper.getLongText();
			}
			return super.getToolTipText(element, index);
		}

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
	private StyledString getStyledTextForColumn(SqlStatementData data, Column enumId) {
		switch (enumId) {
		case DATABASE_URL:
			if (rawMode) {
				if (StringUtils.isNotEmpty(data.getDatabaseUrl())) {
					return new StyledString(data.getDatabaseUrl());
				} else {
					return new StyledString("Unknown");
				}
			} else {
				return emptyStyledString;
			}
		case TIMESTAMP:
			if (rawMode) {
				return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
			} else {
				return emptyStyledString;
			}
		case STATEMENT:
			if (rawMode) {
				String sql = TextFormatter.clearLineBreaks(data.getSqlWithParameterValues());
				return new StyledString(sql);
			} else {
				String sql = TextFormatter.clearLineBreaks(data.getSql());
				return new StyledString(sql);
			}
		case COUNT:
			return new StyledString(Long.toString(data.getCount()));
		case AVERAGE:
			return new StyledString(NumberFormatter.formatDouble(data.getAverage()));
		case MIN:
			return new StyledString(NumberFormatter.formatDouble(data.getMin()));
		case MAX:
			return new StyledString(NumberFormatter.formatDouble(data.getMax()));
		case DURATION:
			return new StyledString(NumberFormatter.formatDouble(data.getDuration()));
		case PREPARED:
			if (rawMode) {
				return new StyledString(Boolean.toString(data.isPreparedStatement()));
			} else {
				return emptyStyledString;
			}
		default:
			return new StyledString("error");
		}
	}

	/**
	 * Returns the styled text for a specific column.
	 *
	 * @param data
	 *            {@link DatabaseInfoHelper}.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getDatabaseStyledTextForColumn(DatabaseInfoHelper data, Column enumId) {
		switch (enumId) {
		case DATABASE_URL:
			return new StyledString(data.getDatabaseUrl());
		default:
			return emptyStyledString;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getExpandLevel() {
		return AbstractTreeViewer.ALL_LEVELS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReadableString(Object object) {
		if (object instanceof SqlStatementData) {
			SqlStatementData data = (SqlStatementData) object;
			StringBuilder sb = new StringBuilder();
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, column).toString());
				sb.append('\t');
			}
			return sb.toString();
		} else if (object instanceof DatabaseInfoHelper) {
			DatabaseInfoHelper data = (DatabaseInfoHelper) object;
			StringBuilder sb = new StringBuilder();
			for (Column column : Column.values()) {
				sb.append(getDatabaseStyledTextForColumn(data, column).toString());
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
		if (object instanceof SqlStatementData) {
			SqlStatementData data = (SqlStatementData) object;
			List<String> values = new ArrayList<>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, column).toString());
			}
			return values;
		} else if (object instanceof DatabaseInfoHelper) {
			DatabaseInfoHelper data = (DatabaseInfoHelper) object;
			List<String> values = new ArrayList<>();
			for (Column column : Column.values()) {
				values.add(getDatabaseStyledTextForColumn(data, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getObjectsToSearch(Object treeInput) {
		return sqlStatementDataList.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}

}
