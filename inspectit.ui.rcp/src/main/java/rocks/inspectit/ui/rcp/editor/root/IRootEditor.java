package rocks.inspectit.ui.rcp.editor.root;

import java.util.List;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.ui.rcp.editor.ISubView;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.IPreferencePanel;

/**
 * Interface for all root editors.
 *
 * @author Patrice Bouillet
 *
 */
public interface IRootEditor {

	/**
	 * Refresh the view.
	 */
	void doRefresh();

	/**
	 * Returns the sub view registered for this root editor.
	 *
	 * @return the sub view.
	 */
	ISubView getSubView();

	/**
	 * Sets the current active sub view.
	 *
	 * @param subView
	 *            The sub view.
	 */
	void setActiveSubView(ISubView subView);

	/**
	 * Returns the current active sub view. One editor can only have one sub view child, but this
	 * child can act as a composite with other sub views contained in it. So this method returns the
	 * sub view which is currently active somewhere in one of the composite sub-view child elements
	 * (if there are any).
	 *
	 * @return The active sub view.
	 */
	ISubView getActiveSubView();

	/**
	 * Returns the input definition for this view.
	 *
	 * @return The input definition.
	 */
	InputDefinition getInputDefinition();

	/**
	 * This will set the data input of the view. Every view can initialize itself with some data
	 * (like live data from the server). This is only needed if some specific needs to be displayed.
	 *
	 * @param data
	 *            The list of {@link DefaultData} objects.
	 */
	void setDataInput(List<? extends Object> data);

	/**
	 * Returns the preference panel.
	 *
	 * @return The preference panel.
	 */
	IPreferencePanel getPreferencePanel();

}