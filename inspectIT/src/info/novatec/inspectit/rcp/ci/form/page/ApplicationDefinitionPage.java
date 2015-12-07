package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import info.novatec.inspectit.rcp.ci.form.part.SelectiveRulesPart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Edit page for {@link ApplicationDefinition}.
 *
 * @author Alexander Wert
 *
 */
public class ApplicationDefinitionPage extends FormPage implements IPropertyListener {
	/**
	 * Id of the page.
	 */
	private static final String ID = ApplicationDefinitionPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Application Definition";

	/**
	 * {@link ApplicationDefinition} instance to be edited.
	 */
	private ApplicationDefinition application;

	/**
	 * Section part for the definition of application matching rules.
	 */
	private SelectiveRulesPart applicationMatchingRulesPart;

	/**
	 * Main form of this editor page.
	 */
	private ScrolledForm mainForm;

	/**
	 * Composite containing the description of the application.
	 */
	private Composite descriptionComposite;

	/**
	 * Label for the description text.
	 */
	private Label descriptionLabel;

	/**
	 * Default constructor.
	 *
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public ApplicationDefinitionPage(FormEditor editor) {
		super(editor, ID, TITLE);
		editor.addPropertyListener(this);

		ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) getEditor().getEditorInput();
		this.application = input.getApplication();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		mainForm = managedForm.getForm();
		mainForm.setText(TITLE + ": " + application.getApplicationName());

		mainForm.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_APPLICATION));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(mainForm.getForm());

		// body
		Composite body = mainForm.getBody();
		body.setLayout(new GridLayout(1, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));

		// description part
		descriptionComposite = toolkit.createComposite(body);
		descriptionComposite.setLayout(new GridLayout(2, false));
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		Label infoLabel = toolkit.createLabel(descriptionComposite, "");
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		descriptionLabel = toolkit.createLabel(descriptionComposite, application.getDescription());
		descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (null == application.getDescription() || application.getDescription().isEmpty()) {
			descriptionComposite.setVisible(false);
		}

		// matching rules part
		applicationMatchingRulesPart = new SelectiveRulesPart("Application Mapping", body, managedForm);
		applicationMatchingRulesPart.setEditable(application.getId() != ApplicationDefinition.DEFAULT_ID);
		applicationMatchingRulesPart.setDescriptionText("Define the matching rule that should be used to match this application:");
		applicationMatchingRulesPart.initContent(application.getMatchingRuleExpression());

		applicationMatchingRulesPart.addPropertyListeners(this);
	}

	/**
	 * Applies changes.
	 */
	public void commit() {
		AbstractExpression applicationMatchingRule = applicationMatchingRulesPart.constructMatchingRuleExpression();
		application.setMatchingRuleExpression(applicationMatchingRule);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Manually set focus to form body, otherwise is the tool-bar in focus.
	 */
	@Override
	public void setFocus() {
		getManagedForm().getForm().getBody().setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT) {
			ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) getEditor().getEditorInput();
			setInput(input);
			application = input.getApplication();
			mainForm.setText(TITLE + ": " + application.getApplicationName());

			if (null != application.getDescription() && !application.getDescription().isEmpty()) {
				descriptionLabel.setText(application.getDescription());
				descriptionComposite.setVisible(true);
			} else {
				descriptionComposite.setVisible(false);
			}

			mainForm.reflow(true);
			applicationMatchingRulesPart.initContent(application.getMatchingRuleExpression());
		} else if (propId == SelectiveRulesPart.ADVANCED_VIEW_TOGGLE_PROP_ID) {
			commit();
			applicationMatchingRulesPart.initContent(application.getMatchingRuleExpression());
		}
	}
}
