package rocks.inspectit.ui.rcp.editor.table.input;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
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

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.IDataComparator;
import rocks.inspectit.shared.all.communication.comparator.InvocationAwareDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.MethodSensorDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.cs.cmr.service.IExceptionDataAccessService;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.preferences.IPreferenceGroup;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.editor.table.RemoteTableViewerComparator;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class UngroupedExceptionOverviewInputController extends AbstractTableInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.table.ungroupedexceptionoverview";

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static enum Column {
		/** The count column. */
		TIMESTAMP("Timestamp", 150, InspectITImages.IMG_TIMESTAMP, DefaultDataComparatorEnum.TIMESTAMP),
		/** The class column. */
		CLASS("Class", 250, InspectITImages.IMG_CLASS, MethodSensorDataComparatorEnum.CLASS),
		/** The package column. */
		PACKAGE("Package", 250, InspectITImages.IMG_PACKAGE, MethodSensorDataComparatorEnum.PACKAGE),
		/** Invocation Affiliation. */
		INVOCATION_AFFILLIATION("In Invocations", 120, InspectITImages.IMG_INVOCATION, InvocationAwareDataComparatorEnum.INVOCATION_AFFILIATION);

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;
		/** Comparator for the column. */
		private IDataComparator<? super ExceptionSensorData> dataComparator;

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
		private Column(String name, int width, String imageName, IDataComparator<? super ExceptionSensorData> dataComparator) {
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
	 * Default comparator when no sorting is defined.
	 */
	private final ResultComparator<ExceptionSensorData> defaultComparator = new ResultComparator<>(DefaultDataComparatorEnum.TIMESTAMP, false);

	/**
	 * The template object which is send to the server.
	 */
	private ExceptionSensorData template;

	/**
	 * The list of invocation sequence data objects which is displayed.
	 */
	private List<ExceptionSensorData> exceptionSensorData = new ArrayList<ExceptionSensorData>();

	/**
	 * The limit of the result set.
	 */
	private int limit = PreferencesUtils.getIntValue(PreferencesConstants.ITEMS_COUNT_TO_SHOW);;

	/**
	 * Indicates from which point in time data should be shown.
	 */
	private Date fromDate;

	/**
	 * Indicates until which point in time data should be shown.
	 */
	private Date toDate;

	/**
	 * This data access service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * The data access service to access the data on the CMR.
	 */
	private IExceptionDataAccessService dataAccessService;

	/**
	 * Result comparator to be used on the server.
	 */
	private ResultComparator<ExceptionSensorData> resultComparator = defaultComparator;

	/**
	 * {@inheritDoc}
	 */
	@Override
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

		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
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
		return exceptionSensorData;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new UngroupedExceptionOverviewContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new UngroupedExceptionOverviewLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public ViewerComparator getComparator() {
		RemoteTableViewerComparator<ExceptionSensorData> exceptionViewerComparator = new RemoteTableViewerComparator<ExceptionSensorData>() {
			@Override
			protected void sortRemotely(ResultComparator<ExceptionSensorData> resultComparator) {
				if (null != resultComparator) {
					UngroupedExceptionOverviewInputController.this.resultComparator = resultComparator;
				} else {
					UngroupedExceptionOverviewInputController.this.resultComparator = defaultComparator;
				}
				loadDataFromService();
			}
		};
		for (Column column : Column.values()) {
			// since it is remote sorting we do not provide local cached data service
			ResultComparator<ExceptionSensorData> resultComparator = new ResultComparator<ExceptionSensorData>(column.dataComparator);
			exceptionViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return exceptionViewerComparator;
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
		preferences.add(PreferenceId.ITEMCOUNT);
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
	public void setLimit(int limit) {
		this.limit = limit;
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
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Updating the Ungrouped Exception Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the Ungrouped Exception Overview");

		loadDataFromService();

		monitor.done();
	}

	/**
	 * Reloads the data from the service.
	 */
	private void loadDataFromService() {
		List<ExceptionSensorData> exData = null;

		// if fromDate and toDate are set, then we retrieve only the data for
		// this time interval
		if (null != fromDate && null != toDate) {
			exData = dataAccessService.getUngroupedExceptionOverview(template, limit, fromDate, toDate, resultComparator);
		} else {
			exData = dataAccessService.getUngroupedExceptionOverview(template, limit, resultComparator);
		}
		exceptionSensorData.clear();

		if (null != exData) {
			exceptionSensorData.addAll(exData);
		}

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
						monitor.beginTask("Retrieving Exception Tree", IProgressMonitor.UNKNOWN);
						ExceptionSensorData data = (ExceptionSensorData) selection.getFirstElement();
						List<ExceptionSensorData> exceptionSensorDataList = dataAccessService.getExceptionTree(data);
						final List<ExceptionSensorData> finalSensorDataList = new ArrayList<ExceptionSensorData>();

						finalSensorDataList.add(exceptionSensorDataList.get(0));

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								IWorkbenchPage page = window.getActivePage();
								IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
								rootEditor.setDataInput(finalSensorDataList);
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
	private final class UngroupedExceptionOverviewLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			ExceptionSensorData data = (ExceptionSensorData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
		}

	}

	/**
	 * The content provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static final class UngroupedExceptionOverviewContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<ExceptionSensorData> exceptionSensorData = (List<ExceptionSensorData>) inputElement;
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
	 * @param methodIdent
	 *            The method ident object.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(ExceptionSensorData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case PACKAGE:
			if (methodIdent.getPackageName() != null && !methodIdent.getPackageName().equals("")) {
				return new StyledString(methodIdent.getPackageName());
			} else {
				return new StyledString("(default)");
			}
		case CLASS:
			return new StyledString(methodIdent.getClassName());
		case TIMESTAMP:
			return new StyledString(NumberFormatter.formatTime(data.getTimeStamp()));
		case INVOCATION_AFFILLIATION:
			int percentage = (int) (data.getInvocationAffiliationPercentage() * 100);
			int invocations = 0;
			if (null != data.getInvocationParentsIdSet()) {
				invocations = data.getInvocationParentsIdSet().size();
			}
			return TextFormatter.getInvocationAffilliationPercentageString(percentage, invocations);
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
		if (object instanceof ExceptionSensorData) {
			ExceptionSensorData data = (ExceptionSensorData) object;
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
		if (object instanceof ExceptionSensorData) {
			ExceptionSensorData data = (ExceptionSensorData) object;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, methodIdent, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

}
