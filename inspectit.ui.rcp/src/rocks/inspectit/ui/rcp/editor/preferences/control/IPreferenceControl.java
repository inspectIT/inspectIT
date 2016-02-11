package info.novatec.inspectit.rcp.editor.preferences.control;

import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The interface for all concrete preference control creators.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public interface IPreferenceControl {

	/**
	 * Returns the unique {@link PreferenceId} of this preference control group.
	 * 
	 * @return The unique {@link PreferenceId} of this preference control group.
	 */
	PreferenceId getControlGroupId();

	/**
	 * Creates the controls of the control group.
	 * 
	 * @param parent
	 *            The {@link Composite} to which the controls will be added.
	 * @param toolkit
	 *            The used toolkit.
	 * @return The {@link Composite} containing the controls.
	 */
	Composite createControls(Composite parent, FormToolkit toolkit);

	/**
	 * This method gets called when pressing the Update Button in the global control of the panel.
	 * The method gets the actual data values from all controls and puts them in a {@link Map}.
	 * 
	 * @return The {@link Map} containing the data values from the control items.
	 */
	Map<IPreferenceGroup, Object> eventFired();

	/**
	 * Disposes the preference control.
	 */
	void dispose();

}
