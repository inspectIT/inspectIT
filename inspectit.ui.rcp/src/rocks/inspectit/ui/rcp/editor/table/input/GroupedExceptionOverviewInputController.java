package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.IExceptionDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.comparator.AggregatedExceptionSensorDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ExceptionSensorDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.InvocationAwareDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class GroupedExceptionOverviewInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.groupedexceptionoverview";

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static enum Column {
		/** The class column. */
		FQN("Fully-Qualified Name", 450, InspectITImages.IMG_CLASS, ExceptionSensorDataComparatorEnum.FQN),
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 120, InspectITImages.IMG_INVOCATION, InvocationAwareDataComparatorEnum.INVOCATION_AFFILIATION),
		/** The CREATED column. */
		CREATED("Created", 70, null, AggregatedExceptionSensorDataComparatorEnum.CREATED),
		/** The RETHROWN column. */
		RETHROWN("Rethrown", 70, null, AggregatedExceptionSensorDataComparatorEnum.RETHROWN),
		/** The HANDLED column. */
		HANDLED("Handled", 70, null, AggregatedExceptionSensorDataComparatorEnum.HANDLED);

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;
		/** Comparator for the column. */
		private IDataComparator<? super AggregatedExceptionSensorData> dataComparator;

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
		private Column(String name, int width, String imageName, IDataComparator<? super AggregatedExceptionSensorData> dataComparator) {
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
	 * The template object which is send to the server.
	 */
	private ExceptionSensorData template;

	/**
	 * Indicates from which point in time data should be shown.
	 */
	private Date fromDate;

	/**
	 * Indicates until which point in time data should be shown.
	 */
	private Date toDate;

	/**
	 * The list of {@link ExceptionSensorData} objects which is displayed.
	 */
	private List<AggregatedExceptionSensorData> exceptionSensorDataList = new ArrayList<AggregatedExceptionSensorData>();

	/**
	 * This map holds all objects that are needed to be represented in this view. It uses the fqn of
	 * an exception as the key. It contains as value the objects that are belonging to a specific
	 * exception class.
	 */
	private Map<String, List<AggregatedExceptionSensorData>> overviewMap;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private IExceptionDataAccessService dataAccessService;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new ExceptionSensorData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());

		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.EXCEPTION_TYPE_EXTRAS_MARKER)) {
			String throwableType = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.EXCEPTION_TYPE_EXTRAS_MARKER).getThrowableType();
			template.setThrowableType(throwableType);
		}

		dataAccessService = inputDefinition.getRepositoryDefinition().getExceptionDataAccessService();
	}

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
	public Object getTableInput() {
		// this list will be filled with data
		return exceptionSensorDataList;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new GroupedExceptionOverviewContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new GroupedExceptionOverviewLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public ViewerComparator getComparator() {
		ICachedDataService cachedDataService = getInputDefinition().getRepositoryDefinition().getCachedDataService();
		TableViewerComparator<AggregatedExceptionSensorData> exceptionOverviewViewerComparator = new TableViewerComparator<AggregatedExceptionSensorData>();
		for (Column column : Column.values()) {
			ResultComparator<AggregatedExceptionSensorData> resultComparator = new ResultComparator<AggregatedExceptionSensorData>(column.dataComparator, cachedDataService);
			exceptionOverviewViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return exceptionOverviewViewerComparator;
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
		preferences.add(PreferenceId.TIMELINE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (data.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
		case TIMELINE:
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			if (preferenceMap.containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				fromDate = (Date) preferenceMap.get(PreferenceId.TimeLine.FROM_DATE_ID);
			}
			if (preferenceMap.containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				toDate = (Date) preferenceMap.get(PreferenceId.TimeLine.TO_DATE_ID);
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
	@SuppressWarnings("unchecked")
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Updating Grouped Exception Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the Grouped Exception Overview");
		List<AggregatedExceptionSensorData> ungroupedList = null;

		// if fromDate and toDate are set, then we retrieve only the data for
		// this time interval
		if (null != fromDate && null != toDate) {
			ungroupedList = dataAccessService.getDataForGroupedExceptionOverview(template, fromDate, toDate);
		} else {
			ungroupedList = dataAccessService.getDataForGroupedExceptionOverview(template);
		}

		List<AggregatedExceptionSensorData> groupedOverviewList = new ArrayList<AggregatedExceptionSensorData>();
		overviewMap = new HashMap<String, List<AggregatedExceptionSensorData>>();

		for (AggregatedExceptionSensorData ungroupedObject : ungroupedList) {
			List<AggregatedExceptionSensorData> groupedObjects = Collections.EMPTY_LIST;
			if (!overviewMap.containsKey(ungroupedObject.getThrowableType())) {
				// map doesn't contain the actual exception class, so we create
				// and add a new list for exception classes of the same type
				groupedObjects = new ArrayList<AggregatedExceptionSensorData>();
				groupedObjects.add(ungroupedObject);
				overviewMap.put(ungroupedObject.getThrowableType(), groupedObjects);
			} else {
				// map contains the actual exception class, so we get the list
				// and search for the object within the list where the counter
				// values must be updated
				groupedObjects = overviewMap.get(ungroupedObject.getThrowableType());
				groupedObjects.add(ungroupedObject);
			}
		}

		// we are creating the list that contains all object to be shown in the
		// overview
		for (Map.Entry<String, List<AggregatedExceptionSensorData>> entry : overviewMap.entrySet()) {
			String throwableType = entry.getKey();
			AggregatedExceptionSensorData data = createObjectForOverview(throwableType, entry.getValue());
			groupedOverviewList.add(data);
		}

		if (null != groupedOverviewList) {
			exceptionSensorDataList.clear();
			monitor.subTask("Displaying the Exception Overview");
			exceptionSensorDataList.addAll(groupedOverviewList);
		}

		monitor.done();
	}

	/**
	 * Creates the {@link AggregatedExceptionSensorData} object for the table input from the list of
	 * same type aggregated objects.
	 * 
	 * @param throwableType
	 *            Throwable type.
	 * @param dataList
	 *            List of {@link AggregatedExceptionSensorData}
	 * @return Aggregated data for the table.
	 */
	private AggregatedExceptionSensorData createObjectForOverview(String throwableType, List<AggregatedExceptionSensorData> dataList) {
		AggregatedExceptionSensorData data = new AggregatedExceptionSensorData();
		data.setThrowableType(throwableType);

		for (AggregatedExceptionSensorData object : dataList) {
			if (data.getPlatformIdent() == 0) {
				data.setPlatformIdent(object.getPlatformIdent());
			}
			data.aggregateExceptionData(object);
		}

		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		final StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
					public void run(final IProgressMonitor monitor) {
						monitor.beginTask("Retrieving Exception Messages", IProgressMonitor.UNKNOWN);
						AggregatedExceptionSensorData data = (AggregatedExceptionSensorData) selection.getFirstElement();
						final List<AggregatedExceptionSensorData> dataList = (List<AggregatedExceptionSensorData>) overviewMap.get(data.getThrowableType());

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								IWorkbenchPage page = window.getActivePage();
								IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
								rootEditor.setDataInput(dataList);
							}
						});
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
	 * The label provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private final class GroupedExceptionOverviewLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			AggregatedExceptionSensorData data = (AggregatedExceptionSensorData) element;
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, enumId);
		}

	}

	/**
	 * The content provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static final class GroupedExceptionOverviewContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<AggregatedExceptionSensorData> exceptionSensorData = (List<AggregatedExceptionSensorData>) inputElement;
			return exceptionSensorData.toArray();
		}

		/**
		 * {@inheritDoc}
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose() {
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
	private StyledString getStyledTextForColumn(AggregatedExceptionSensorData data, Column enumId) {
		switch (enumId) {
		case FQN:
			return new StyledString(data.getThrowableType());
		case INVOCATION_AFFILLIATION:
			int percentage = (int) (data.getInvocationAffiliationPercentage() * 100);
			int invocations = 0;
			if (null != data.getInvocationParentsIdSet()) {
				invocations = data.getInvocationParentsIdSet().size();
			}
			return TextFormatter.getInvocationAffilliationPercentageString(percentage, invocations);
		case CREATED:
			return new StyledString(String.valueOf(data.getCreated()));
		case RETHROWN:
			return new StyledString(String.valueOf(data.getPassed()));
		case HANDLED:
			return new StyledString(String.valueOf(data.getHandled()));
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
		if (object instanceof AggregatedExceptionSensorData) {
			AggregatedExceptionSensorData data = (AggregatedExceptionSensorData) object;
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
		if (object instanceof AggregatedExceptionSensorData) {
			AggregatedExceptionSensorData data = (AggregatedExceptionSensorData) object;
			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

}
