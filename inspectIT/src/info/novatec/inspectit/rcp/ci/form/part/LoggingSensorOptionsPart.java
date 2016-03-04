package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.sensor.StringConstraintSensorConfig;
import info.novatec.inspectit.ci.sensor.method.ILoggingSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

/**
 * Part for displaying logging sensor options.
 *
 * @author Ivan Senic
 *
 */
public class LoggingSensorOptionsPart extends SectionPart implements IPropertyListener {

	/**
	 * Environment to be edited.
	 */
	private Environment environment;

	/**
	 * All {@link LoggingConfigurationComponent}s on the page.
	 */
	private final List<LoggingConfigurationComponent> logConfigComponents = new ArrayList<>();

	/**
	 * Form page part belongs to.
	 */
	private final FormPage formPage;

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
	public LoggingSensorOptionsPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createClient(getSection(), toolkit);

		// text and description on our own
		getSection().setText("Logging Sensor Options");
		Label label = toolkit.createLabel(getSection(), "Define options for logging sensors");
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

		// method sensors
		for (IMethodSensorConfig sensorConfig : environment.getMethodSensorConfigs()) {
			if (sensorConfig instanceof ILoggingSensorConfig) {
				LoggingConfigurationComponent logConfigComponent = new LoggingConfigurationComponent((ILoggingSensorConfig) sensorConfig);
				logConfigComponents.add(logConfigComponent);
				logConfigComponent.createComponent(mainComposite, toolkit,
						"Select the minimum level that a logging needs to have in order to be captured. For example, when selecting WARN all log with FATAL, ERROR or WARN level will be captured by inspectIT.",
						layoutColumns);
			}
		}

		// listeners
		Listener dirtyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!isDirty()) {
					markDirty();
				}
			}
		};
		for (LoggingConfigurationComponent logComponent : logConfigComponents) {
			logComponent.addDirtyListener(dirtyListener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);

			for (LoggingConfigurationComponent logComponent : logConfigComponents) {
				logComponent.update();
			}
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
	private static class LoggingConfigurationComponent {

		/**
		 * Sensor config.
		 */
		private ILoggingSensorConfig sensorConfig;

		/**
		 * Combo for defining the minimum level.
		 */
		private Combo levelCombo;

		/**
		 * Default constructor.
		 *
		 * @param sensorConfig
		 *            Sensor config.
		 */
		LoggingConfigurationComponent(ILoggingSensorConfig sensorConfig) {
			this.sensorConfig = sensorConfig;
		}

		/**
		 * Creates component.
		 *
		 * @param parent
		 *            Parent composite
		 * @param toolkit
		 *            {@link FormToolkit}m
		 * @param infoText
		 *            Info text
		 * @param layoutColumns
		 *            How much columns should component occupy.
		 */
		public void createComponent(Composite parent, FormToolkit toolkit, String infoText, int layoutColumns) {
			// title part
			FormText timerText = toolkit.createFormText(parent, false);
			timerText.setText("<form><p><img href=\"img\" /> <b>" + sensorConfig.getTechnologyName() + "</b></p></form>", true, false);
			timerText.setImage("img", ImageFormatter.getSensorConfigImage(sensorConfig));
			timerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, layoutColumns, 1));

			// widgets
			toolkit.createLabel(parent, "Minimum level:").setLayoutData(getIndentGridData());

			levelCombo = new Combo(parent, SWT.READ_ONLY);
			toolkit.adapt(levelCombo, false, false);

			for (String levels : sensorConfig.getLogLevels()) {
				levelCombo.add(levels);
			}

			createInfoLabel(parent, toolkit, infoText);

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
			levelCombo.addListener(SWT.Selection, listener);
		}

		/**
		 * Updates config.
		 *
		 */
		public void update() {
			sensorConfig.setMinLevel(levelCombo.getText());
		}

		/**
		 * Fills the min level value.
		 */
		private void fillValue() {
			String level = sensorConfig.getMinLevel();
			levelCombo.select(levelCombo.indexOf(level));
		}

		/**
		 * Updates the sensor config if it's relating to the same class.
		 *
		 * @param sensorConfig
		 *            new config
		 * @return <code>true</code> if update occurred
		 */
		public boolean updateSensorConfig(ILoggingSensorConfig sensorConfig) {
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

			for (LoggingConfigurationComponent logComponent : logConfigComponents) {
				for (IMethodSensorConfig methodSensorConfig : environment.getMethodSensorConfigs()) {
					if (methodSensorConfig instanceof ILoggingSensorConfig) {
						if (logComponent.updateSensorConfig((ILoggingSensorConfig) methodSensorConfig)) {
							break;
						}
					}
				}
			}
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
