package info.novatec.inspectit.rcp.editor.tree.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.root.SubViewClassificationController;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * The interface for all tree input controller.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface TreeInputController extends SubViewClassificationController {

	/**
	 * Sets the input definition of this controller.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	void setInputDefinition(InputDefinition inputDefinition);

	/**
	 * Creates the columns in the given tree viewer.
	 * 
	 * @param treeViewer
	 *            The tree viewer.
	 */
	void createColumns(TreeViewer treeViewer);

	/**
	 * The {@link info.novatec.inspectit.rcp.editor.tree.TreeSubView} might need to alter the column
	 * width/visibility if the column has the remembered size. With this method the controller gives
	 * or denies the {@link info.novatec.inspectit.rcp.editor.tree.TreeSubView} to alter the column
	 * width.
	 * 
	 * @param treeColumn
	 *            {@link TreeColumn}
	 * 
	 * @return Returns true if the {@link TreeColumn} can be altered.
	 */
	boolean canAlterColumnWidth(TreeColumn treeColumn);

	/**
	 * This method will be called when a double click event is executed.
	 * 
	 * @param event
	 *            The event object.
	 */
	void doubleClick(DoubleClickEvent event);

	/**
	 * Generates and returns the input for the tree. Returning <code>null</code> is possible and
	 * indicates most of the time that there is no default list or object to display in the table.
	 * For some {@link DefaultData} objects, the method {@link #canOpenInput(List)} should return
	 * true so that the input object is set by the
	 * {@link info.novatec.inspectit.rcp.editor.tree.TreeSubView}.
	 * 
	 * @return The tree input or <code>null</code> if nothing to display for default.
	 */
	Object getTreeInput();

	/**
	 * Returns the content provider for the {@link TreeViewer}.
	 * 
	 * @return The content provider.
	 * @see IContentProvider
	 */
	IContentProvider getContentProvider();

	/**
	 * Returns the label provider for the {@link TreeViewer}.
	 * 
	 * @return The label provider
	 * @see IBaseLabelProvider
	 */
	IBaseLabelProvider getLabelProvider();

	/**
	 * Returns the comparator for the {@link TreeViewer}. Can be <code>null</code> to indicate that
	 * no sorting of the elements should be done.
	 * 
	 * @return The tree viewer comparator.
	 */
	ViewerComparator getComparator();

	/**
	 * Refreshes the current data and updates the tree input if new items are available.
	 * 
	 * @param monitor
	 *            The progress monitor.
	 * @param rootEditor
	 *            RootEditor of the view that is being refreshed.
	 */
	void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor);

	/**
	 * Returns <code>true</code> if the controller can open the input which consists of one or
	 * several {@link DefaultData} objects.
	 * 
	 * @param data
	 *            The data which is checked if the controller can open it.
	 * @return Returns <code>true</code> if the controller can open the input.
	 */
	boolean canOpenInput(List<? extends DefaultData> data);

	/**
	 * Returns all needed preference IDs.
	 * 
	 * @return A {@link Set} containing all {@link PreferenceId}. Returning <code>null</code> is not
	 *         permitted here. At least a {@link java.util.Collections#EMPTY_SET} should be
	 *         returned.
	 */
	Set<PreferenceId> getPreferenceIds();

	/**
	 * This method is called whenever something is changed in one of the preferences.
	 * 
	 * @param preferenceEvent
	 *            The event object containing the changed objects.
	 */
	void preferenceEventFired(PreferenceEvent preferenceEvent);

	/**
	 * This method creates a human readable string out of the given object (which is object from the
	 * tree model).
	 * 
	 * @param object
	 *            The object to create the string from.
	 * @return The created human readable string.
	 */
	String getReadableString(Object object);

	/**
	 * Return the values of all columns in the tree for the given object. Not visible columns values
	 * will also be included. The order of the values will be same to the initial tree column order,
	 * thus not reflecting the current state of the tree if the columns were moved.
	 * 
	 * @param object
	 *            Object to get values for.
	 * @return List of string representing the values.
	 */
	List<String> getColumnValues(Object object);

	/**
	 * Returns an optional filter for this tree.
	 * 
	 * @return the filter array.
	 */
	ViewerFilter[] getFilters();

	/**
	 * Returns the level to which the viewer's tree should be expanded.
	 * 
	 * @return The level to which the viewer's tree should be expanded.
	 */
	int getExpandLevel();

	/**
	 * Returns the list of the objects that should be searched.
	 * 
	 * @param treeInput
	 *            Current input of the table. The {@link TreeInputController} is responsible to
	 *            modify the input if necessary.
	 * @return Returns the list of the objects that should be searched.
	 */
	Object[] getObjectsToSearch(Object treeInput);

	/**
	 * Disposes the tree input.
	 */
	void dispose();

}
