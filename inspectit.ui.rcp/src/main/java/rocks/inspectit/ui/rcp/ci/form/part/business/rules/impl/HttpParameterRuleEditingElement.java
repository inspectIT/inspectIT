package rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.HttpParameterValueSource;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractStringMatchingRuleEditingElement;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * Editing element for a HTTP parameter matching expression.
 *
 * @author Alexander Wert
 *
 */
public class HttpParameterRuleEditingElement extends AbstractStringMatchingRuleEditingElement<HttpParameterValueSource> {

	/**
	 * The name of the string source.
	 */
	private static final String SOURCE_NAME = "Value";

	/**
	 * Error message for the parameter name validator.
	 */
	private static final String PARAMETER_NAME_VALIDATION_ERROR_MESSAGE = "Parameter name must not be empty!";

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
	 * Identifier of the parameter name validator.
	 */
	private static final String PARAMETER_NAME_VALIDATOR_ID = "PARAMETER_NAME_VALIDATOR_ID";

	/**
	 * Validates the contents of the passed {@link StringMatchingExpression} without the need to
	 * create corresponding editing controls.
	 *
	 * @param expression
	 *            {@link StringMatchingExpression} to validate
	 * @return a set of {@link ValidationState} instances.
	 */
	public static Set<ValidationState> validate(StringMatchingExpression expression) {
		Set<ValidationState> resultSet = new HashSet<>();
		HttpParameterValueSource httpParameterValueSource = (HttpParameterValueSource) expression.getStringValueSource();
		if (StringUtils.isBlank(httpParameterValueSource.getParameterName())) {
			resultSet.add(new ValidationState(PARAMETER_NAME_VALIDATOR_ID, false, PARAMETER_NAME_VALIDATION_ERROR_MESSAGE));
		}
		resultSet.addAll(AbstractStringMatchingRuleEditingElement.validate(expression, SOURCE_NAME));
		return resultSet;
	}

	/**
	 * Text editing field for the {@link #parameterName} property.
	 */
	private Text parameterNameText;

	/**
	 * Constructor.
	 *
	 * @param expression
	 *            The {@link AbstractExpression} instance to modify.
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 * @param upstreamValidationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	public HttpParameterRuleEditingElement(StringMatchingExpression expression, boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		super(expression, MatchingRuleType.HTTP_PARAMETER, DESCRIPTION, SOURCE_NAME, true, editable, upstreamValidationManager);
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
		ValidationControlDecoration<Text> parameterNameTextValidation = new ValidationControlDecoration<Text>(parameterNameText, getValidationManager()) {
			@Override
			protected boolean validate(Text control) {
				return StringUtils.isNotBlank(control.getText());
			}
		};
		parameterNameTextValidation.setDescriptionText(PARAMETER_NAME_VALIDATION_ERROR_MESSAGE);
		parameterNameTextValidation.registerListener(SWT.Modify);
		getValidationManager().addValidator(parameterNameTextValidation, PARAMETER_NAME_VALIDATOR_ID);
	}
}
