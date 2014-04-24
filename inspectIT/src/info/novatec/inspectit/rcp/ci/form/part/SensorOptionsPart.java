package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.sensor.StringConstraintSensorConfig;
import info.novatec.inspectit.ci.sensor.exception.impl.ExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.IMethodSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.HttpSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.StatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.TimerSensorConfig;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Part for displaying sensor options.
 * 
 * @author Ivan Senic
 * 
 */
public class SensorOptionsPart extends SectionPart implements IPropertyListener {

	/**
	 * Environment to be edited.
	 */
	private Environment environment;

	/**
	 * Exception sensor simple option.
	 */
	private Button exceptionSimple;

	/**
	 * Exception sensor enhanced option.
	 */
	private Button exceptionEnhanced;

	/**
	 * All {@link StringConstraintComponent} on the page.
	 */
	private List<StringConstraintComponent> constrainComponents = new ArrayList<>();

	/**
	 * Form page.
	 */
	private FormPage formPage;

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
	public SensorOptionsPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		this.formPage = formPage;
		getSection().setText("Sensor Options");
		getSection().setDescription("Define options for specific sensors");
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		createClient(getSection(), toolkit);
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
		int layoutColumns = 4;
		GridLayout gridLayout = new GridLayout(layoutColumns, false);
		gridLayout.horizontalSpacing = 10;
		mainComposite.setLayout(gridLayout);
		section.setClient(mainComposite);

		// method sensors
		for (IMethodSensorConfig methodSensorConfig : environment.getMethodSensorConfigs()) {
			if (methodSensorConfig instanceof StringConstraintSensorConfig) {
				StringConstraintComponent stringConstraintComponent = new StringConstraintComponent((StringConstraintSensorConfig) methodSensorConfig);
				constrainComponents.add(stringConstraintComponent);

				if (methodSensorConfig instanceof TimerSensorConfig) {
					stringConstraintComponent.createComponet(mainComposite, toolkit, "String length of captured context(s):",
							"Defines the maximum string length of captured context (parameters, fields, return values) for timer sensor.", layoutColumns);
				} else if (methodSensorConfig instanceof HttpSensorConfig) {
					stringConstraintComponent.createComponet(mainComposite, toolkit, "String length of captured HTTP data:",
							"Defines the maximum string length of captured HTTP data (parameters, headers, attributes, etc) for HTTP sensor.", layoutColumns);
				} else if (methodSensorConfig instanceof InvocationSequenceSensorConfig) {
					stringConstraintComponent.createComponet(mainComposite, toolkit, "String length of captured context(s):",
							"Defines the maximum string length of captured context (parameters, fields, return values) for invocation sensor.", layoutColumns);
				} else if (methodSensorConfig instanceof PreparedStatementSensorConfig) {
					stringConstraintComponent.createComponet(mainComposite, toolkit, "String length of SQLs:",
							"Defines the maximum string length of SQL strings and parameters for prepared statement sensor.", layoutColumns);
				} else if (methodSensorConfig instanceof StatementSensorConfig) {
					stringConstraintComponent.createComponet(mainComposite, toolkit, "String length of SQLs:", "Defines the maximum string length of SQL strings and parameters for statement sensor.",
							layoutColumns);
				}
			}
		}

		// exception sensor
		ExceptionSensorConfig exceptionSensorConfig = (ExceptionSensorConfig) environment.getExceptionSensorConfig();
		StringConstraintComponent stringConstraintComponent = new StringConstraintComponent(exceptionSensorConfig);
		constrainComponents.add(stringConstraintComponent);
		stringConstraintComponent.createComponet(mainComposite, toolkit, "String length of stack trace & messages:",
				"Defines the maximum string length of captured stack traces and exception messages for exception sensor.", layoutColumns);

		toolkit.createLabel(mainComposite, "Mode:").setLayoutData(getIndentGridData());
		Composite radioGroupHelp = toolkit.createComposite(mainComposite);
		radioGroupHelp.setLayout(getLayoutForRadioGroup());
		radioGroupHelp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		exceptionSimple = toolkit.createButton(radioGroupHelp, "Simple", SWT.RADIO);
		exceptionSimple.setSelection(true);
		exceptionSimple.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		exceptionEnhanced = toolkit.createButton(radioGroupHelp, "Enhanced", SWT.RADIO);
		toolkit.createLabel(mainComposite, "");
		createInfoLabel(mainComposite, toolkit,
				"Simple mode will only provide the information where have the exceptions been created. All other informations like passed and handled will be available when enhanced mode if on. But, with simple mode there will be no performance overhead for using the exception sensor.");

		// set selection for simple / enhanced
		exceptionSimple.setSelection(!exceptionSensorConfig.isEnhanced());
		exceptionEnhanced.setSelection(exceptionSensorConfig.isEnhanced());

