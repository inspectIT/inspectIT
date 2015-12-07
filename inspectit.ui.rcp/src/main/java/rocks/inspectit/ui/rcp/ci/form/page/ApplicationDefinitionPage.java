package rocks.inspectit.ui.rcp.ci.form.page;

import java.util.Set;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory;
import rocks.inspectit.ui.rcp.ci.form.part.business.SelectiveRulesPart;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.ValidationState;

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
	 * Description text.
	 */
	private static final String DESCRIPTION = "Define the matching rule that should be used to match this application:";

	/**
	 * Main form of this editor page.
	 */
	private ScrolledForm mainForm;

	/**
	 * {@link ApplicationDefinition} instance to be edited.
	 */
	private ApplicationDefinition application;

	/**
	 * Section part for the definition of application matching rules.
	 */
	private SelectiveRulesPart applicationMatchingRulesPart;

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
		mainForm.setText(TITLE);

		mainForm.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_APPLICATION));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(mainForm.getForm());

		// body
		Composite body = mainForm.getBody();
		body.setLayout(new GridLayout(1, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));

		// matching rules part
		applicationMatchingRulesPart = new SelectiveRulesPart("Application Mapping", body, managedForm, new ApplicationValidationManager());
		applicationMatchingRulesPart.initContent(application);
		applicationMatchingRulesPart.setDescriptionText(DESCRIPTION);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Manually set focus to form body, otherwise is the tool-bar in focus.
	 */
	@Override
	public void setFocus() {
		mainForm.getBody().setFocus();
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

			if (null != mainForm) {
				mainForm.reflow(true);
				applicationMatchingRulesPart.initContent(application);
				applicationMatchingRulesPart.setDescriptionText(DESCRIPTION);
			}
		}
	}

	/**
	 * Validation Manager for the ApplicationDefinitionPage. Responsible for showing aggregated
	 * validation messages in the page.
	 *
	 * @author Alexander Wert
	 *
	 */
	private class ApplicationValidationManager extends AbstractValidationManager<AbstractExpression> {
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void notifyUpstream(AbstractExpression key, Set<ValidationState> states) {
			// no upstream, so nothing to do here
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void showMessage(AbstractExpression expression, Set<ValidationState> states) {
			int errorCount = 0;
			String concatenatedMessage = "";
			for (ValidationState state : states) {
				if (!state.isValid()) {
					errorCount++;
					concatenatedMessage += state.getMessage();
				}
			}
			String errorsText = "";
			if (errorCount == 1) {
				errorsText = "One field contains a validation error";
			} else if (errorCount > 1) {
				errorsText = errorCount + " fields contain validation errors";
			}

			String message = ((expression instanceof StringMatchingExpression) ? MatchingRulesEditingElementFactory.getMatchingRuleType(expression).toString() + " (" + errorsText + ")"
					: concatenatedMessage);
			mainForm.getMessageManager().addMessage(expression, message, null, IMessageProvider.ERROR);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void hideMessage(AbstractExpression key) {
			mainForm.getMessageManager().removeMessage(key);
		}

	}
}
