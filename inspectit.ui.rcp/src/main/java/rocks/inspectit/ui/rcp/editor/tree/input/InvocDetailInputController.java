package rocks.inspectit.ui.rcp.editor.tree.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.HttpTimerDataHelper;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.LoggingData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeElement;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeUtil;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.tree.InvocationTreeContentProvider;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.NumberFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.model.ExceptionImageFactory;
import rocks.inspectit.ui.rcp.model.ModifiersImageFactory;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;

/**
 * This input controller displays the detail contents of {@link InvocationSequenceData} objects.
 *
 * @author Patrice Bouillet
 *
 */
public class InvocDetailInputController extends AbstractTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.invocdetail";

	/**
	 * The resource manager is used for the images etc.
	 */
	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * The value of the selected data types.
	 */
	private Set<Class<?>> selectedDataTypes = PreferencesUtils.getObject(PreferencesConstants.INVOCATION_FILTER_DATA_TYPES);

	/**
	 * The value for the exclusive time filter.
	 */
	private double defaultExclusiveFilterTime = PreferencesUtils.getDoubleValue(PreferencesConstants.INVOCATION_FILTER_EXCLUSIVE_TIME);

	/**
	 * The value for the total time filter.
	 */
	private double defaultTotalFilterTime = PreferencesUtils.getDoubleValue(PreferencesConstants.INVOCATION_FILTER_TOTAL_TIME);

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private enum Column {
		/** The element column. */
		ELEMENT("Element", 700, InspectITImages.IMG_CALL_HIERARCHY),
		/** The duration column. */
		DURATION("Duration (ms)", 100, InspectITImages.IMG_TIME),
		/** The exclusive duration column. */
		EXCLUSIVE("Exc. duration (ms)", 100, null),
		/** The cpu duration column. */
		CPUDURATION("Cpu Duration (ms)", 100, null),
		/** The time-stamp column. **/
		START_DELTA("Start Delta (ms)", 100, InspectITImages.IMG_TIME_DELTA),
		/** The count column. */
		SQL("SQL", 300, InspectITImages.IMG_DATABASE),
		/** The parameter/field contents. */
		PARAMETER("Parameter Content", 200, null);

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
	 * The cached service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * Span service for loading span information.
	 */
	protected ISpanService spanService;

	/**
	 * Content provider for this class.
	 */
	private InvocationTreeContentProvider contentProvider = new InvocationTreeContentProvider();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
		spanService = inputDefinition.getRepositoryDefinition().getSpanService();
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
	public IContentProvider getContentProvider() {
		return contentProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new InvocDetailLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		preferences.add(PreferenceId.FILTERDATATYPE);
		preferences.add(PreferenceId.INVOCFILTEREXCLUSIVETIME);
		preferences.add(PreferenceId.INVOCFILTERTOTALTIME);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
		case FILTERDATATYPE:
			Class<?> dataTypeClass = (Class<?>) preferenceEvent.getPreferenceMap().get(PreferenceId.DataTypeSelection.SENSOR_DATA_SELECTION_ID);
			if (selectedDataTypes.contains(dataTypeClass)) {
				selectedDataTypes.remove(dataTypeClass);
			} else {
				selectedDataTypes.add(dataTypeClass);
			}
			break;
		case INVOCFILTEREXCLUSIVETIME:
			defaultExclusiveFilterTime = (Double) preferenceEvent.getPreferenceMap().get(PreferenceId.InvocExclusiveTimeSelection.TIME_SELECTION_ID);
			break;
		case INVOCFILTERTOTALTIME:
			defaultTotalFilterTime = (Double) preferenceEvent.getPreferenceMap().get(PreferenceId.InvocTotalTimeSelection.TIME_SELECTION_ID);
			break;
		default:
			// nothing to do by default
			break;
		}
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

		Object inputObject = data.get(0);

		if (inputObject instanceof InvocationTreeElement) {
			return true;
		}

		return false;
	}

	/**
	 * The invoc detail label provider for this view.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private final class InvocDetailLabelProvider extends StyledCellIndexLabelProvider {

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

			if (element instanceof InvocationSequenceData) {
				InvocationSequenceData data = (InvocationSequenceData) element;
				MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

				return getStyledTextForColumn(data, methodIdent, enumId);
			} else if (element instanceof Span) {
				return getStyledTextForColumn((Span) element, enumId);
			} else if (element instanceof SpanIdent) {
				StyledString missingSpanString = new StyledString();
				if (enumId == Column.ELEMENT) {
					missingSpanString.append("Unknown Span", StyledString.QUALIFIER_STYLER);
				}
				return missingSpanString;
			}

			return super.getStyledText(element, index);
		}

		/**
		 * Returns the column image for the given element at the given index.
		 *
		 * @param element
		 *            The element.
		 * @param index
		 *            The index.
		 * @return Returns the Image.
		 */
		@Override
		public Image getColumnImage(Object element, int index) {
			Column enumId = Column.fromOrd(index);

			if (element instanceof InvocationSequenceData) {
				InvocationSequenceData data = (InvocationSequenceData) element;
				MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());

				switch (enumId) {
				case ELEMENT:
					ExceptionSensorData exceptionSensorData = null;
					Image image = ModifiersImageFactory.getImage(methodIdent.getModifiers());

					if (InvocationSequenceDataHelper.hasExceptionData(data)) {
						exceptionSensorData = data.getExceptionSensorDataObjects().get(data.getExceptionSensorDataObjects().size() - 1);
						image = ExceptionImageFactory.decorateImageWithException(image, exceptionSensorData, resourceManager);
					}

					return image;
				default:
					return null;
				}
			} else if (element instanceof Span) {
				Span span = (Span) element;

				switch (enumId) {
				case ELEMENT:
					return ImageFormatter.getSpanImage(span, resourceManager);
				default:
					return null;
				}
			} else if (element instanceof SpanIdent) {
				switch (enumId) {
				case ELEMENT:
					return InspectIT.getDefault().getImage(InspectITImages.IMG_DISCOVERY_QUESTION);
				default:
					return null;
				}
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Color getBackground(Object element, int index) {
			if (!(element instanceof InvocationSequenceData)) {
				return null;
			}
			InvocationSequenceData data = (InvocationSequenceData) element;
			double duration = InvocationSequenceDataHelper.calculateDuration(data, spanService);

			if (-1.0d != duration) { // no duration?
				double exclusiveTime = duration - InvocationSequenceDataHelper.computeNestedDuration(data, spanService);

				double invocationDuration = InvocationSequenceDataHelper.getRootElementInSequence(data).getDuration();
				// compute the correct color
				int colorValue = 255 - (int) ((exclusiveTime / invocationDuration) * 100);

				if ((colorValue > 255) || (colorValue < 0)) {
					InspectIT.getDefault().createErrorDialog("The computation of the color value for the detail view returned an invalid value: " + colorValue, null, -1);
					return null;
				}

				Color color = resourceManager.createColor(new RGB(colorValue, colorValue, colorValue));
				return color;
			}

			return null;
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
	private StyledString getStyledTextForColumn(InvocationSequenceData data, MethodIdent methodIdent, Column enumId) {
		StyledString styledString = null;
		switch (enumId) {
		case ELEMENT:
			return TextFormatter.getStyledMethodString(methodIdent);
		case START_DELTA:
			InvocationSequenceData root = data;
			while (!InvocationSequenceDataHelper.isRootElementInSequence(root)) {
				root = root.getParentSequence();
			}
			long delta = data.getTimeStamp().getTime() - root.getTimeStamp().getTime();
			return new StyledString(NumberFormatter.formatLong(delta));
		case DURATION:
			styledString = new StyledString();
			double duration = InvocationSequenceDataHelper.calculateDuration(data, spanService);
			if (-1.0d != duration) {
				styledString.append(NumberFormatter.formatDouble(duration));
			}
			return styledString;
		case CPUDURATION:
			styledString = new StyledString();
			if (InvocationSequenceDataHelper.hasTimerData(data) && data.getTimerData().isCpuMetricDataAvailable()) {
				styledString.append(NumberFormatter.formatDouble(data.getTimerData().getCpuDuration()));
			}
			return styledString;
		case EXCLUSIVE:
			styledString = new StyledString();
			double dur = InvocationSequenceDataHelper.calculateDuration(data, spanService);

			if (-1.0d != dur) {
				double exclusiveTime = dur - InvocationSequenceDataHelper.computeNestedDuration(data, spanService);
				styledString.append(NumberFormatter.formatDouble(exclusiveTime));
			}

			return styledString;
		case SQL:
			styledString = new StyledString();
			if (InvocationSequenceDataHelper.hasSQLData(data)) {
				styledString.append(TextFormatter.clearLineBreaks(data.getSqlStatementData().getSqlWithParameterValues()));
			}
			return styledString;
		case PARAMETER:
			styledString = new StyledString();

			if (InvocationSequenceDataHelper.hasHttpTimerData(data)) {
				HttpTimerData httpTimer = (HttpTimerData) data.getTimerData();
				HttpInfo httpInfo = httpTimer.getHttpInfo();
				if (null != httpInfo.getUri()) {
					styledString.append("URI: ");
					styledString.append(httpInfo.getUri());
					styledString.append(" | ");
				}
				if (HttpTimerDataHelper.hasResponseCode(httpTimer)) {
					styledString.append("Status: ");
					styledString.append(String.valueOf(httpTimer.getHttpResponseStatus()));
					styledString.append(" | ");
				}
			}

			if (InvocationSequenceDataHelper.hasCapturedParameters(data)) {
				List<ParameterContentData> parameters = InvocationSequenceDataHelper.getCapturedParameters(data, true);
				boolean isFirst = true;
				for (ParameterContentData parameterContentData : parameters) {
					// shorten the representation here.
					if (!isFirst) {
						styledString.append(", ");
					} else {
						isFirst = false;
					}
					styledString.append("'");
					styledString.append(parameterContentData.getName());
					styledString.append("': ");
					styledString.append(TextFormatter.clearLineBreaks(parameterContentData.getContent()));
				}
			}

			if (InvocationSequenceDataHelper.hasLoggingData(data)) {
				LoggingData loggingData = data.getLoggingData();
				styledString.append("[" + loggingData.getLevel().toUpperCase() + "] ");
				styledString.append(loggingData.getMessage());
			}

			return styledString;
		default:
			return styledString;
		}
	}

	/**
	 * Returns styled text for {@link Span} based on the column.
	 *
	 * @param span
	 *            span
	 * @param enumId
	 *            column
	 * @return string
	 */
	public StyledString getStyledTextForColumn(Span span, Column enumId) {
		switch (enumId) {
		case ELEMENT:
			return TextFormatter.getSpanDetailsFull(span, cachedDataService);
		case DURATION:
			return new StyledString(NumberFormatter.formatDouble(span.getDuration()));
		default:
			return new StyledString();
		}
	}

	/**
	 * Searches the {@link InvocationSequenceData} list for the given span ident based on current
	 * input and returns the first invocation containing the span ident.
	 *
	 * @param input
	 *            Root invocations.
	 * @param spanIdent
	 *            ident
	 * @return Found data or <code>null</code>
	 */
	protected InvocationSequenceData getForSpanIdent(List<InvocationSequenceData> input, SpanIdent spanIdent) {
		for (InvocationSequenceData invoc : input) {
			if (Objects.equals(spanIdent, invoc.getSpanIdent())) {
				return invoc;
			}

			InvocationSequenceData containing = getForSpanIdent(invoc.getNestedSequences(), spanIdent);
			if (null != containing) {
				return containing;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ViewerFilter[] getFilters() {
		ViewerFilter sensorDataFilter = new InvocationViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof InvocationSequenceData) {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) element;
					if (checkIsOnlyInvocation(invocationSequenceData) && checkSensorDataTypeForObject(invocationSequenceData)) {
						return true;
					} else if (checkSensorDataTypeForObject(invocationSequenceData.getTimerData())) {
						return true;
					} else if (checkSensorDataTypeForObject(invocationSequenceData.getSqlStatementData())) {
						return true;
					} else if (CollectionUtils.isNotEmpty(invocationSequenceData.getExceptionSensorDataObjects())) {
						return checkSensorDataTypeForObject(invocationSequenceData.getExceptionSensorDataObjects().get(0));
					}
					return false;
				}
				return true;
			}

			private boolean checkSensorDataTypeForObject(Object object) {
				if (null != object) {
					return selectedDataTypes.contains(object.getClass());
				}
				return false;
			}

			private boolean checkIsOnlyInvocation(InvocationSequenceData data) {
				return (null == data.getTimerData()) && (null == data.getSqlStatementData()) && CollectionUtils.isEmpty(data.getExceptionSensorDataObjects());
			}
		};
		ViewerFilter exclusiveTimeFilter = new InvocationViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (Double.isNaN(defaultExclusiveFilterTime)) {
					return true;
				}

				if (element instanceof InvocationSequenceData) {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) element;

					// filter by the exclusive duration
					double duration = Double.NaN;
					if (InvocationSequenceDataHelper.hasSQLData(invocationSequenceData)) {
						duration = invocationSequenceData.getSqlStatementData().getDuration();
					} else if (InvocationSequenceDataHelper.hasTimerData(invocationSequenceData)) {
						double totalDuration = invocationSequenceData.getTimerData().getDuration();
						duration = totalDuration - InvocationSequenceDataHelper.computeNestedDuration(invocationSequenceData, spanService);
					} else if (InvocationSequenceDataHelper.isRootElementInSequence(invocationSequenceData)) {
						duration = invocationSequenceData.getDuration() - InvocationSequenceDataHelper.computeNestedDuration(invocationSequenceData, spanService);
					}

					if (!Double.isNaN(duration) && (duration <= defaultExclusiveFilterTime)) {
						return false;
					}
				}
				return true;
			}
		};
		ViewerFilter totalTimeFilter = new InvocationViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (Double.isNaN(defaultTotalFilterTime)) {
					return true;
				}

				if (element instanceof InvocationSequenceData) {
					InvocationSequenceData invocationSequenceData = (InvocationSequenceData) element;

					// filter by the exclusive duration
					double duration = InvocationSequenceDataHelper.calculateDuration(invocationSequenceData, spanService);
					if ((duration != -1.0d) && (duration <= defaultTotalFilterTime)) {
						return false;
					}
				}
				return true;
			}
		};
		return new ViewerFilter[] { sensorDataFilter, exclusiveTimeFilter, totalTimeFilter };
	}

	/**
	 * This class is needed to modify the filter method which behaves a little bit differently than
	 * the original one: Instead of filtering out a specific element _and_ all its sub-elements, it
	 * only filters out the specific elements and pushes up the elements which are child-elements of
	 * that one.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private abstract class InvocationViewerFilter extends ViewerFilter {
		/**
		 * The filtering method which tries to push up the child elements if a parent element has to
		 * be filtered out.
		 *
		 * @param viewer
		 *            The viewer
		 * @param parent
		 *            The parent object
		 * @param elements
		 *            The elements to check if they should be filtered
		 * @return Returns a set of elements which could be now even more than the initial elements
		 */
		@Override
		public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
			List<Object> out = new ArrayList<>();
			for (Object element : elements) {
				if (select(viewer, parent, element)) {
					out.add(element);
				} else {
					// This else branch has to be added to not filter out
					// child elements which would pass the filter.
					if (element instanceof InvocationSequenceData) {
						InvocationSequenceData data = (InvocationSequenceData) element;
						if (data.getChildCount() > 0) {
							// the parent object stays the same as this is the
							// graphical representation and not the underlying model
							out.addAll(Arrays.asList(filter(viewer, parent, data.getNestedSequences().toArray())));
						}
					}
				}
			}
			return out.toArray();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getReadableString(Object object) {
		if (object instanceof InvocationSequenceData) {
			InvocationSequenceData data = (InvocationSequenceData) object;
			StringBuilder sb = new StringBuilder();
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(data, methodIdent, column).toString());
				sb.append('\t');
			}
			return sb.toString();
		} else if (object instanceof Span) {
			Span span = (Span) object;
			StringBuilder sb = new StringBuilder();
			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(span, column).toString());
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
		if (object instanceof InvocationSequenceData) {
			InvocationSequenceData data = (InvocationSequenceData) object;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			List<String> values = new ArrayList<>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(data, methodIdent, column).toString());
			}
			return values;
		} else if (object instanceof Span) {
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
		return InvocationTreeUtil.getDataElements(contentProvider.getRootElement()).toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		resourceManager.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.SLAVE;
	}

}
