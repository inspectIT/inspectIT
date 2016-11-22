package rocks.inspectit.ui.rcp.editor.table.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.tracing.data.ISpanIdentAware;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.communication.comparator.DefaultDataComparatorEnum;
import rocks.inspectit.shared.cs.communication.comparator.IDataComparator;
import rocks.inspectit.shared.cs.communication.comparator.ResultComparator;
import rocks.inspectit.shared.cs.tracing.comparator.SpanComparator;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId.LiveMode;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.editor.table.RemoteTableViewerComparator;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.handlers.OpenTraceDetailsHandler;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * The input controller for the table that shows all the root spans.
 *
 * @author Ivan Senic
 *
 */
public class TraceOverviewInputController extends AbstractTableInputController implements TableInputController {

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 *
	 * @author Patrice Bouillet
	 * @author Ivan Senic
	 *
	 */
	private enum Column {
		/** The start time column. */
		TIME("Start Time", 200, InspectITImages.IMG_TIMESTAMP, DefaultDataComparatorEnum.TIMESTAMP),
		/** The duration column. */
		DURATION("Duration (ms)", 100, InspectITImages.IMG_TIME, SpanComparator.DURATION),
		/** Propagation type. */
		PROPAGATION("Propagation", 120, null, SpanComparator.PROPAGATION_TYPE),
		/** Details. */
		DETAILS("Details", 300, null, null),
		/** Details. */
		ORIGIN("Origin", 120, null, null),
		/** Details. */
		TRACE_ID("Trace ID", 120, null, null);

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;
		/** Comparator for the column. */
		private IDataComparator<? super AbstractSpan> dataComparator;

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
		Column(String name, int width, String imageName, IDataComparator<? super AbstractSpan> dataComparator) {
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
			if ((i < 0) || (i >= Column.values().length)) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}

	}

	/**
	 * Default comparator when no sorting is defined.
	 */
	private final ResultComparator<AbstractSpan> defaultComparator = new ResultComparator<>(DefaultDataComparatorEnum.TIMESTAMP, false);

	/**
	 * Span service.
	 */
	private ISpanService spanService;

	/**
	 * Global data access service.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * List of spans to be displayed.
	 */
	private Collection<Span> spanList = new ArrayList<>();

	/**
	 * The limit of the result set.
	 */
	private int limit = PreferencesUtils.getIntValue(PreferencesConstants.ITEMS_COUNT_TO_SHOW);

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
	 * Result comparator to be used on the server.
	 */
	private ResultComparator<AbstractSpan> resultComparator = defaultComparator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		spanService = inputDefinition.getRepositoryDefinition().getSpanService();
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
		preferences.add(PreferenceId.ITEMCOUNT);
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
	public boolean canOpenInput(List<? extends Object> data) {
		return data != null;
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
	public Object getTableInput() {
		return spanList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Getting trace information..", IProgressMonitor.UNKNOWN);
		loadDataFromService();
		monitor.done();
	}

	/**
	 * Loads data from the service with current filters.
	 */
	protected void loadDataFromService() {
		Collection<? extends Span> spans;
		if (autoUpdate) {
			spans = spanService.getRootSpans(limit, null, null, defaultComparator);
		} else {
			spans = spanService.getRootSpans(limit, fromDate, toDate, resultComparator);
		}

		spanList.clear();
		if (CollectionUtils.isNotEmpty(spans)) {
			spanList.addAll(spans);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new SpanLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerComparator getComparator() {
		RemoteTableViewerComparator<AbstractSpan> spanViewerComparator = new RemoteTableViewerComparator<AbstractSpan>() {
			@Override
			protected void sortRemotely(ResultComparator<AbstractSpan> resultComparator) {
				if (null != resultComparator) {
					TraceOverviewInputController.this.resultComparator = resultComparator;
				} else {
					TraceOverviewInputController.this.resultComparator = defaultComparator;
				}
				loadDataFromService();
			}
		};
		for (Column column : Column.values()) {
			if (null != column.dataComparator) {
				ResultComparator<AbstractSpan> resultComparator = new ResultComparator<>(column.dataComparator);
				spanViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
			}
		}

		return spanViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		StructuredSelection selection = (StructuredSelection) event.getSelection();
		if ((selection.size() == 1) && (selection.getFirstElement() instanceof ISpanIdentAware)) {
			// open trace details view
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

			Command command = commandService.getCommand(OpenTraceDetailsHandler.COMMAND);
			ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());

			try {
				command.executeWithChecks(executionEvent);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReadableString(Object object) {
		if (object instanceof Span) {
			Span data = (Span) object;
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
		if (object instanceof Span) {
			Span data = (Span) object;
			List<String> values = new ArrayList<>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * Label provider for the view.
	 *
	 * @author Ivan Senic
	 *
	 */
	private final class SpanLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * The resource manager is used for the images etc.
		 */
		private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StyledString getStyledText(Object element, int index) {
			Span data = (Span) element;
			Column enumId = Column.fromOrd(index);

			StyledString styledString = getStyledTextForColumn(data, enumId);
			return styledString;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			Span data = (Span) element;
			Column enumId = Column.fromOrd(index);
			switch (enumId) {
			case PROPAGATION:
				return ImageFormatter.getPropagationImage(data.getPropagationType());
			case ORIGIN:
				return ImageFormatter.getSpanOriginImage(data, cachedDataService.getPlatformIdentForId(data.getPlatformIdent()));
			default:
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			resourceManager.dispose();
			super.dispose();
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
	private StyledString getStyledTextForColumn(Span data, Column enumId) {
		switch (enumId) {
		case TIME:
			return new StyledString(NumberFormatter.formatTimeWithMillis(data.getTimeStamp()));
		case DURATION:
			return new StyledString(NumberFormatter.formatDouble(data.getDuration(), timeDecimalPlaces));
		case PROPAGATION:
			return TextFormatter.getPropagationStyled(data.getPropagationType());
		case DETAILS:
			return TextFormatter.getSpanDetailsFull(data, cachedDataService);
		case ORIGIN:
			return TextFormatter.getSpanOriginStyled(data, cachedDataService.getPlatformIdentForId(data.getPlatformIdent()));
		case TRACE_ID:
			return new StyledString(Long.toHexString(data.getSpanIdent().getTraceId()));
		default:
			return new StyledString("error");
		}
	}
}
