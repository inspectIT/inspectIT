package info.novatec.inspectit.rcp.editor.preferences.control;

import info.novatec.inspectit.rcp.editor.preferences.IPreferencePanel;

/**
 * Abstract class for all preference controls.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractPreferenceControl implements IPreferenceControl {

	/**
	 * Preference panel.
	 */
	private IPreferencePanel preferencePanel;

	/**
	 * Default constructor.
	 * 
	 * @param preferencePanel
	 *            Preference panel.
	 */
	public AbstractPreferenceControl(IPreferencePanel preferencePanel) {
		this.preferencePanel = preferencePanel;
	}

	/**
	 * Gets {@link #preferencePanel}.
	 * 
	 * @return {@link #preferencePanel}
	 */
	protected IPreferencePanel getPreferencePanel() {
		return preferencePanel;
	}

}
