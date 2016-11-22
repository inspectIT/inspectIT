package rocks.inspectit.ui.rcp.editor.tree.input;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import io.opentracing.tag.Tags;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.TraceInputDefinitionExtra;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.root.IRootEditor;
import rocks.inspectit.ui.rcp.editor.tree.util.TraceTreeData;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.handlers.LocateHandler;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * The input controller that displays the details of one trace.
 *
 * @author Ivan Senic
 *
 */
public class TraceDetailsTreeInputController extends AbstractTreeInputController {

	/**
	 * Columns for the view.
	 *
	 * @author Ivan Senic
	 *
	 */
	private enum Column {
		/** The method column. */
		DETAILS("Details", 350, null),
		/** Type. */
		TYPE("Client", 40, null),
		/** Propagation. */
		PROPAGATION("Propagation", 100, null),
		/** Error. */
		ERROR("Error", 40, null),
		/** Propagation. */
		NESTED_DATA("Nested Data", 40, null),
		/** The start time column. */
		TIME("Start Time", 160, InspectITImages.IMG_TIMESTAMP),
		/** The duration column. */
		DURATION("Duration (ms)", 80, InspectITImages.IMG_TIME),
		/** The exclusive duration column. */
		EXCLUSIVE("Exc. duration (ms)", 100, null),
		/** The application column. */
		APPLICATION("Application", 150, null),
		/** The business transaction column. */
		BUSINESS_TRANSACTION("Business Transaction", 150, null),
		/** Propagation. */
		ORIGIN("Origin", 120, null);

		/** The name. */
		private String name;
		/** The width of the column. */
		private int width;
		/** The image descriptor. Can be <code>null</code> */
		private Image image;

