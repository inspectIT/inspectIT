package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.ISqlDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.InvocationAwareDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.comparator.SqlStatementDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.TimerDataComparatorEnum;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.SqlStatementInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.LiveMode;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.text.input.SqlStatementTextInputController.SqlHolderHelper;
import info.novatec.inspectit.rcp.editor.tree.TreeViewerComparator;
import info.novatec.inspectit.rcp.editor.tree.util.DatabaseSqlTreeComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.util.data.DatabaseInfoHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * This input controller displays the contents of {@link SqlStatementData} objects.
 * 
 * @author Patrice Bouillet
 * 
 */
public class SqlInputController extends AbstractTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.sql";

	/**
	 * Empty {@link StyledString}.
	 */
	private static final StyledString EMPTY_STYLED_STRING = new StyledString("");

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private static enum Column {

		/** The column containing the name of the database. */
		DATABASE_URL("Database URL", 120, null, null),
		/** The statement column. */
		STATEMENT("Statement", 600, InspectITImages.IMG_DATABASE, SqlStatementDataComparatorEnum.SQL),
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 120, InspectITImages.IMG_INVOCATION, InvocationAwareDataComparatorEnum.INVOCATION_AFFILIATION),
		/** The count column. */
		COUNT("Count", 80, null, TimerDataComparatorEnum.COUNT),
		/** The average column. */
		AVERAGE("Avg (ms)", 80, null, TimerDataComparatorEnum.AVERAGE),
		/** The min column. */
		MIN("Min (ms)", 80, null, TimerDataComparatorEnum.MIN),
		/** The max column. */
		MAX("Max (ms)", 80, null, TimerDataComparatorEnum.MAX),
		/** The duration column. */
		DURATION("Duration (ms)", 80, null, TimerDataComparatorEnum.MAX),
		/** The prepared column. */
		PREPARED("Prepared?", 80, null, SqlStatementDataComparatorEnum.IS_PREPARED_STATEMENT);

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;
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
		 * @param dataComparator
		 *            Comparator for the column.
		 */
		private Column(String name, int width, String imageName, IDataComparator<? super SqlStatementData> dataComparator) {
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
	 * The template which is send to the Repository to retrieve the actual data.
	 */
	private SqlStatementData template;

	/**
	 * Input map.
	 */
	private Map<DatabaseInfoHelper, List<SqlStatementData>> inputMap = new HashMap<>();

	/**
	 * The data access service to access the data on the CMR.
	 */
	private ISqlDataAccessService dataAccessService;

	/**
	 * The cached service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

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

		template = new SqlStatementData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setId(-1);

		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.SQL_STATEMENT_EXTRAS_MARKER)) {
			SqlStatementInputDefinitionExtra inputDefinitionExtra = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.SQL_STATEMENT_EXTRAS_MARKER);
			template.setSql(inputDefinitionExtra.getSql());
		}

		dataAccessService = inputDefinition.getRepositoryDefinition().getSqlDataAccessService();
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createColumns(TreeViewer treeViewer) {
		for (Column column : Column.values()) {
			TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText(column.name);
			viewerColumn.getColumn().setWidth(column.width);
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
	public Object getTreeInput() {
		return inputMap.keySet();
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new SqlContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new SqlLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public ViewerComparator getComparator() {
		TreeViewerComparator<SqlStatementData> sqlViewerComparator = new DatabaseSqlTreeComparator();
		for (Column column : Column.values()) {
			if (null != column.dataComparator) {
				ResultComparator<SqlStatementData> resultComparator = new ResultComparator<SqlStatementData>(column.dataComparator, cachedDataService);
				sqlViewerComparator.addColumn(getMappedTreeViewerColumn(column).getColumn(), resultComparator);
			}
		}

		return sqlViewerComparator;
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
		if (data != null) {
			for (DefaultData defaultData : data) {
				if (!(defaultData instanceof SqlStatementData)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh(IProgressMonitor monitor, final IRootEditor rootEditor) {
		monitor.beginTask("Getting SQL information", IProgressMonitor.UNKNOWN);
		List<SqlStatementData> sqlStatementList;
		if (autoUpdate) {
			sqlStatementList = dataAccessService.getAggregatedSqlStatements(template);
		} else {
			sqlStatementList = dataAccessService.getAggregatedSqlStatements(template, fromDate, toDate);
		}

		inputMap.clear();
		if (CollectionUtils.isNotEmpty(sqlStatementList)) {
			inputMap.putAll(createInputMap(sqlStatementList));
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (null != rootEditor) {
					rootEditor.setDataInput(Collections.<DefaultData> emptyList());
				}
			}
		});
		monitor.done();
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
	 * The sql label provider used by this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SqlLabelProvider extends StyledCellIndexLabelProvider {

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
				return getSqlStyledTextForColumn((SqlStatementData) element, enumId);
			} else if (element instanceof DatabaseInfoHelper) {
				return getDatabaseStyledTextForColumn((DatabaseInfoHelper) element, enumId);
			}
			return EMPTY_STYLED_STRING;
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
	 * The sql content provider used by this view.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	private final class SqlContentProvider extends ArrayContentProvider implements ITreeContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof DatabaseInfoHelper) {
				return inputMap.get(parentElement).toArray();
			}
			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getParent(Object element) {
			if (element instanceof SqlStatementData) {
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
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		final StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (!selection.isEmpty() && selection.getFirstElement() instanceof SqlStatementData) {
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
					public void run(final IProgressMonitor monitor) {
						monitor.beginTask("Retrieving Parameter Aggregated SQLs", IProgressMonitor.UNKNOWN);
						SqlStatementData data = (SqlStatementData) selection.getFirstElement();
						List<SqlStatementData> dataList = Collections.emptyList();
						boolean hasNoParameters = !data.isPreparedStatement();
						if (data.isPreparedStatement()) {
							dataList = dataAccessService.getParameterAggregatedSqlStatements(data, fromDate, toDate);

							// if we have only one statement and it has no parameters, we won't load
							// the bottom part with empty parameters
							if (dataList.size() == 1 && CollectionUtils.isEmpty(dataList.get(0).getParameterValues())) {
								hasNoParameters = true;
							}
						}

						if (hasNoParameters) {
							final SqlHolderHelper inputForParametersTable = new SqlHolderHelper(Collections.<SqlStatementData> emptyList(), true);
							final SqlHolderHelper inputForTextOnly = new SqlHolderHelper(Collections.singletonList(data), false);
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
									IWorkbenchPage page = window.getActivePage();
									IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
									if (null != rootEditor) {
										rootEditor.setDataInput(Collections.singletonList(inputForParametersTable));
										rootEditor.setDataInput(Collections.singletonList(inputForTextOnly));
									}
								}
							});

						} else {
							final SqlHolderHelper inputForParametersTable = new SqlHolderHelper(dataList, true);
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
									IWorkbenchPage page = window.getActivePage();
									IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
									if (null != rootEditor) {
										rootEditor.setDataInput(Collections.singletonList(inputForParametersTable));
									}
								}
							});
						}
						monitor.done();
					}
				});
			} catch (InvocationTargetException e) {
				MessageDialog.openError(Display.getDefault().getActiveShell().getShell(), "Error", e.getCause().toString());
			} catch (InterruptedException e) {
				MessageDialog.openInformation(Display.getDefault().getActiveShell().getShell(), "Cancelled", e.getCause().toString());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getExpandLevel() {
		return TreeViewer.ALL_LEVELS;
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
	private StyledString getSqlStyledTextForColumn(SqlStatementData data, Column enumId) {
		switch (enumId) {
		case STATEMENT:
			String sql = data.getSql().replaceAll("[\r\n]+", " ");
			return new StyledString(sql);
		case INVOCATION_AFFILLIATION:
			int percentage = (int) (data.getInvocationAffiliationPercentage() * 100);
			int invocations = 0;
			if (null != data.getInvocationParentsIdSet()) {
				invocations = data.getInvocationParentsIdSet().size();
			}
			return TextFormatter.getInvocationAffilliationPercentageString(percentage, invocations);
		case COUNT:
			return new StyledString(Long.toString(data.getCount()));
		case AVERAGE:
			return new StyledString(NumberFormatter.formatDouble(data.getAverage(), timeDecimalPlaces));
		case MIN:
			return new StyledString(NumberFormatter.formatDouble(data.getMin(), timeDecimalPlaces));
		case MAX:
			return new StyledString(NumberFormatter.formatDouble(data.getMax(), timeDecimalPlaces));
		case DURATION:
			return new StyledString(NumberFormatter.formatDouble(data.getDuration(), timeDecimalPlaces));
		case PREPARED:
			if (data.isPreparedStatement()) {
				return new StyledString("true");
			} else {
				return new StyledString("false");
			}
		default:
			return EMPTY_STYLED_STRING;
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
			return EMPTY_STYLED_STRING;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
		if (object instanceof SqlStatementData) {
			SqlStatementData data = (SqlStatementData) object;
			StringBuilder sb = new StringBuilder();
			for (Column column : Column.values()) {
				sb.append(getSqlStyledTextForColumn(data, column).toString());
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
			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getSqlStyledTextForColumn(data, column).toString());
			}
			return values;
		} else if (object instanceof DatabaseInfoHelper) {
			DatabaseInfoHelper data = (DatabaseInfoHelper) object;
			List<String> values = new ArrayList<String>();
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
		List<SqlStatementData> sqlStatementDatas = new ArrayList<>();
		for (List<SqlStatementData> datas : inputMap.values()) {
			sqlStatementDatas.addAll(datas);
		}
		return sqlStatementDatas.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		inputMap.clear();
	}
}
