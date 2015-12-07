package rocks.inspectit.ui.rcp.ci.form.part.business.rules.impl;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.impl.MethodParameterValueSource;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractStringMatchingRuleEditingElement;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.InputValidatorControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;
import rocks.inspectit.ui.rcp.validation.validator.FqnMethodSignatureValidator;

/**
 * @author Alexander Wert
 *
 */
public class MethodParameterRuleEditingElement extends AbstractStringMatchingRuleEditingElement<MethodParameterValueSource> {
	/**
	 * The name of the string source.
	 */
	private static final String SOURCE_NAME = "Value";

	/**
	 * Description text for the parameter row.
	 */
	private static final String PARAMETER_DESCRIPTION = "Specify the fully qualified method signature and the parameter index. \n\nNo wildcards are allowed for the method signature!";

	/**
	 * Description text for the method parameter matching rule.
	 */
	private static final String DESCRIPTION = "This rule applies if the value of the specified method parameter of the corresponding request\n"
			+ "matches (equals, starts with, etc.) the specified String value.";

	/**
	 * Identifier of the method parameter name validator.
	 */
	private static final String METHOD_PARAMETER_NAME_VALIDATOR_ID = "METHOD_PARAMETER_NAME_VALIDATOR_ID";

	/**
	 * Text control for editing the method signature.
	 */
	private Text methodSignatureText;

	/**
	 * Spinner control for editing the parameter index.
	 */
	private Spinner parameterIndexSpinner;

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
	public MethodParameterRuleEditingElement(StringMatchingExpression expression, boolean editable, AbstractValidationManager<AbstractExpression> upstreamValidationManager) {
		super(expression, MatchingRuleType.METHOD_PARAMETER, DESCRIPTION, "Value", true, editable, upstreamValidationManager);
	}

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
		MethodParameterValueSource methodParameterValueSource = (MethodParameterValueSource) expression.getStringValueSource();
		FqnMethodSignatureValidator methodSignatureValidator = new FqnMethodSignatureValidator(false);
		String errorMessage = methodSignatureValidator.isValid(methodParameterValueSource.getMethodSignature());
		if (null != errorMessage) {
			resultSet.add(new ValidationState(METHOD_PARAMETER_NAME_VALIDATOR_ID, false, errorMessage));
		}
		resultSet.addAll(AbstractStringMatchingRuleEditingElement.validate(expression, SOURCE_NAME));
		return resultSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(Composite parent, FormToolkit toolkit) {
		Label fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(fillLabel);

		Label methodSignatureLabel = toolkit.createLabel(parent, "Method signature:");
		methodSignatureLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(methodSignatureLabel);

		methodSignatureText = new Text(parent, SWT.BORDER);
		methodSignatureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		methodSignatureText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				getStringValueSource().setMethodSignature(methodSignatureText.getText());
				notifyModifyListeners();
			}
		});
		addControl(methodSignatureText);

		Label parameterIndexLabel = toolkit.createLabel(parent, "Parameter index:");
		parameterIndexLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		addControl(parameterIndexLabel);

		parameterIndexSpinner = new Spinner(parent, SWT.BORDER);
		parameterIndexSpinner.setMinimum(0);
		parameterIndexSpinner.setMaximum(50);
		parameterIndexSpinner.setSelection(0);
		parameterIndexSpinner.setIncrement(1);
		parameterIndexSpinner.setPageIncrement(5);
		parameterIndexSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		parameterIndexSpinner.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getStringValueSource().setParameterIndex(parameterIndexSpinner.getSelection());
				notifyModifyListeners();
			}
		});
		addControl(parameterIndexSpinner);

		Label infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(PARAMETER_DESCRIPTION);
		addControl(infoLabel);

		super.createSpecificElements(parent, toolkit);
	}

	@Override
	protected void executeSpecificInitialization(StringMatchingExpression expression) {
		if (isValidExpression(expression)) {
			super.executeSpecificInitialization(expression);
			methodSignatureText.setText(getStringValueSource().getMethodSignature());
			parameterIndexSpinner.setSelection(getStringValueSource().getParameterIndex());
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isValidExpression(StringMatchingExpression expression) {
		return expression.getStringValueSource() instanceof MethodParameterValueSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createControlValidators() {
		super.createControlValidators();
		InputValidatorControlDecoration methodSignatureTextValidation = new InputValidatorControlDecoration(methodSignatureText, getValidationManager(), new FqnMethodSignatureValidator(false));
		methodSignatureTextValidation.registerListener(SWT.Modify);
		getValidationManager().addValidator(methodSignatureTextValidation, METHOD_PARAMETER_NAME_VALIDATOR_ID);
	}
}