		/**
		 * Default constructor which creates a column enumeration object.
		 *
		 * @param name
		 *            The name of the column.
		 * @param width
		 *            The width of the column.
		 * @param imageName
		 *            The name of the image. Names are defined in {@link InspectITImages}.
		 */
		Column(String name, int width, String imageName) {
			this.name = name;
			this.width = width;
			this.image = InspectIT.getDefault().getImage(imageName);
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
	 * Empty styled string.
	 */
	private static final StyledString EMPTY_STYLED_STRING = new StyledString();

	/**
	 * Trace id to display details for.
	 */
	private long traceId;

	/**
	 * The cached service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * Span service for loading traces.
	 */
	private ISpanService spanService;

	/**
	 * Invocation services for loading invocation related to a trace.
	 */
	private IInvocationDataAccessService invocationDataAccessService;

	/**
	 * Current input of the tree.
	 */
	private TraceTreeData input;

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

		spanService = inputDefinition.getRepositoryDefinition().getSpanService();
		invocationDataAccessService = inputDefinition.getRepositoryDefinition().getInvocationDataAccessService();
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();

		if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.TRACE_EXTRAS_MARKER)) {
			TraceInputDefinitionExtra inputDefinitionExtra = inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.TRACE_EXTRAS_MARKER);
			traceId = inputDefinitionExtra.getTraceId();
		}
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
			viewerColumn.getColumn().setWidth(column.width);
			if (null != column.image) {
				viewerColumn.getColumn().setImage(column.image);
			}
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
		preferences.add(PreferenceId.UPDATE);
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
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Loading trace details..", IProgressMonitor.UNKNOWN);

		Collection<? extends Span> spans = spanService.getSpans(traceId);
		Collection<InvocationSequenceData> invocations = invocationDataAccessService.getInvocationSequenceDetail(traceId);

		if (CollectionUtils.isNotEmpty(spans)) {
			input = TraceTreeData.buildModel(spans, invocations);

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					IRootEditor rootEditor = (IRootEditor) page.getActiveEditor();
					rootEditor.setDataInput(Collections.singletonList(input));
				}
			});
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTreeInput() {
		return input;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends Object> data) {
		if (CollectionUtils.isEmpty(data)) {
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new TraceDetailContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new TraceDetailLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getExpandLevel() {
		return TreeViewer.ALL_LEVELS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeExpandedState(boolean expandedState, TreePath treePath) {
		// only expand, never collapse
		return !expandedState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		StructuredSelection selection = (StructuredSelection) event.getSelection();
		if (!selection.isEmpty()) {
			// open trace details view
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

			Command command = commandService.getCommand(LocateHandler.COMMAND);
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
	public List<String> getColumnValues(Object object) {
		if (object instanceof Span) {
			Span span = (Span) object;
			List<String> values = new ArrayList<>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(span, column).toString());
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
		TraceTreeData data = (TraceTreeData) treeInput;
		if (null != data) {
			List<Span> allObjects = new ArrayList<>();
			extractAllChildren(allObjects, data);
			return allObjects.toArray();
		}
		return new Object[0];

	}

	/**
	 * Extracts all children from {@link TraceTreeData} in all objects list.
	 *
	 * @param allObjects
	 *            List to extract children.
	 * @param data
	 *            trace data
	 */
	private void extractAllChildren(List<Span> allObjects, TraceTreeData data) {
		allObjects.add(data.getSpan());

		for (TraceTreeData child : data.getChildren()) {
			extractAllChildren(allObjects, child);
		}
	}

	/**
	 * Returns {@link StyledString} for the data / column combo.
	 *
	 * @param span
	 *            {@link Span}.
	 * @param column
	 *            {@link Column}
	 * @return {@link StyledString}
	 */
	private StyledString getStyledTextForColumn(Span span, Column column) {
		TraceTreeData data = TraceTreeData.getForSpanIdent(input, span.getSpanIdent());
		switch (column) {
		case DETAILS:
			return TextFormatter.getSpanDetailsShort(span, cachedDataService);
		case PROPAGATION:
			return TextFormatter.getPropagationStyled(span.getPropagationType());
		case ORIGIN:
			return TextFormatter.getSpanOriginStyled(span, cachedDataService.getPlatformIdentForId(span.getPlatformIdent()));
		case TIME:
			return new StyledString(NumberFormatter.formatTimeWithMillis(span.getTimeStamp()));
		case DURATION:
			StyledString durationString = new StyledString(NumberFormatter.formatDouble(span.getDuration(), timeDecimalPlaces));
			if (data.isConsideredAsync()) {
				durationString.append(TextFormatter.getWarningSign());
			}
			return durationString;
		case EXCLUSIVE:
			StyledString exclusive = new StyledString();
			exclusive.append(NumberFormatter.formatDouble(data.getExclusiveDuration(), timeDecimalPlaces));
			exclusive.append(" (", StyledString.COUNTER_STYLER);
			int percentage = (int) ((data.getExclusivePercentage()) * 100);
			exclusive.append(NumberFormatter.formatInteger(percentage), StyledString.COUNTER_STYLER);
			exclusive.append("%)", StyledString.COUNTER_STYLER);
			if (data.isConsideredAsync()) {
				exclusive.append(TextFormatter.getWarningSign());
			}
			return exclusive;
		case APPLICATION:
			if (CollectionUtils.isNotEmpty(data.getInvocations())) {
				Set<Integer> applicationIds = new HashSet<>(1);
				for (InvocationSequenceData invoc : data.getInvocations()) {
					applicationIds.add(invoc.getApplicationId());
				}
				if (applicationIds.size() > 1) {
					return new StyledString("Multiple");
				} else {
					int appId = applicationIds.iterator().next();
					ApplicationData application = cachedDataService.getApplicationForId(appId);
					if (null != application) {
						return new StyledString(application.getName());
					}
				}
			}
			return EMPTY_STYLED_STRING;
		case BUSINESS_TRANSACTION:
			if (CollectionUtils.isNotEmpty(data.getInvocations())) {
				Set<Integer> transationsIds = new HashSet<>(1);
				Set<Integer> applicationIds = new HashSet<>(1);
				for (InvocationSequenceData invoc : data.getInvocations()) {
					applicationIds.add(invoc.getApplicationId());
					transationsIds.add(invoc.getBusinessTransactionId());
				}
				if ((transationsIds.size() > 1) || (applicationIds.size() > 1)) {
					return new StyledString("Miltiple");
				} else {
					int appId = applicationIds.iterator().next();
					int btId = transationsIds.iterator().next();
					BusinessTransactionData transaction = cachedDataService.getBusinessTransactionForId(appId, btId);
					if (null != transaction) {
						return new StyledString(transaction.getName());
					}
				}
			}
			return EMPTY_STYLED_STRING;
		default:
			return EMPTY_STYLED_STRING;
		}
	}

	/**
	 * Content provider.
	 *
	 * @author Ivan Senic
	 *
	 */
	private final class TraceDetailContentProvider implements ITreeContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			// INPORTANT: We always return spans as the elements for the tree, so that all command
			// like search and locate can work
			if (inputElement instanceof TraceTreeData) {
				return new Object[] { ((TraceTreeData) inputElement).getSpan() };
			}
			return new Object[] {};
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Span) {
				Span span = (Span) parentElement;
				TraceTreeData data = TraceTreeData.getForSpanIdent(input, span.getSpanIdent());
				if (null != data) {
					Object[] children = new Object[data.getChildren().size()];
					for (int i = 0; i < children.length; i++) {
						children[i] = data.getChildren().get(i).getSpan();
					}
					return children;
				}
			}
			return new Object[0];
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getParent(Object element) {
			if (element instanceof Span) {
				Span span = (Span) element;
				TraceTreeData data = TraceTreeData.getForSpanIdent(input, span.getSpanIdent());
				if (null != data) {
					TraceTreeData parent = data.getParent();
					if (null != parent) {
						return parent.getSpan();
					}
				}
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof Span) {
				Span span = (Span) element;
				TraceTreeData data = TraceTreeData.getForSpanIdent(input, span.getSpanIdent());
				if (null != data) {
					return CollectionUtils.isNotEmpty(data.getChildren());
				}
			}
			return false;
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
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * Label provider.
	 *
	 * @author Ivan Senic
	 *
	 */
	private final class TraceDetailLabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * The resource manager is used for the images etc.
		 */
		private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected StyledString getStyledText(Object element, int index) {
			Span span = (Span) element;
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(span, enumId);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image getColumnImage(Object element, int index) {
			Span span = (Span) element;
			TraceTreeData data = TraceTreeData.getForSpanIdent(input, span.getSpanIdent());
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case DETAILS:
				return ImageFormatter.getSpanImage(span, resourceManager);
			case TYPE:
				if (span.isCaller()) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK);
				}
				break;
			case ERROR:
				boolean error = MapUtils.getBoolean(span.getTags(), Tags.ERROR.getKey(), false);
				if (error) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_ERROR_CIRCLE_FRAME);
				}
				break;
			case NESTED_DATA:
				if (data.hasSqlsInInvocations() && data.hasExceptionsInInvocations()) {
					return ImageFormatter.getCombinedImage(resourceManager, SWT.HORIZONTAL, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_DATABASE),
							InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_EXCEPTION_SENSOR));
				} else if (data.hasSqlsInInvocations()) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_DATABASE);
				} else if (data.hasExceptionsInInvocations()) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_EXCEPTION_SENSOR);
				}
				break;
			case PROPAGATION:
				return ImageFormatter.getPropagationImage(span.getPropagationType());
			case ORIGIN:
				return ImageFormatter.getSpanOriginImage(span, cachedDataService.getPlatformIdentForId(span.getPlatformIdent()));
			default:
				return super.getColumnImage(element, index);
			}
			return super.getColumnImage(element, index);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getToolTipText(Object element, int index) {
			Span span = (Span) element;
			TraceTreeData data = TraceTreeData.getForSpanIdent(input, span.getSpanIdent());
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case NESTED_DATA:
				boolean hasSqlsInInvocations = data.hasSqlsInInvocations();
				boolean hasExceptionsInInvocations = data.hasExceptionsInInvocations();
				if (hasSqlsInInvocations || hasExceptionsInInvocations) {
					StringBuilder toolTip = new StringBuilder("This trace span contains:");
					if (hasSqlsInInvocations) {
						toolTip.append("\n - SQL statement(s)");
					}
					if (hasExceptionsInInvocations) {
						toolTip.append("\n - Exception(s)");
					}
					return toolTip.toString();
				}
				break;
			case TIME:
				return "Start times are reported by the clock on specific agent, thus differences can occur.";
			case DURATION:
				if (data.isConsideredAsync()) {
					return "This span is considered asynchrouns to it's parent.\nIt's duration can not be used in reference to it's parent.";
				}
				break;
			case EXCLUSIVE:
				if (data.isConsideredAsync()) {
					return "This span is considered asynchrouns to it's parent.\nIt's exclusive duration only reflects this span and it's children.";
				}
				break;
			case PROPAGATION:
				return TextFormatter.getPropagationStyled(span.getPropagationType()).getString();
			default:
				return super.getToolTipText(element, index);
			}
			return super.getToolTipText(element, index);
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

}
