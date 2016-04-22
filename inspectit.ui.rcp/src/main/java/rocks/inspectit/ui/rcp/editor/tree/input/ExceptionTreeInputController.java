package rocks.inspectit.ui.rcp.editor.tree.input;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.viewers.StyledCellIndexLabelProvider;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.model.ExceptionImageFactory;
import rocks.inspectit.ui.rcp.model.ModifiersImageFactory;

/**
 * This input controller displays the detail contents of {@link ExceptionSensorData} objects.
 *
 * @author Eduard Tudenhoefner
 *
 */
public class ExceptionTreeInputController extends AbstractTreeInputController {

	/**
	 * The ID of this subview / controller.
	 */
	public static final String ID = "inspectit.subview.tree.exceptiontree";

	/**
	 * The list of {@link ExceptionSensorData} objects which is displayed.
	 */
	private List<ExceptionSensorData> exceptionSensorData = new ArrayList<>();

	/**
	 * The resource manager is used for the images etc.
	 */
	private LocalResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 *
	 * @author Eduard Tudenhoefner
	 *
	 */
	private static enum Column {
		/** The event type column. */
		EVENT_TYPE("Event Type", 280, null),
		/** The method column. */
		METHOD_CONSTRUCTOR("Method / Constructor", 500, InspectITImages.IMG_METHOD),
		/** The error message column. */
		ERROR_MESSAGE("Error Message", 250, null),
		/** The cause column. */
		CAUSE("Cause", 120, null);

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
		private Column(String name, int width, String imageName) {
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
	public int getExpandLevel() {
		return AbstractTreeViewer.ALL_LEVELS;
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
	public Object getTreeInput() {
		return exceptionSensorData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new ExceptionTreeContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBaseLabelProvider getLabelProvider() {
		return new ExceptionTreeLabelProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends DefaultData> data) {
		if (null == data) {
			return false;
		}

		if (data.isEmpty()) {
			return true;
		}

		if (!(data.get(0) instanceof ExceptionSensorData)) {
			return false;
		}

		return true;
	}

	/**
	 * The exception tree details label provider for this view.
	 *
	 * @author Eduard Tudenhoefner
	 *
	 */
	private final class ExceptionTreeLabelProvider extends StyledCellIndexLabelProvider {

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
			ExceptionSensorData data = (ExceptionSensorData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			return getStyledTextForColumn(data, methodIdent, enumId);
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
			ExceptionSensorData data = (ExceptionSensorData) element;
			MethodIdent methodIdent = cachedDataService.getMethodIdentForId(data.getMethodIdent());
			Column enumId = Column.fromOrd(index);

			switch (enumId) {
			case METHOD_CONSTRUCTOR:
				Image image = ModifiersImageFactory.getImage(methodIdent.getModifiers());
				image = ExceptionImageFactory.decorateImageWithException(image, data, resourceManager);
				return image;
			case EVENT_TYPE:
				return null;
			case ERROR_MESSAGE:
				return null;
			case CAUSE:
				return null;
			default:
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Color getBackground(Object element, int index) {
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
	private static StyledString getStyledTextForColumn(ExceptionSensorData data, MethodIdent methodIdent, Column enumId) {
		StyledString styledString = null;
		switch (enumId) {
		case METHOD_CONSTRUCTOR:
			return new StyledString(TextFormatter.getMethodWithParameters(methodIdent));
		case EVENT_TYPE:
			styledString = new StyledString(data.getExceptionEvent().toString());
			return styledString;
		case ERROR_MESSAGE:
			styledString = new StyledString();
			if (null != data.getErrorMessage()) {
				styledString.append(data.getErrorMessage());
			}
			return styledString;
		case CAUSE:
			styledString = new StyledString();
			if (null != data.getCause()) {
				styledString.append(data.getCause().toString());
			}
			return styledString;
		default:
			return styledString;
		}
	}

	/**
	 * The exception tree details content provider for this view.
	 *
	 * @author Eduard Tudenhoefner
	 *
	 */
	private static final class ExceptionTreeContentProvider implements ITreeContentProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object[] getChildren(Object parent) {
			ExceptionSensorData exceptionSensorData = (ExceptionSensorData) parent;
			List<ExceptionSensorData> exceptionSensorDataList = new ArrayList<>();
			exceptionSensorDataList.add(exceptionSensorData.getChild());

			return exceptionSensorDataList.toArray();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getParent(Object child) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean hasChildren(Object parent) {
			if (parent == null) {
				return false;
			}

			if (parent instanceof ExceptionSensorData) {
				ExceptionSensorData exceptionSensorData = (ExceptionSensorData) parent;
				if (null != exceptionSensorData.getChild()) {
					return true;
				}
			}

			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			List<ExceptionSensorData> exceptionSensorData = (List<ExceptionSensorData>) inputElement;
			return exceptionSensorData.toArray();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
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
			List<String> values = new ArrayList<>();
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
	@SuppressWarnings("unchecked")
	public Object[] getObjectsToSearch(Object treeInput) {
		List<ExceptionSensorData> allObjects = new ArrayList<>();
		List<ExceptionSensorData> exceptionSensorDataList = (List<ExceptionSensorData>) treeInput;
		for (ExceptionSensorData exData : exceptionSensorDataList) {
			ExceptionSensorData objectToAdd = exData;
			while (null != objectToAdd) {
				allObjects.add(objectToAdd);
				objectToAdd = objectToAdd.getChild();
			}
		}
		return allObjects.toArray();
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