		// listeners
		Listener dirtyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!isDirty()) {
					markDirty();
				}
			}
		};
		for (StringConstraintComponent constraintComponent : constrainComponents) {
			constraintComponent.addDirtyListener(dirtyListener);
		}
		exceptionSimple.addListener(SWT.Selection, dirtyListener);
		exceptionEnhanced.addListener(SWT.Selection, dirtyListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		getManagedForm().dirtyStateChanged();

		if (onSave) {
			for (StringConstraintComponent constraintComponent : constrainComponents) {
				constraintComponent.validateUpdate(true);
			}
			((ExceptionSensorConfig) environment.getExceptionSensorConfig()).setEnhanced(exceptionEnhanced.getSelection());
			getManagedForm().dirtyStateChanged();
		}
	}

	/**
	 * @return Layout for radio group.
	 */
	private Layout getLayoutForRadioGroup() {
		GridLayout gl = new GridLayout(2, true);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		return gl;
	}

	/**
	 * @return Return {@link GridData} with the horizontal ident.
	 */
	private Object getIndentGridData() {
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
	private void createInfoLabel(Composite parent, FormToolkit toolkit, String text) {
		Label label = toolkit.createLabel(parent, "");
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
	}

	/**
	 * Help class for managing {@link StringConstraintSensorConfig}s.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class StringConstraintComponent {

		/**
		 * Sensor config.
		 */
		private StringConstraintSensorConfig sensorConfig;

		/**
		 * Unlimited button.
		 */
		private Button unlimited;

		/**
		 * Limited button.
		 */
		private Button limited;

		/**
		 * Value of string length.
		 */
		private Text value;

		/**
		 * {@link ControlDecoration} for displaying validation errors.
		 */
		private ValidationControlDecoration<Text> valueDecoration;

		/**
		 * Default constructor.
		 * 
		 * @param sensorConfig
		 *            Sensor config.
		 */
		public StringConstraintComponent(StringConstraintSensorConfig sensorConfig) {
			this.sensorConfig = sensorConfig;
		}

		/**
		 * Creates component.
		 * 
		 * @param parent
		 *            Parent composite
		 * @param toolkit
		 *            {@link FormToolkit}m
		 * @param label
		 *            Main label text
		 * @param infoText
		 *            Info text
		 * @param layoutColumns
		 *            How much columns should component occupy.
		 */
		public void createComponet(Composite parent, FormToolkit toolkit, String label, String infoText, int layoutColumns) {
			// title part
			FormText timerText = toolkit.createFormText(parent, false);
			timerText.setText("<form><p><img href=\"img\" /> <b>" + TextFormatter.getSensorConfigName(sensorConfig) + "</b></p></form>", true, false);
			timerText.setImage("img", ImageFormatter.getSensorConfigImage(sensorConfig));
			timerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, layoutColumns, 1));

			// widgets
			toolkit.createLabel(parent, label).setLayoutData(getIndentGridData());
			Composite radioGroupHelp = toolkit.createComposite(parent);
			radioGroupHelp.setLayout(getLayoutForRadioGroup());
			radioGroupHelp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, layoutColumns - 3, 1));
			unlimited = toolkit.createButton(radioGroupHelp, "Unlimited", SWT.RADIO);
			unlimited.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			limited = toolkit.createButton(radioGroupHelp, "Limited", SWT.RADIO);
			limited.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			value = toolkit.createText(parent, "", SWT.BORDER | SWT.RIGHT);
			value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			createInfoLabel(parent, toolkit, infoText);
			
			// decoration
			valueDecoration = new ValidationControlDecoration<Text>(value) {
				@Override
				protected boolean validate(Text control) {
					return validateUpdate(false);
				}
			};
			valueDecoration.setDescriptionText("String length must be a positive number.");
			valueDecoration.registerListener(SWT.Modify);

			// fill with current value
			int length = sensorConfig.getStringLength();
			if (length > 0) {
				unlimited.setSelection(false);
				limited.setSelection(true);
				value.setText(String.valueOf(length));
			} else {
				unlimited.setSelection(true);
				limited.setSelection(false);
				value.setText("");
				value.setEnabled(false);
			}

			// listeners
			Listener listener = new Listener() {
				@Override
				public void handleEvent(Event event) {
					if (limited.getSelection()) {
						value.setEnabled(true);
						value.setText(String.valueOf(sensorConfig.getStringLength()));
					} else {
						value.setEnabled(false);
						value.setText("");
					}
				}
			};
			unlimited.addListener(SWT.Selection, listener);
			limited.addListener(SWT.Selection, listener);
		}

		/**
		 * Adds dirty listener to needed controls.
		 * 
		 * @param listener
		 *            Dirty listener.
		 */
		public void addDirtyListener(Listener listener) {
			unlimited.addListener(SWT.Selection, listener);
			limited.addListener(SWT.Selection, listener);
			value.addListener(SWT.Modify, listener);
		}

		/**
		 * Validates the value and updates if needed.
		 * 
		 * @param update
		 *            If beside validation an update on the model object should be done.
		 * @return if valud in the control is valid
		 */
		public boolean validateUpdate(boolean update) {
			boolean valid = true;
			if (unlimited.getSelection()) {
				if (update) {
					sensorConfig.setStringLength(0);
				}
			} else {
				try {
					int length = Integer.parseInt(value.getText());
					if (length <= 0) {
						valid = false;
					} else {
						if (update) {
							sensorConfig.setStringLength(length);
						}
					}
				} catch (NumberFormatException e) {
					valid = false;
				}
			}
			return valid;
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
		}
	}

}
