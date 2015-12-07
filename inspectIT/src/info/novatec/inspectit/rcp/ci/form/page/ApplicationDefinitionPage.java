package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.IContainerExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import info.novatec.inspectit.rcp.ci.form.part.business.SelectiveRulesPart;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

/**
 * Edit page for {@link ApplicationDefinition}.
 *
 * @author Alexander Wert
 *
 */
public class ApplicationDefinitionPage extends FormPage implements IValidatorRegistry, IPropertyListener {
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
	 * {@link ValidationControlDecoration} instances.
	 */
	private final Map<ValidatorKey, ValidationControlDecoration<?>> validationControlDecorators = new HashMap<>();

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
		applicationMatchingRulesPart = new SelectiveRulesPart("Application Mapping", body, managedForm, this);
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
				clearValidators();
				mainForm.reflow(true);
				applicationMatchingRulesPart.initContent(application);
				applicationMatchingRulesPart.setDescriptionText(DESCRIPTION);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<ValidatorKey, ValidationControlDecoration<?>> getValidationControlDecorators() {
		return validationControlDecorators;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerValidator(ValidatorKey key, ValidationControlDecoration<?> validator) {
		getValidationControlDecorators().put(key, validator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performInitialValidation() {
		for (Entry<ValidatorKey, ValidationControlDecoration<?>> entry : validationControlDecorators.entrySet()) {
			updateValidationMessage(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validationStateChanged(boolean valid, ValidationControlDecoration<?> validationControlDecoration) {
		updateValidationMessage(getValidatorKey(validationControlDecoration), validationControlDecoration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterValidators(Set<ValidatorKey> keys) {
		for (ValidatorKey key : keys) {
			if (key.getControlIndex() < 0 && null != key.getAbstractExpression()) {
				removeValidatorsForExpressions(key.getAbstractExpression());
			} else {
				validationControlDecorators.remove(key);
				mainForm.getMessageManager().removeMessage(key.getGroupId());
			}
		}
	}

	/**
	 * Clears all {@link ValidationControlDecoration} instances.
	 */
	private void clearValidators() {
		for (Entry<ValidatorKey, ValidationControlDecoration<?>> entry : validationControlDecorators.entrySet()) {
			mainForm.getMessageManager().removeMessage(entry.getKey().getGroupId());
		}
		validationControlDecorators.clear();
	}

	/**
	 * Updates the validation message for this page based on the change in the passed
	 * {@link ValidationControlDecoration} instance.
	 *
	 * @param key
	 *            {@link ValidatorKey} of the changed {@link ValidationControlDecoration} instance
	 * @param validator
	 *            changed {@link ValidationControlDecoration} instance
	 */
	private void updateValidationMessage(ValidatorKey key, ValidationControlDecoration<?> validator) {
		if (!validator.isValid()) {
			String message = getShortMessage(key.getGroupId());
			if (null != key.getAbstractExpression()) {
				message = key.getGroupName() + " (" + message + ")";
				mainForm.getMessageManager().addMessage(key.getGroupId(), message, null, IMessageProvider.ERROR);
			}
		} else {
			mainForm.getMessageManager().removeMessage(key.getGroupId());
		}
	}

	/**
	 * Creates an aggregated validation error message for the given group ID.
	 *
	 * @param groupId
	 *            the identifier of the group for which the message shell be created
	 * @return an aggregated validation error message
	 */
	private String getShortMessage(int groupId) {
		int errorCount = 0;
		for (Entry<ValidatorKey, ValidationControlDecoration<?>> entry : validationControlDecorators.entrySet()) {
			ValidatorKey currentKey = entry.getKey();
			if (currentKey.getGroupId() == groupId && !entry.getValue().isValid()) {
				errorCount++;
			}
		}

		String errorsText = "";
		if (errorCount == 1) {
			errorsText = "One field contains a validation error";
		} else if (errorCount > 1) {
			errorsText = errorCount + " fields contain validation errors";
		}

		return errorsText;
	}

	/**
	 * Retrieves the {@link ValidatorKey} for the given {@link ValidationControlDecoration}
	 * instance.
	 *
	 * @param validationControlDecoration
	 *            {@link ValidationControlDecoration} instance
	 * @return Returns the {@link ValidatorKey} for the given {@link ValidationControlDecoration}
	 *         instance.
	 */
	private ValidatorKey getValidatorKey(ValidationControlDecoration<?> validationControlDecoration) {
		for (Entry<ValidatorKey, ValidationControlDecoration<?>> entry : validationControlDecorators.entrySet()) {
			if (entry.getValue() == validationControlDecoration) {
				return entry.getKey();
			}
		}
		throw new RuntimeException("Unknown ValidationControlDecoration instance!");
	}

	/**
	 * Removes {@link ValidationControlDecoration} instances that are associated with the given
	 * {@link AbstractExpression}, either as direct belonging or as a descendant of an
	 * {@link IContainerExpression}.
	 *
	 * @param expression
	 *            {@link AbstractExpression} identifying the {@link ValidationControlDecoration}
	 *            instances to be removed
	 */
	private void removeValidatorsForExpressions(AbstractExpression expression) {
		if (expression instanceof IContainerExpression) {
			for (AbstractExpression childExpression : ((IContainerExpression) expression).getOperands()) {
				removeValidatorsForExpressions(childExpression);
			}
		} else {
			List<ValidatorKey> keysToRemove = new ArrayList<>();
			for (Entry<ValidatorKey, ValidationControlDecoration<?>> validatorEntry : validationControlDecorators.entrySet()) {
				if (expression.equals(validatorEntry.getKey().getAbstractExpression())) {
					keysToRemove.add(validatorEntry.getKey());
				}
			}

			for (ValidatorKey key : keysToRemove) {
				validationControlDecorators.remove(key);
				mainForm.getMessageManager().removeMessage(key.getGroupId());
			}
		}
	}
}
