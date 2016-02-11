package info.novatec.inspectit.rcp.editor.preferences.control;

import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.IPreferencePanel;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * This class creates a control group with a sampling rate slider in the
 * {@link info.novatec.inspectit.rcp.editor.preferences.FormPreferencePanel}.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class SamplingRateControl extends AbstractPreferenceControl implements IPreferenceControl {

	/**
	 * The unique id of this preference control.
	 */
	private static final PreferenceId CONTROL_GROUP_ID = PreferenceId.SAMPLINGRATE;

	/**
	 * The sampling rate slider.
	 */
	private Scale slider = null;

	/**
	 * The radio button for timeframe selection.
	 */
	private Button timeframeModeButton = null;

	/**
	 * The available sensitivity modes.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum Sensitivity {
		/** No sensitivity. */
		NO_SENSITIVITY(0),
		/** 'Very fine' sensitivity. */
		VERY_FINE(200),
		/** 'Fine' sensitivity. */
		FINE(120),
		/** 'Medium' sensitivity. */
		MEDIUM(75),
		/** 'Coarse' sensitivity. */
		COARSE(30),
		/** 'Very coarse' sensitivity. */
		VERY_COARSE(15);

		/**
		 * The value.
		 */
		private int value;

		/**
		 * The constructor needing the specific value.
		 * 
		 * @param value
		 *            The value.
		 */
		private Sensitivity(int value) {
			this.value = value;
		}

		/**
		 * Returns the sensitivity value.
		 * 
		 * @return The value.
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Converts an ordinal into a {@link Sensitivity}.
		 * 
		 * @param i
		 *            The ordinal.
		 * @return The appropriate column.
		 */
		public static Sensitivity fromOrd(int i) {
			if (i < 0 || i >= Sensitivity.values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Sensitivity.values()[i];
		}
	}

	/**
	 * The default sensitivity.
	 */
	public static final Sensitivity DEFAULT_SENSITIVITY = Sensitivity.MEDIUM;

	/**
	 * Default constructor.
	 * 
	 * @param preferencePanel
	 *            Preference panel.
	 */
	public SamplingRateControl(IPreferencePanel preferencePanel) {
		super(preferencePanel);
	}

	/**
	 * {@inheritDoc}
	 */
	public Composite createControls(Composite parent, FormToolkit toolkit) {
		Section section = toolkit.createSection(parent, Section.TITLE_BAR);
		section.setText("Sampling Rate");
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		Composite composite = toolkit.createComposite(section);
		section.setClient(composite);

		GridLayout layout = new GridLayout(4, false);
		layout.marginLeft = 10;
		layout.horizontalSpacing = 10;
		composite.setLayout(layout);
		GridData gridData = new GridData(SWT.MAX, SWT.DEFAULT);
		gridData.grabExcessHorizontalSpace = true;
		composite.setLayoutData(gridData);

		final Label sliderLabel = toolkit.createLabel(composite, "no sensitivity selected", SWT.LEFT);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = sliderLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		sliderLabel.setLayoutData(data);

		slider = new Scale(composite, SWT.HORIZONTAL);
		toolkit.adapt(slider, true, true);
		slider.setMinimum(0);
		slider.setMaximum(Sensitivity.values().length - 1);
		slider.setIncrement(1);
		slider.setSize(200, 10);
		slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		slider.addSelectionListener(new SelectionAdapter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent event) {
				Sensitivity sensitivity = Sensitivity.fromOrd(slider.getSelection());

				switch (sensitivity) {
				case NO_SENSITIVITY:
					sliderLabel.setText("no sensitivity selected");
					break;
				case VERY_FINE:
					sliderLabel.setText("very fine");
					break;
				case FINE:
					sliderLabel.setText("fine");
					break;
				case MEDIUM:
					sliderLabel.setText("medium");
					break;
				case COARSE:
					sliderLabel.setText("coarse");
					break;
				case VERY_COARSE:
					sliderLabel.setText("very coarse");
					break;
				default:
					break;
				}
			}
		});
		slider.setSelection(DEFAULT_SENSITIVITY.ordinal());
		slider.notifyListeners(SWT.Selection, null);

		Label modeLabel = toolkit.createLabel(composite, "Sampling Rate Mode: ", SWT.LEFT);
		modeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		timeframeModeButton = toolkit.createButton(composite, "Timeframe dividing", SWT.RADIO);
		timeframeModeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		timeframeModeButton.setSelection(true);

		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<IPreferenceGroup, Object> eventFired() {
		Sensitivity sensitivity = Sensitivity.fromOrd(slider.getSelection());

		Map<IPreferenceGroup, Object> preferenceControlMap = new HashMap<IPreferenceGroup, Object>();
		preferenceControlMap.put(PreferenceId.SamplingRate.SLIDER_ID, sensitivity);

		// get the actual selected divider mode button and set the specific id
		if (timeframeModeButton.getSelection()) {
			preferenceControlMap.put(PreferenceId.SamplingRate.DIVIDER_ID, PreferenceId.SamplingRate.TIMEFRAME_DIVIDER_ID);
		}

		return preferenceControlMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public PreferenceId getControlGroupId() {
		return CONTROL_GROUP_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
