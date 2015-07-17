package info.novatec.inspectit.rcp.editor.table.input;

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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.TableColumn;

/**
 * The interface for all table input controller.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface TableInputController extends SubViewClassificationController {

	/**
	 * Sets the input definition of this controller.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	void setInputDefinition(InputDefinition inputDefinition);

	/**
	 * Creates the columns in the given table viewer.
	 * 
	 * @param tableViewer
	 *            The table viewer.
	 */
	void createColumns(TableViewer tableViewer);

	/**
	 * The {@link info.novatec.inspectit.rcp.editor.table.TableSubView} might need to alter the
	 * column width/visibility if the column has the remembered size. With this method the
	 * controller gives or denies the {@link info.novatec.inspectit.rcp.editor.table.TableSubView}
	 * to alter the column width.
	 * 
	 * @param tableColumn
	 *            {@link TableColumn}
	 * 
	 * @return Returns true if the {@link TableColumn} can be altered.
	 */
	boolean canAlterColumnWidth(TableColumn tableColumn);

	/**
	 * Generates and returns the input for the table. Returning <code>null</code> is possible and
	 * indicates most of the time that there is no default list or object to display in the table.
	 * For some {@link DefaultData} objects, the method {@link #canOpenInput(List)} should return
	 * true so that the input object is set by the
	 * {@link info.novatec.inspectit.rcp.editor.table.TableSubView}.
	 * 
	 * @return The table input or <code>null</code> if nothing to display for default.
	 */
	Object getTableInput();

	/**
	 * Returns the content provider for the {@link TableViewer}.
	 * 
	 * @return The content provider.
	 * @see IContentProvider
	 */
	IContentProvider getContentProvider();

	/**
	 * Returns the label provider for the {@link TableViewer}.
	 * 
	 * @return The label provider
	 * @see IBaseLabelProvider
	 */
	IBaseLabelProvider getLabelProvider();

	/**
	 * Returns the comparator for the {@link TableViewer}. Can be <code>null</code> to indicate that
	 * no sorting of the elements should be done.
	 * 
	 * @return The table viewer comparator.
	 */
	ViewerComparator getComparator();

	/**
	 * Sets the limit of the displayed elements in the table.
	 * 
	 * @param limit
	 *            The limit value.
	 */
	void setLimit(int limit);

	/**
	 * Refreshes the current data and updates the table input if new items are available.
	 * 
	 * @param monitor
	 *            The progress monitor.
	 * @param rootEditor
	 *            RootEditor of the view that is being refreshed.
	 */
	void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor);

	/**
	 * This method will be called when a double click event is executed.
	 * 
	 * @param event
	 *            The event object.
	 */
	void doubleClick(DoubleClickEvent event);

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
	 * table model).
	 * 
	 * @param object
	 *            The object to create the string from.
	 * @return The created human readable string.
	 */
	String getReadableString(Object object);

	/**
	 * Return the values of all columns in the table for the given object. Not visible columns
	 * values will also be included. The order of the values will be same to the initial table
	 * column order, thus not reflecting the current state of the table if the columns were moved.
	 * 
	 * @param object
	 *            Object to get values for.
	 * @return List of string representing the values.
	 */
	List<String> getColumnValues(Object object);

	/**
	 * Returns the list of the objects that should be searched.
	 * 
	 * @param tableInput
	 *            Current input of the table. The {@link TableInputController} is responsible to
	 *            modify the input if necessary.
	 * @return Returns the list of the objects that should be searched.
	 */
	Object[] getObjectsToSearch(Object tableInput);

	/**
	 * Disposes the table input.
	 */
	void dispose();

	/**
	 * Signals that the object has been check so that input controller can perform necessary
	 * actions.
	 * 
	 * @param object
	 *            Object that has be checked.
	 * @param checked
	 *            True if object is checked, false if it is not.
	 */
	void objectChecked(Object object, boolean checked);

	/**
	 * @return If the table should apply the SWT.CHECK style.
	 */
	boolean isCheckStyle();

	/**
	 * @return The initial state of check box for all elements.
	 */
	boolean areItemsInitiallyChecked();

}
