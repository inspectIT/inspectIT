package rocks.inspectit.ui.rcp.editor.preferences.control;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.editor.preferences.IPreferenceGroup;
import rocks.inspectit.ui.rcp.editor.preferences.IPreferencePanel;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.root.FormRootEditor;

/**
 * Control for editing the alert id.
 * 
 * @author Alexander Wert
 *
 */
public class AlertIdControl extends AbstractPreferenceControl implements IPreferenceControl, PreferenceEventCallback {

	/**
	 * Input field for alert id.
	 */
	private Text inputField;

	/**
	 * The initial alert id.
	 */
	private String alertId;

	/**
	 * Constructor.
	 *
	 * @param preferencePanel
	 *            Preference panel.
	 * @param alertId
	 *            The alert id.
	 */
	public AlertIdControl(IPreferencePanel preferencePanel, String alertId) {
		super(preferencePanel);
		this.alertId = alertId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void eventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.ALERT_INFO.equals(preferenceEvent.getPreferenceId())) {
			inputField.setText(preferenceEvent.getPreferenceMap().get(PreferenceId.AlertInformation.ALERT_ID).toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PreferenceId getControlGroupId() {
		return PreferenceId.ALERT_INFO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Composite createControls(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		section.setText("Alert Identifier");
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Composite mainComposite = toolkit.createComposite(section);
		mainComposite.setLayout(new GridLayout(2, false));
		section.setClient(mainComposite);

		toolkit.createLabel(mainComposite, "Alert identifier:");
		inputField = toolkit.createText(mainComposite, "");
		SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				getPreferencePanel().update();
				IEditorPart editor = InspectIT.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (editor instanceof FormRootEditor) {
					((FormRootEditor) editor).updateEditorName(inputField.getText());
				}
			}
		};
		inputField.addSelectionListener(selectionListener);

		inputField.setText(alertId);

		getPreferencePanel().registerCallback(this);

		return mainComposite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<IPreferenceGroup, Object> eventFired() {
		Map<IPreferenceGroup, Object> preferenceControlMap = new HashMap<>();
		preferenceControlMap.put(PreferenceId.AlertInformation.ALERT_ID, inputField.getText());
		return preferenceControlMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		getPreferencePanel().removeCallback(this);
	}

}
