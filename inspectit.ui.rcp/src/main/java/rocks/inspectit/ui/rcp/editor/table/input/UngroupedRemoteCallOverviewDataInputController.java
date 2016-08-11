package rocks.inspectit.ui.rcp.editor.table.input;

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

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.IDataComparator;
import rocks.inspectit.shared.all.communication.comparator.MethodSensorDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.RemoteCallDataComparatorEnum;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.shared.all.communication.data.RemoteHttpCallData;
import rocks.inspectit.shared.all.communication.data.RemoteMQCallData;
import rocks.inspectit.shared.cs.cmr.service.IRemoteCallDataAccessService;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.editor.table.TableViewerComparator;
import rocks.inspectit.ui.rcp.editor.tooltip.IColumnToolTipProvider;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

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

		/** The platformId. */
		PLATFORMIDENT("Platform ID", 100, InspectITImages.IMG_PACKAGE, DefaultDataComparatorEnum.PLATFORM_IDENT),
		/** The time column. */
		TIME("Start Time", 100, InspectITImages.IMG_TIMER, DefaultDataComparatorEnum.TIMESTAMP),
		/** The method column. */
		METHOD("Method", 550, InspectITImages.IMG_METHOD_PUBLIC, MethodSensorDataComparatorEnum.METHOD),
		/** The remote platformId. */
		REMOTEPLATFORMIDENT("Remote Platform ID", 100, InspectITImages.IMG_PACKAGE, RemoteCallDataComparatorEnum.REMOTE_PLATFORM_ID),
		/** The method column. */
		CALLING("Calling", 50, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComparatorEnum.IS_CALLING),
		/** The method column. */
		IDENTIFICATION("Identification", 100, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComparatorEnum.IDENTIFICATION),
		/** Connection Type. */
		REMOTE_TYPE("Type", 100, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComparatorEnum.REMOTE_TYPE),
		/** Connection Specific Data. */
		CONNECTION_SPECIFIC_DATA("Connection Specific Information", 250, InspectITImages.IMG_METHOD_PUBLIC, RemoteCallDataComparatorEnum.CONNECTION_SPECIFIC_DATA);

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
		private final String name;
		/** The width of the column. */
		private final int width;
		/** The image descriptor. Can be <code>null</code> */
		private final Image image;

		/** Comparator for the column. */
		private final IDataComparator<? super RemoteCallData> dataComparator;

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
		case REMOTEPLATFORMIDENT:
			return new StyledString(String.valueOf(data.getRemotePlatformIdent()));
		case REMOTE_TYPE:
			if (data instanceof RemoteHttpCallData) {
				return new StyledString("http");
			} else if (data instanceof RemoteMQCallData) {
				return new StyledString("mq");
			}
		case CONNECTION_SPECIFIC_DATA:
			return new StyledString(data.getSpecificData());
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
		template.setMethodIdent(inputDefinition.getIdDefinition().getMethodId());

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
		@Override
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<RemoteCallData> remoteCallData = (List<RemoteCallData>) inputElement;
			return remoteCallData.toArray();
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

}
