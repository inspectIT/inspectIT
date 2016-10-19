package rocks.inspectit.ui.rcp.editor.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.preferences.control.AlertIdControl;
import rocks.inspectit.ui.rcp.editor.preferences.control.IPreferenceControl;
import rocks.inspectit.ui.rcp.editor.preferences.control.SamplingRateControl;
import rocks.inspectit.ui.rcp.editor.preferences.control.TimeLineControl;

/**
 * This factory creates the preference control groups and adds it to a list, because one class can
 * have more then on control group. The list with the control groups will then be returned.
 *
 * @author Eduard Tudenhoefner
 *
 */
public final class PreferenceControlFactory {

	/**
	 * The private constructor.
	 */
	private PreferenceControlFactory() {
	}

	/**
	 * Creates and returns a new instance of {@link IPreferenceControl}.
	 *
	 * @param parent
	 *            The {@link Composite} used to draw the elements to.
	 * @param toolkit
	 *            The used toolkit.
	 * @param preferenceIdEnum
	 *            The {@link PreferenceId} by which the {@link IPreferenceControl} will be created.
	 * @param preferencePanel
	 *            Preference panel
	 * @param inputDefinition
	 *            {@link InputDefinition} instance.
	 * @return An instance of {@link IPreferenceControl}.
	 */
	public static IPreferenceControl createPreferenceControls(Composite parent, FormToolkit toolkit, PreferenceId preferenceIdEnum, IPreferencePanel preferencePanel, InputDefinition inputDefinition) {
		switch (preferenceIdEnum) {
		case TIMELINE:
			IPreferenceControl timeLineControl = new TimeLineControl(preferencePanel);
			timeLineControl.createControls(parent, toolkit);
			return timeLineControl;
		case SAMPLINGRATE:
			IPreferenceControl samplingRateControl = new SamplingRateControl(preferencePanel);
			samplingRateControl.createControls(parent, toolkit);
			return samplingRateControl;
		case ALERT_INFO:
			if (inputDefinition.hasInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.ALERT_EXTRAS_MARKER)) {
				IPreferenceControl alertIdControl = new AlertIdControl(preferencePanel, inputDefinition.getInputDefinitionExtra(InputDefinitionExtrasMarkerFactory.ALERT_EXTRAS_MARKER).getAlertId());
				alertIdControl.createControls(parent, toolkit);
				return alertIdControl;
			} else {
				throw new IllegalStateException("Input definition must have an ALERT_EXTRAS_MARKER!");
			}
		default:
			return null;
		}
	}

}
