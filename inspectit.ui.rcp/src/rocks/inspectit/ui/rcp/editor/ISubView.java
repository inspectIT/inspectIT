package info.novatec.inspectit.rcp.editor;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Interface used by all sub-views which are creating the final view.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface ISubView {

	/**
	 * Sets the root editor for this sub view. This is needed for event handling purposes or the
	 * access to the preference area.
	 * 
	 * @param rootEditor
	 *            The root editor.
	 */
	void setRootEditor(AbstractRootEditor rootEditor);

	/**
	 * Returns the root editor.
	 * 
	 * @return The root editor.
	 */
	AbstractRootEditor getRootEditor();

	/**
	 * Informs the sub-view has to do all initialization tasks. This method will be called after the
	 * {@link #setRootEditor(AbstractRootEditor)} has been executed, so that sub-view can get all
	 * necessary information from {@link AbstractRootEditor}.
	 */
	void init();

	/**
	 * Creates the part control of this view.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit which is used for defining the colors of the widgets. Can be
	 *            <code>null</code> to indicate that there is no toolkit.
	 */
	void createPartControl(Composite parent, FormToolkit toolkit);

	/**
	 * A sub-view should return all preference IDs itself is in need of and the ones of the children
	 * (it is a sub-view containing other views).
	 * 
	 * @return A {@link Set} containing all {@link PreferenceId}. Returning <code>null</code> is not
	 *         permitted here. At least a {@link java.util.Collections#EMPTY_SET} should be
	 *         returned.
	 */
	Set<PreferenceId> getPreferenceIds();

	/**
	 * Every sub-view contains some logic to retrieve the data on its own. This method invokes the
	 * refresh process which should update the view.
	 * <p>
	 * For some views, it is possible that they do not show or do anything for default.
	 */
	void doRefresh();

	/**
	 * This method is called whenever something is changed in one of the preferences.
	 * 
	 * @param preferenceEvent
	 *            The event object containing the changed objects.
	 */
	void preferenceEventFired(PreferenceEvent preferenceEvent);

	/**
	 * This will set the data input of the view. Every view can initialize itself with some data
	 * (like live data from the server). This is only needed if some specific needs to be displayed.
	 * 
	 * @param data
	 *            The list of {@link DefaultData} objects.
	 */
	void setDataInput(List<? extends DefaultData> data);

	/**
	 * Returns the control class of this view controller.
	 * 
	 * @return The {@link Control} class.
	 */
	Control getControl();

	/**
	 * Returns the selection provider for this view.
	 * 
	 * @return The selection provider.
	 */
	ISelectionProvider getSelectionProvider();

	/**
	 * Selects the given {@link ISubView} if it exists. The composite sub views should check if the
	 * given {@link ISubView} is one of the containing views and select it. Non-composite sub views
	 * should not do anything.
	 * 
	 * @param subView
	 *            {@link ISubView} to select.
	 */
	void select(ISubView subView);

	/**
	 * Returns the sub view of specific class. The composite sub views should check if the given
	 * {@link ISubView} is one of the given class and return it. Non-composite sub views should
	 * check if they are of the given class and return them self if so.
	 * 
	 * @param <E>
	 *            Type of sub view.
	 * @param clazz
	 *            Sub view class to search for.
	 * @return {@link ISubView} of given class or <code>null</code> if view can not be found.
	 */
	<E extends ISubView> E getSubView(Class<E> clazz);

	/**
	 * Returns the sub view that has given input controller class if exists. The composite sub views
	 * should check if the given {@link ISubView} is one of the given contained and return it.
	 * Non-composite sub views should check if they have the controller and return them self if so.
	 * 
	 * @param inputControllerClass
	 *            Class of the input controller that sub view to search for has.
	 * @return {@link ISubView} of or <code>null</code> if view can not be found.
	 */
	ISubView getSubViewWithInputController(Class<?> inputControllerClass);

	/**
	 * Disposes this sub-view.
	 */
	void dispose();

}
