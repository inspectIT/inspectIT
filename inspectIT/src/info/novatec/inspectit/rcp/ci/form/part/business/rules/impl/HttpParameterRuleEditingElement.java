package info.novatec.inspectit.rcp.ci.form.part.business.rules.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.valuesource.impl.HttpParameterValueSource;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractStringMatchingRuleEditingElement;
import info.novatec.inspectit.rcp.validation.IValidatorRegistry;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Editing element for a HTTP parameter matching expression.
 *
 * @author Alexander Wert
 *
 */
public class HttpParameterRuleEditingElement extends AbstractStringMatchingRuleEditingElement<HttpParameterValueSource> {

	/**
	 * Description text for the parameter row.
	 */
	private static final String PARAMETER_DESCRIPTION = "Specify the HTTP parameter name. \n\nNo wildcards are allowed here!";

	/**
	 * Description for the HTTP parameter matching rule.
	 */
	private static final String DESCRIPTION = "This rule applies if the value of the specified HTTP parameter of the corresponding request\n"
			+ "matches (equals, starts with, etc.) the specified String value.";

	/**
	 * Text editing field for the {@link #parameterName} property.
	 */
	private Text parameterNameText;

	/**
	 * {@link ValidationControlDecoration} for {@link #parameterNameText}.
	 */
	private ValidationControlDecoration<Text> parameterNameTextValidation;

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 */
	public HttpParameterRuleEditingElement(StringMatchingExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		super(expression, MatchingRuleType.HTTP_PARAMETER, DESCRIPTION, "Value", true, editable, validatorRegistry);
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(final Composite parent, FormToolkit toolkit) {
		Label fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(fillLabel);

		Label parameterLabel = toolkit.createLabel(parent, "Parameter name:");
		parameterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(parameterLabel);

		parameterNameText = new Text(parent, SWT.BORDER);
		parameterNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

		parameterNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getStringValueSource().setParameterName(parameterNameText.getText());
				notifyModifyListeners();
			}
		});
		addControl(parameterNameText);

		Label infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(PARAMETER_DESCRIPTION);
		addControl(infoLabel);

		super.createSpecificElements(parent, toolkit);
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected void executeSpecificInitialization(StringMatchingExpression expression) {
		if (isValidExpression(expression)) {
			super.executeSpecificInitialization(expression);
			parameterNameText.setText(getStringValueSource().getParameterName());
		}
	}

	/**
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isValidExpression(StringMatchingExpression expression) {
		return expression.getStringValueSource() instanceof HttpParameterValueSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControlValidators() {
		super.createControlValidators();
		parameterNameTextValidation = new ValidationControlDecoration<Text>(parameterNameText, getValidatorRegistry()) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText());
			}
		};
		parameterNameTextValidation.setDescriptionText("Parameter name must not be empty!");
		parameterNameTextValidation.registerListener(SWT.Modify);
		addValidator(parameterNameTextValidation);
	}
}
