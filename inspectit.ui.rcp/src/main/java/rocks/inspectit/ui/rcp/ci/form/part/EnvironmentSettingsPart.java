package rocks.inspectit.ui.rcp.ci.form.part;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import rocks.inspectit.shared.all.instrumentation.config.impl.RetransformationStrategy;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;

/**
 * Part for defining the environment general setting.
 *
 * @author Ivan Senic
 *
 */
public class EnvironmentSettingsPart extends SectionPart implements IPropertyListener {

	/**
	 * Form page.
	 */
	private FormPage formPage;

	/**
	 * Environment being edited.
	 */
	private Environment environment;

	/**
	 * {@link ControlDecoration} for displaying validation errors for buffer size.
	 */
	private ValidationControlDecoration<Text> bufferSizeValueDecoration;

	/**
	 * {@link Text} for buffer strategy value.
	 */
	private Text bufferSizeValue;

	/**
	 * Combo for choosing retransformation strategy.
	 */
	private Combo retransformationCombo;

	/**
	 * Button for class loading delegation.
	 */
	private Button classDelegationButton;

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
	public EnvironmentSettingsPart(FormPage formPage, Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		EnvironmentEditorInput input = (EnvironmentEditorInput) formPage.getEditor().getEditorInput();
		this.environment = input.getEnvironment();
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);

		// client
		createPart(getSection(), toolkit);

		// text and description on our own
		getSection().setText("Strategies and Options");
		Label label = toolkit.createLabel(getSection(), "Define different options and strategies of the environment.");
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
	private void createPart(Section section, FormToolkit toolkit) {
		Composite mainComposite = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 10;
		mainComposite.setLayout(gridLayout);
		section.setClient(mainComposite);

		// data buffer size
		toolkit.createLabel(mainComposite, "Data buffer size:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		bufferSizeValue = toolkit.createText(mainComposite, "", SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.widthHint = 50;
		bufferSizeValue.setLayoutData(gd);
		createInfoLabel(mainComposite, toolkit,
				"The data buffer size defines number of monitoring points that inspectIT can maximally cache on the agent side. This number must be a positive and a number that is power of two.");

		// retransformation
		toolkit.createLabel(mainComposite, "Retransformation strategy:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		retransformationCombo = new Combo(mainComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		retransformationCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		toolkit.adapt(retransformationCombo, false, false);
		createInfoLabel(mainComposite, toolkit,
				"The retransformation strategy states whether agents should use class retransformation to instrument already loaded classes during startup phase.\nIf retransformation is disabled, class redefinition is used and classes cannot be dynamically reinstrumented without application restart.");

		// class delegation
		toolkit.createLabel(mainComposite, "Class loading delegation:").setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		classDelegationButton = toolkit.createButton(mainComposite, "Active", SWT.CHECK);
		classDelegationButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		classDelegationButton.setSelection(environment.isClassLoadingDelegation());
		createInfoLabel(mainComposite, toolkit,
				"If activated all sub-classes of java.lang.ClassLoader will be instrumented so that loading of the inspectIT classes is delegated to the inspectIT class loader. Should only be changed to false in rare cases and is expert user level option.");

		// fill the boxes and values
		bufferSizeValue.setText(String.valueOf(environment.getDataBufferSize()));
		for (RetransformationStrategy strategy : RetransformationStrategy.values()) {
			retransformationCombo.add(strategy.toString());
			retransformationCombo.setData(strategy.toString(), strategy);
		}
		retransformationCombo.select(environment.getRetransformationStrategy().ordinal());

		// validation boxes
		bufferSizeValueDecoration = new ValidationControlDecoration<Text>(bufferSizeValue, formPage.getManagedForm().getMessageManager()) {
			@Override
			protected boolean validate(Text control) {
				return validateUpdateBufferSize(false);
			}
		};
		bufferSizeValueDecoration.registerListener(SWT.Modify);

		// dirty listener
		Listener dirtyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (!isDirty()) {
					markDirty();
				}
			}
		};
		retransformationCombo.addListener(SWT.Selection, dirtyListener);
		bufferSizeValue.addListener(SWT.Modify, dirtyListener);
		classDelegationButton.addListener(SWT.Selection, dirtyListener);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
		if (onSave) {
			super.commit(onSave);

			validateUpdateBufferSize(true);
			environment.setRetransformationStrategy((RetransformationStrategy) retransformationCombo.getData(retransformationCombo.getItem(retransformationCombo.getSelectionIndex())));
			environment.setClassLoadingDelegation(classDelegationButton.getSelection());
			getManagedForm().dirtyStateChanged();
		}
	}

	/**
	 * Validates the data bufer size.
	 *
	 * @param update
	 *            If beside validation an update on the model object should be done.
	 * @return if control has valid value
	 */
	private boolean validateUpdateBufferSize(boolean update) {
		boolean valid = true;
		try {
			int size = Integer.parseInt(bufferSizeValue.getText());
			if ((size <= 0) || ((size & (size - 1)) != 0)) {
				// negative or not power of two
				showBufferSizeValidationMessage();
				valid = false;
			} else {
				if (update) {
					environment.setDataBufferSize(size);
				}
			}
		} catch (NumberFormatException exception) {
			showBufferSizeValidationMessage();
			valid = false;
		}

		return valid;
	}

	/**
	 * Shows validation error message for data buffer size.
	 */
	private void showBufferSizeValidationMessage() {
		bufferSizeValueDecoration.setDescriptionText("Data size buffer must define a number that is greater than zero and power of 2.");
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
	protected void createInfoLabel(Composite parent, FormToolkit toolkit, String text) {
		Label label = toolkit.createLabel(parent, "");
		label.setToolTipText(text);
		label.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
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

	@Override
	public void dispose() {
		formPage.getEditor().removePropertyListener(this);
		super.dispose();
	}

}
