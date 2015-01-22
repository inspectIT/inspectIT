package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.IRemoteCallDataAccessService;
import info.novatec.inspectit.communication.comparator.DefaultDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.MethodSensorDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.RemoteCallDataComaratorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.tooltip.IColumnToolTipProvider;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
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
 * This input controller displays an overview of {@link RemoteCallData} objects.
 * 
 * @author Thomas Kluge
 * 
 */
public class UngroupedRemoteCallOverviewDataInputController extends AbstractTableInputController {

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Thomas Kluge
	 * 
	 */
	private static enum Column {

		/** The time column. */
		PLATFORMIDENT("Platform ID", 150, InspectITImages.IMG_PACKAGE, DefaultDataComparatorEnum.PLATFORM_IDENT),
		/** The time column. */
		TIME("Start Time", 150, InspectITImages.IMG_TIMER, DefaultDataComparatorEnum.TIMESTAMP),
		/** The method column. */
		METHOD("Method", 550, InspectITImages.IMG_METHOD_PUBLIC, MethodSensorDataComparatorEnum.METHOD),
		/** The method column. */
		CALLING("Calling", 100, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComaratorEnum.IS_CALLING),
		/** The method column. */
		IDENTIFICATION("Identification", 150, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComaratorEnum.IDENTIFICATION),
		/** The method column. */
		URL("URL", 550, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComaratorEnum.IDENTIFICATION),
		/** The method column. */
		RESPONSECODE("Response Code", 100, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComaratorEnum.IDENTIFICATION);

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

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;

		/** Comparator for the column. */
		private IDataComparator<? super RemoteCallData> dataComparator;

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
		private Column(String name, int width, String imageName, IDataComparator<? super RemoteCallData> dataComparator) {
			this.name = name;
			this.width = width;
			this.image = InspectIT.getDefault().getImage(imageName);
			this.dataComparator = dataComparator;
		}

	}

	/**
	 * The label provider for this view.
	 * 
	 * @author Thomas Kluge
	 * 
	 */
	private final class RemoteCallDataLabelProvider extends StyledCellIndexLabelProvider implements IColumnToolTipProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			RemoteCallData data = (RemoteCallData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
		}

	}

	/**
	 * The template object which is send to the server.
	 */
	private RemoteCallData template;

	/**
	 * The used data access service to access the data on the CMR.
	 */
	private IRemoteCallDataAccessService dataAccessService;

	/**
	 * The cached service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * List of RemoteCall data to be displayed.
	 */
	protected List<RemoteCallData> remoteCallDataList = new ArrayList<RemoteCallData>();

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

	@Override
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Getting remote call data information", IProgressMonitor.UNKNOWN);
		List<RemoteCallData> remoteCallData;

		remoteCallData = dataAccessService.getRemoteCallData(template);

		remoteCallDataList.clear();
		if (CollectionUtils.isNotEmpty(remoteCallData)) {
			remoteCallDataList.addAll(remoteCallData);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getColumnValues(Object object) {
		if (object instanceof RemoteCallData) {
			RemoteCallData data = (RemoteCallData) object;
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
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		TableViewerComparator<RemoteCallData> remoteCallDataViewerComparator = new TableViewerComparator<RemoteCallData>();
		for (Column column : Column.values()) {
			ResultComparator<RemoteCallData> resultComparator = new ResultComparator<RemoteCallData>(column.dataComparator, cachedDataService);
			remoteCallDataViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return remoteCallDataViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new UngroupedRemoteCallOverviewContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new RemoteCallDataLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReadableString(Object object) {
		if (object instanceof RemoteCallData) {
			RemoteCallData data = (RemoteCallData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, methodIdent, column).toString());
				sb.append('\t');
			}
			return sb.toString();
		} else {
			throw new RuntimeException("Could not create the human readable string! Class is: " + object.getClass().getCanonicalName());
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
	private StyledString getStyledTextForColumn(RemoteCallData data, MethodIdent methodIdent, Column enumId) {
		switch (enumId) {
		case TIME:
			return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
		case METHOD:
			return TextFormatter.getStyledMethodString(methodIdent);
		case CALLING:
			return new StyledString(String.valueOf(data.isCalling()));
		case IDENTIFICATION:
			return new StyledString(String.valueOf(data.getIdentification()));
		case PLATFORMIDENT:
			return new StyledString(String.valueOf(data.getPlatformIdent()));
		case URL:
			if (data.getUrl() != null) {
				return new StyledString(data.getUrl());
			} else {
				return new StyledString();
			}
		case RESPONSECODE:
			if (data.getResponseCode() > 0) {
				return new StyledString(String.valueOf(data.getResponseCode()));
			} else {
				return new StyledString();
			}
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new RemoteCallData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		dataAccessService = inputDefinition.getRepositoryDefinition().getRemoteCallDataAccessService();
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	@Override
	public Object getTableInput() {
		return remoteCallDataList;
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
	 * The content provider for this view.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	private static final class UngroupedRemoteCallOverviewContentProvider implements IStructuredContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<RemoteCallData> remoteCallData = (List<RemoteCallData>) inputElement;
			return remoteCallData.toArray();
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

}
