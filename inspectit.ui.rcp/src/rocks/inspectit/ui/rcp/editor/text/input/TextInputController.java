package info.novatec.inspectit.rcp.editor.text.input;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * The controller for all text inputs.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface TextInputController {

	/**
	 * Sets the input definition of this controller.
	 * 
	 * @param inputDefinition
	 *            The input definition.
	 */
	void setInputDefinition(InputDefinition inputDefinition);

	/**
	 * Returns an object containing the composite with the whole input.
	 * 
	 * @param parent
	 *            The parent used to draw the elements to.
	 * @param toolkit
	 *            The form toolkit.
	 */
	void createPartControl(Composite parent, FormToolkit toolkit);

	/**
	 * The do refresh method is called at least one time to fill the labels with some initial data.
	 * It depends on several settings if this method is called repeatedly.
	 * <p>
	 * <b>Note that this method is not called in the UI thread because it is expected that this can
	 * be long running operation. Any access to the widgets in this method must be run in UI thread
	 * to ensure no InvalidThreadException occurs.</b>
	 */
	void doRefresh();

	/**
	 * Disposes this view / editor.
	 */
	void dispose();

	/**
	 * This method is called when the input of the
	 * {@link info.novatec.inspectit.rcp.editor.text.TextSubView} has been changed.
	 * 
	 * @param data
	 *            New input.
	 */
	void setDataInput(List<? extends DefaultData> data);

}
