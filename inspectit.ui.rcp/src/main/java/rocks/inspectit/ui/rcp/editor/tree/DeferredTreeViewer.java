package rocks.inspectit.ui.rcp.editor.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.PendingUpdateAdapter;

/**
 * This tree viewer works in conjunction with the
 * {@link org.eclipse.ui.progress.DeferredTreeContentManager} so that the expand function will work.
 * <p>
 * <b>IMPORTANT:</b> The class is licensed under the Eclipse Public License v1.0 as it includes the
 * code from the {@link org.eclipse.jface.viewers.TreeViewer} class belonging to the Eclipse Rich
 * Client Platform. EPL v1.0 license can be found
 * <a href="https://www.eclipse.org/legal/epl-v10.html">here</a>.
 * <p>
 * Please relate to the LICENSEEXCEPTIONS.txt file for more information about license exceptions
 * that apply regarding to InspectIT and Eclipse RCP and/or EPL Components.
 * 
 * @author Patrice Bouillet
 * 
 */
public class DeferredTreeViewer extends TreeViewer {

	/**
	 * Maps the parent widgets to the level so that we know how deep we want to go.
	 */
	private Map<Widget, Integer> parentWidgets = Collections.synchronizedMap(new HashMap<Widget, Integer>());

	/**
	 * List of the elements that need to be expanded.
	 */
	private Set<Object> objectsToBeExpanded = Collections.synchronizedSet(new HashSet<Object>());

	/**
	 * Object to be selected.
	 */
	private AtomicReference<Object> objectToSelect = new AtomicReference<Object>();

	/**
	 * Creates a tree viewer on a newly-created tree control under the given parent. The tree
	 * control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and
	 * <code>BORDER</code>. The viewer has no input, no content provider, a default label provider,
	 * no sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public DeferredTreeViewer(Composite parent) {
		super(parent);
	}

	/**
	 * Creates a tree viewer on the given tree control. The viewer has no input, no content
	 * provider, a default label provider, no sorter, and no filters.
	 * 
	 * @param tree
	 *            the tree control
	 */
	public DeferredTreeViewer(Tree tree) {
		super(tree);
	}

