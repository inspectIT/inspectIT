package rocks.inspectit.ui.rcp.ci.form.part;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.sensor.StringConstraintSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;

/**
 * Part for displaying JMX sensor options.
 *
 * @author Ivan Senic
 *
 */
public class JmxSensorOptionsPart extends SectionPart implements IPropertyListener {

	/**
	 * Environment to be edited.
	 */
	private Environment environment;

	/**
	 * Form page part belongs to.
	 */
	private final FormPage formPage;

	/**
	 * {@link ConfigurationComponent}.
	 */
	private ConfigurationComponent configurationComponent;

	/**
	 * Default constructor.
	 *
	 * @param formPage
	 *            {@link FormPage} section belongs to.
	 * @param parent
	 *            Parent composite.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param style
	 *            Style used for creating the section.
	 */
	public JmxSensorOptionsPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createClient(getSection(), toolkit);

		// text and description on our own
		getSection().setText("JMX Sensor Options");
		Label label = toolkit.createLabel(getSection(), "Define options for JMX sensor");
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		getSection().setDescriptionControl(label);
	}

	/**
	 * Creates complete client.
	 *
	 * @param section
	 *            {@link Section}
	 * @param toolkit
	 *            {@link FormToolkit}
	 */
	private void createClient(Section section, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(section);
		int layoutColumns = 3;
		GridLayout gridLayout = new GridLayout(layoutColumns, false);
		gridLayout.horizontalSpacing = 10;
		mainComposite.setLayout(gridLayout);
		section.setClient(mainComposite);

		// listeners
		Listener dirtyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!isDirty()) {
					markDirty();
				}
			}
		};

		configurationComponent = new ConfigurationComponent(environment.getJmxSensorConfig());
		configurationComponent.createComponent(mainComposite, toolkit, layoutColumns);
		configurationComponent.addDirtyListener(dirtyListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);

			configurationComponent.update();
		}
	}

	/**
	 * @return Return {@link GridData} with the horizontal ident.
	 */
	private static Object getIndentGridData() {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.horizontalIndent = 20;
		return gd;
	}

	/**
	 * Creates info icon with given text as tool-tip.
	 *
	 * @param parent
	 *            Composite to create on.
	 * @param toolkit
	 *            {@link FormToolkit} to use.
	 * @param text
	 *            Information text.
	 */
	private static void createInfoLabel(Composite parent, FormToolkit toolkit, String text) {
		Label label = toolkit.createLabel(parent, "");
		if (null != text) {
			label.setToolTipText(text);
			label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		}
	}

	/**
	 * Help class for managing {@link StringConstraintSensorConfig}s.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class ConfigurationComponent {

		/**
		 * Force text to display.
		 */
		private static final String FORCE_TEXT = "Force";

		/**
		 * No-force text to display.
		 */
		private static final String NO_FORCE_TEXT = "Don't force";

		/**
		 * Sensor config.
		 */
		private JmxSensorConfig sensorConfig;

		/**
		 * If sensor is active.
		 */
		private Button activeButton;

		/**
		 * Combo for defining forcing of the server creation.
		 */
		private Combo forceCombo;

		/**
		 * Default constructor.
		 *
		 * @param sensorConfig
		 *            Sensor config.
		 */
		ConfigurationComponent(JmxSensorConfig sensorConfig) {
			this.sensorConfig = sensorConfig;
		}

		/**
		 * Creates component.
		 *
		 * @param parent
		 *            Parent composite
		 * @param toolkit
		 *            {@link FormToolkit}m
		 * @param layoutColumns
		 *            How much columns should component occupy.
		 */
		public void createComponent(Composite parent, FormToolkit toolkit, int layoutColumns) {
			// title part
			FormText timerText = toolkit.createFormText(parent, false);
			timerText.setText("<form><p><img href=\"img\" /> <b>" + TextFormatter.getSensorConfigName(sensorConfig) + "</b></p></form>", true, false);
			timerText.setImage("img", ImageFormatter.getSensorConfigImage(sensorConfig));
			timerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, layoutColumns, 1));

			// widgets
			toolkit.createLabel(parent, "Active:").setLayoutData(getIndentGridData());
			activeButton = toolkit.createButton(parent, "Yes", SWT.CHECK);
			createInfoLabel(parent, toolkit,
					"Defines if sensor is active. Note that when sensor is active it will check all the MBean servers for possible beans/attributes to monitor. If you don't plan to monitor JMX beans it's recommended that you keep sensor inactive.");

			toolkit.createLabel(parent, "MBean server creation:").setLayoutData(getIndentGridData());
			forceCombo = new Combo(parent, SWT.READ_ONLY);
			forceCombo.add(NO_FORCE_TEXT);
			forceCombo.add(FORCE_TEXT);
			forceCombo.setData(NO_FORCE_TEXT, false);
			forceCombo.setData(FORCE_TEXT, true);
			toolkit.adapt(forceCombo, false, false);
			createInfoLabel(parent, toolkit,
					"Defines if inspectIT should force the creation of the default MBean server. On some application servers forcing the creation can be problematic. On the other hand, if the creation is not forced and no application component is creating the MBean server(s), then monitoring of the MBeans will not be possible as the server(s) will not created.");

			activeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					forceCombo.setEnabled(activeButton.getSelection());
				}
			});

			// fill with current value
			fillValue();
		}

		/**
		 * Adds dirty listener to needed controls.
		 *
		 * @param listener
		 *            Dirty listener.
		 */
		public void addDirtyListener(Listener listener) {
			activeButton.addListener(SWT.Selection, listener);
			forceCombo.addListener(SWT.Selection, listener);
		}

		/**
		 * Updates config.
		 *
		 */
		public void update() {
			sensorConfig.setActive(activeButton.getSelection());
			sensorConfig.setForceMBeanServer((Boolean) forceCombo.getData(forceCombo.getText()));
		}

		/**
		 * Fills the values.
		 */
		private void fillValue() {
			activeButton.setSelection(sensorConfig.isActive());
			forceCombo.setEnabled(sensorConfig.isActive());
			int index;
			if (sensorConfig.isForceMBeanServer()) {
				index = forceCombo.indexOf(FORCE_TEXT);
			} else {
				index = forceCombo.indexOf(NO_FORCE_TEXT);
			}
			forceCombo.select(index);
		}

		/**
		 * Updates the sensor config if it's relating to the same class.
		 *
		 * @param sensorConfig
		 *            new config
		 * @return <code>true</code> if update occurred
		 */
		public boolean updateSensorConfig(JmxSensorConfig sensorConfig) {
			if (Objects.equals(this.sensorConfig.getClassName(), sensorConfig.getClassName())) {
				this.sensorConfig = sensorConfig;
				return true;
			}

			return false;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
			environment = input.getEnvironment();
			configurationComponent.updateSensorConfig(environment.getJmxSensorConfig());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		super.dispose();
	}

}
