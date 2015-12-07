package info.novatec.inspectit.rcp.ci.form.part.business.rules.impl;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.ci.business.valuesource.impl.MethodParameterValueSource;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.page.IValidatorRegistry;
import info.novatec.inspectit.rcp.ci.form.part.business.MatchingRulesEditingElementFactory.MatchingRuleType;
import info.novatec.inspectit.rcp.ci.form.part.business.rules.AbstractStringMatchingRuleEditingElement;
import info.novatec.inspectit.rcp.validation.InputValidatorControlDecoration;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;
import info.novatec.inspectit.rcp.validation.validator.FqnMethodSignatureValidator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Alexander Wert
 *
 */
public class MethodParameterRuleEditingElement extends AbstractStringMatchingRuleEditingElement {

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
	 * Method signature.
	 */
	private String methodSignature = "";

	/**
	 * Text control for editing the method signature.
	 */
	private Text methodSignatureText;

	/**
	 * The parameter index.
	 */
	private int parameterIndex = 0;

	/**
	 * Spinner control for editing the parameter index.
	 */
	private Spinner parameterIndexSpinner;

	/**
	 * {@link ValidationControlDecoration} for {@link #methodSignatureText}.
	 */
	private ValidationControlDecoration<Text> methodSignatureTextValidation;

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
	public MethodParameterRuleEditingElement(StringMatchingExpression expression, boolean editable, IValidatorRegistry validatorRegistry) {
		super(expression, MatchingRuleType.METHOD_PARAMETER, DESCRIPTION, "Value", true, editable, validatorRegistry);
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
				methodSignature = methodSignatureText.getText();
				if (null != methodSignatureTextValidation) {
					methodSignatureTextValidation.executeValidation();
				}
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
				parameterIndex = parameterIndexSpinner.getSelection();
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
			MethodParameterValueSource methodParameterValueSource = (MethodParameterValueSource) expression.getStringValueSource();
			methodSignature = methodParameterValueSource.getMethodSignature();
			methodSignatureText.setText(methodSignature);
			parameterIndex = methodParameterValueSource.getParameterIndex();
			parameterIndexSpinner.setSelection(parameterIndex);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected StringValueSource getStringValueSource() {
		return new MethodParameterValueSource(parameterIndex, methodSignature);
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
		methodSignatureTextValidation = new InputValidatorControlDecoration(methodSignatureText, getValidatorRegistry(), new FqnMethodSignatureValidator(false));
		addValidator(methodSignatureTextValidation);
	}
}
