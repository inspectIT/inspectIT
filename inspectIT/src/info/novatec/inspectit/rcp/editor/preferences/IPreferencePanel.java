package info.novatec.inspectit.rcp.editor.preferences;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;

import java.util.Set;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;

/**
 * The interface for all preference panels.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface IPreferencePanel {

	/**
	 * Returns the ID of this preference panel. Each preference panel has a unique ID that can be
	 * used for a later reference.
	 * 
	 * @return Returns the ID of this preference panel. Each preference panel has a unique ID that
	 *         can be used for a later reference.
	 */
	String getId();

	/**
	 * Creates the part control of this view.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param preferenceSet
	 *            The set containing the preference IDs which are used to show the correct options.
	 * @param inputDefinition
	 *            {@link InputDefinition} of the editor where preference panel will be created.
	 * @param toolBarManager
	 *            The toolbar manager is needed if buttons are going to be displayed. Otherwise it
	 *            can be <code>null</code>.
	 * 
	 */
	void createPartControl(Composite parent, Set<PreferenceId> preferenceSet, InputDefinition inputDefinition, IToolBarManager toolBarManager);

	/**
	 * Registers a callback at this preference panel.
	 * 
	 * @param callback
	 *            The callback to register.
	 */
	void registerCallback(PreferenceEventCallback callback);

	/**
	 * Removes a callback from the preference panel.
	 * 
	 * @param callback
	 *            The callback to remove.
	 */
	void removeCallback(PreferenceEventCallback callback);

	/**
	 * Fires the event for all registered callbacks.
	 * 
	 * @param event
	 *            The event to fire.
	 */
	void fireEvent(PreferenceEvent event);

	/**
	 * Sets the visibility of the preference panel to show/hide.
	 * 
	 * @param visible
	 *            The visibility state.
	 */
	void setVisible(boolean visible);

	/**
	 * This method is called when an option is changed and should be applied to all the contained
	 * views.
	 */
	void update();

	/**
	 * Disables the live mode in the preference panel.
	 */
	void disableLiveMode();

	/**
	 * Signals that the buffer has been cleared and that all views that have register for the
	 * {@link PreferenceId#CLEAR_BUFFER} should delete input data.
	 */
	void bufferCleared();

	/**
	 * Checking the switch stepping control button on preference panel if stepping button exists.
	 * 
	 * @param checked
	 *            True to be checked, false for not checked.
	 */
	void setSteppingControlChecked(boolean checked);

	/**
	 * Disposes this view / editor.
	 */
	void dispose();

}