	/**
	 * Creates a tree viewer on a newly-created tree control under the given parent. The tree
	 * control is created using the given SWT style bits. The viewer has no input, no content
	 * provider, a default label provider, no sorter, and no filters.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the SWT style bits used to create the tree.
	 */
	public DeferredTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalAdd(Widget widget, Object parentElement, Object[] childElements) {
		// we have to activate our own filters first, stupid eclipse
		// implementation which has got two different paths of applying filters
		// ...
		ViewerFilter[] filters = getFilters();
		for (int i = 0; i < filters.length; i++) {
			ViewerFilter filter = filters[i];
			childElements = filter.filter(this, parentElement, childElements);
		}

		super.internalAdd(widget, parentElement, childElements);

		// check if we are currently in the process of expanding the child
		// elements
		if (parentWidgets.containsKey(widget)) {
			// iterate over all child elements
			for (Object object : childElements) {
				// is it expandable
				if (super.isExpandable(object)) {
					// get the level
					Integer level = parentWidgets.get(widget);
					if (level == TreeViewer.ALL_LEVELS) {
						super.expandToLevel(object, TreeViewer.ALL_LEVELS);
					} else {
						super.expandToLevel(object, level - 1);
					}
				}
			}
		}

		if (objectsToBeExpanded != null && !objectsToBeExpanded.isEmpty()) {
			// iterate over all child elements
			for (Object object : childElements) {
				// is object in List of objects that need to be expanded?
				if (objectsToBeExpanded.contains(object)) {
					// then expand it
					if (!getExpandedState(object)) {
						super.expandToLevel(object, 1);
					}
				}
			}
		}

		// if there is object to be selected, we will selected if its parent is expanded
		while (true) {
			Object objToSelect = objectToSelect.get();
			if (objToSelect != null && (!isRootElement(objToSelect) || getExpandedState(getParentElement(objToSelect)))) {
				List<Object> selectionList = new ArrayList<Object>();
				Widget w = internalGetWidgetToSelect(objToSelect);
				if (w != null) {
					if (objectToSelect.compareAndSet(objToSelect, null)) {
						selectionList.add(w);
						setSelection(selectionList);
						break;
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalExpandToLevel(Widget widget, int level) {
		if (level > 1 || TreeViewer.ALL_LEVELS == level) {
			// we want to open more than one level, have to take care of that.
			Object data = widget.getData();
			if (!(data instanceof PendingUpdateAdapter)) {
				// just care about our own widgets
				parentWidgets.put(widget, Integer.valueOf(level));
			}
		}

		// when the widget is actually expanding, we have to remove its data from the list of object
		// that
		// needs to be expanded, if the data of the widget is found in the list
		Object data = widget.getData();
		if (data != null && objectsToBeExpanded.contains(data)) {
			objectsToBeExpanded.remove(data);
		}

		super.internalExpandToLevel(widget, level);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalRemove(Object[] elementsOrPaths) {
		// we want to remove the parent of the PendingUpdateAdapter items from
		// our Map
		if (1 == elementsOrPaths.length) {
			Object object = elementsOrPaths[0];
			if (object instanceof PendingUpdateAdapter) {
				Widget[] widgets = findItems(object);
				if (null != widgets && widgets.length > 0) {
					Widget widget = widgets[0];
					Widget parentWidget = getParentItem((Item) widget);
					if (parentWidgets.containsKey(parentWidget)) {
						parentWidgets.remove(parentWidget);
					}
				}
			}
		}

		super.internalRemove(elementsOrPaths);
	}

	/**
	 * Expands all ancestors of the given element or tree path so that the given element becomes
	 * visible in this viewer's tree control, and then expands the subtree rooted at the given
	 * element to the given level. The element will be then selected.
	 * 
	 * @param elementOrTreePath
	 *            the element
	 * @param level
	 *            non-negative level, or <code>ALL_LEVELS</code> to expand all levels of the tree
	 */
	public void expandToObjectAndSelect(Object elementOrTreePath, int level) {
		if (checkBusy()) {
			return;
		}
		Object parent = getParentElement(elementOrTreePath);
		// check if the element is already visible, or if it is root
		if (parent != null && getExpandedState(parent) || isRootElement(elementOrTreePath)) {
			// then only set selection
			Widget w = internalGetWidgetToSelect(elementOrTreePath);
			if (null != w) {
				// if widget is already available selected it
				List<Object> selectionList = new ArrayList<Object>();
				selectionList.add(w);
				setSelection(selectionList);
				// and overwrite any earlier set selection object
				objectToSelect.set(null);
			} else {
				// otherwise set object to selected
				objectToSelect.set(elementOrTreePath);
			}
		} else {
			// get all the objects that need to be expanded so that object is visible
			objectToSelect.set(elementOrTreePath);
			List<Object> objectsToExpand = createObjectList(parent, new ArrayList<Object>());
			if (!objectsToExpand.isEmpty()) {
				objectsToBeExpanded.addAll(objectsToExpand);
				Widget w = internalExpand(elementOrTreePath, true);
				if (w != null) {
					internalExpandToLevel(w, level);
				}
			} else {
				// if the list if empty, this means that no object in the tree has to load the
				// children, and they are all expanded, thus we can just select the wanted object
				Widget w = internalGetWidgetToSelect(elementOrTreePath);
				if (null != w) {
					// if widget is here available
					List<Object> selectionList = new ArrayList<Object>();
					selectionList.add(w);
					setSelection(selectionList);
					// and overwrite any earlier set selection object
					objectToSelect.set(null);
				}
			}
		}
	}

	/**
	 * Expands all ancestors of the given element or tree path so that the given element becomes
	 * visible in this viewer's tree control and additionally expands the element if it has
	 * children.
	 * 
	 * @param elementOrTreePath
	 *            the element
	 * @param level
	 *            non-negative level, or <code>ALL_LEVELS</code> to expand all levels of the tree
	 */
	public void expandObject(Object elementOrTreePath, int level) {
		if (checkBusy()) {
			return;
		}
		Object parent = getParentElement(elementOrTreePath);
		// check if the element is already visible, or if it is root
		if (!((parent != null && getExpandedState(parent)) || isRootElement(elementOrTreePath))) {
			// get all the objects that need to be expanded so that object is visible
			List<Object> objectsToExpand = createObjectList(parent, new ArrayList<Object>());
			if (!objectsToExpand.isEmpty()) {
				objectsToBeExpanded.addAll(objectsToExpand);
			}
		}
		objectsToBeExpanded.add(elementOrTreePath);
		Widget w = internalExpand(elementOrTreePath, true);
		if (w != null) {
			internalExpandToLevel(w, level);
		}
	}

	/**
	 * Constructs the list of elements that need to be expanded, so that object supplied can be
	 * visible.
	 * 
	 * @param object
	 *            Object that expansion should reach.
	 * @param objectList
	 *            List where the results are stored.
	 * @return List of objects for expansion.
	 */
	private List<Object> createObjectList(Object object, List<Object> objectList) {
		if (areFiltersPassed(object) && !getExpandedState(object)) {
			if (childrenLoaded(object)) {
				// if children are loaded for this object we simply expand it directly
				expandToLevel(object, 1);
			} else {
				if (objectList == null) {
					objectList = new ArrayList<Object>();
				}
				objectList.add(object);
			}
		}
		Object parent = getParentElement(object);
		if (null != parent) {
			createObjectList(parent, objectList);
		}
		return objectList;
	}

	/**
	 * Returns if all the filters are passed for the specific object.
	 * 
	 * @param object
	 *            Object to test.
	 * @return True if all the filters are passed, and thus object is visible in the tree. False
	 *         otherwise.
	 */
	private boolean areFiltersPassed(Object object) {
		ViewerFilter[] filters = getFilters();
		if (null != filters) {
			for (ViewerFilter filer : filters) {
				if (!filer.select(this, getParentElement(object), object)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Tests if the children of the tree item have been loaded.
	 * 
	 * @param object
	 *            Object to test.
	 * @return True if the children have been fetched.
	 */
	private boolean childrenLoaded(Object object) {
		Item[] children = getChildren(doFindItem(object));
		if (null == children) {
			return false;
		}
		for (Item item : children) {
			if (!(item instanceof TreeItem)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the given element is one of the root object in the input list of the tree viewer.
	 * 
	 * @param element
	 *            Element to check.
	 * @return True if the element is one of the root objects.
	 */
	private boolean isRootElement(Object element) {
		Object input = getRoot();
		Object[] rootElemens = ((ITreeContentProvider) getContentProvider()).getElements(input);
		return ArrayUtils.contains(rootElemens, element);
	}
}
