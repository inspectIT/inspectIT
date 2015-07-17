package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.InvocationAwareDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.comparator.SqlStatementDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.TimerDataComparatorEnum;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.text.input.SqlStatementTextInputController.SqlHolderHelper;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.preferences.PreferencesConstants;
import info.novatec.inspectit.rcp.preferences.PreferencesUtils;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Input controller for the table view that is displayed below the aggregated SQL table.
 * <p>
 * Shows one statement aggregated on parameter basis.
 * 
 * @author Ivan Senic
 * 
 */
public class SqlParameterAggregationInputControler extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.sqlparameteraggregation";

	/**
	 * @author Ivan Senic
	 * 
	 */
	private static enum Column {
		/** The Parameters column. */
		PARAMETERS("Parameters", 600, null, SqlStatementDataComparatorEnum.PARAMETERS),
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
		DURATION("Duration (ms)", 80, null, TimerDataComparatorEnum.DURATION);

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
	 * Decimal places.
	 */
	private int timeDecimalPlaces = PreferencesUtils.getIntValue(PreferencesConstants.DECIMAL_PLACES);

	/**
	 * {@inheritDoc}
	 */
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
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferences.add(PreferenceId.CLEAR_BUFFER);
		}
		preferences.add(PreferenceId.TIME_RESOLUTION);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
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
	public Object getTableInput() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (data != null) {
			for (DefaultData defaultData : data) {
				if (!(defaultData instanceof SqlHolderHelper)) {
					return false;
				} else if (!((SqlHolderHelper) defaultData).isMaster()) {
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
	public IContentProvider getContentProvider() {
		return new SqlParameterContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new SqlLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		ICachedDataService cachedDataService = getInputDefinition().getRepositoryDefinition().getCachedDataService();
		TableViewerComparator<SqlStatementData> sqlViewerComparator = new TableViewerComparator<SqlStatementData>();
		for (Column column : Column.values()) {
			ResultComparator<SqlStatementData> resultComparator = new ResultComparator<SqlStatementData>(column.dataComparator, cachedDataService);
			sqlViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return sqlViewerComparator;
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
		}
		throw new RuntimeException("Could not create the human readable string!");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * We don't search the input directly.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object[] getObjectsToSearch(Object tableInput) {
		if (null != tableInput) {
			List<SqlStatementData> sqlStatementDatas = new ArrayList<>();
			// we can cast here safely cause only this can be input
			List<SqlHolderHelper> helpers = (List<SqlHolderHelper>) tableInput;
			for (SqlHolderHelper helper : helpers) {
				sqlStatementDatas.addAll(helper.getSqlStatementDataList());
			}
			return sqlStatementDatas.toArray(new SqlStatementData[sqlStatementDatas.size()]);
		}
		return new Object[0];
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
				values.add(getStyledTextForColumn(data, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		final StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			Object selected = selection.getFirstElement();
			if (selected instanceof SqlStatementData) {
				List<SqlStatementData> sqlList = new ArrayList<SqlStatementData>();
				sqlList.add((SqlStatementData) selected);
				passSqlWithParameters(sqlList);
			}
		}
	}

	/**
	 * Returns styled string for the column.
	 * 
	 * @param data
	 *            Data to return string for.
	 * @param enumId
	 *            Enumerated column.
	 * @return {@link StyledString}.
	 */
	private StyledString getStyledTextForColumn(SqlStatementData data, Column enumId) {
		switch (enumId) {
		case PARAMETERS:
			return new StyledString(TextFormatter.getSqlParametersText(data.getParameterValues()));
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
		default:
			return new StyledString("error");
		}
	}

	/**
	 * Passes the {@link SqlStatementData} to the bottom view that displays the SQL string.
	 * 
	 * @param sqlStatementDataList
	 *            List to pass.
	 */
	private void passSqlWithParameters(final List<SqlStatementData> sqlStatementDataList) {
		final SqlHolderHelper sqlHolderHelper = new SqlHolderHelper(sqlStatementDataList, false);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();
				IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
				if (null != rootEditor) {
					rootEditor.setDataInput(Collections.singletonList(sqlHolderHelper));
				}
			}
		});
	}

	/**
	 * The sql label provider used by this view.
	 * 
	 * @author Ivan Senic
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
			SqlStatementData data = (SqlStatementData) element;
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, enumId);
		}

	}

	/**
	 * @author Ivan Senic
	 * 
	 */
	private final class SqlParameterContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
			if (null == newInput || Objects.equals(newInput, Collections.emptyList())) {
				viewer.getControl().setEnabled(false);
			} else {
				List<SqlHolderHelper> helperList = (List<SqlHolderHelper>) newInput;
				final List<SqlStatementData> list = helperList.get(0).getSqlStatementDataList();
				if (null == list || Objects.equals(list, Collections.emptyList())) {
					viewer.getControl().setEnabled(false);
				} else {
					viewer.getControl().setEnabled(true);
					SqlParameterAggregationInputControler.this.passSqlWithParameters(list);
					StructuredSelection structuredSelection = new StructuredSelection(list.get(0));
					viewer.setSelection(structuredSelection, true);
				}
			}
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
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List && !((List<?>) inputElement).isEmpty()) {
				Object first = ((List<?>) inputElement).get(0);
				if (first instanceof SqlHolderHelper) {
					return ((SqlHolderHelper) first).getSqlStatementDataList().toArray();
				}
			}
			return new Object[0];
		}
	}

}
