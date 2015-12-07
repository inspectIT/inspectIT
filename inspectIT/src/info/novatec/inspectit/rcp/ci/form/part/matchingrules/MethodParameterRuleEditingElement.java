/**
 *
 */
package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.MethodParameterValueSource;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.impl.StringValueSource;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;

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
	 * Label for the method signature.
	 */
	private Label methodSignatureLabel;

	/**
	 * The parameter index.
	 */
	private int parameterIndex = 0;

	/**
	 * Spinner control for editing the parameter index.
	 */
	private Spinner parameterIndexSpinner;

	/**
	 * Label for the parameter index.
	 */
	private Label parameterIndexLabel;

	/**
	 * Dummy label to fill the grid layout.
	 */
	private Label fillLabel;

	/**
	 * Label holding the info text.
	 */
	private Label infoLabel;

	/**
	 * Constructor.
	 *
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 */
	public MethodParameterRuleEditingElement(boolean editable) {
		super(MatchingRuleType.METHOD_PARAMETER.toString(), DESCRIPTION, "the value", true, editable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createSpecificElements(Composite parent, FormToolkit toolkit) {
		fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		methodSignatureLabel = toolkit.createLabel(parent, "In the method with signature");
		methodSignatureLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		methodSignatureText = new Text(parent, SWT.BORDER);
		methodSignatureText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		methodSignatureText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				methodSignature = methodSignatureText.getText();
				notifyModifyListeners();
			}
		});

		parameterIndexLabel = toolkit.createLabel(parent, "for parameter with index");
		parameterIndexLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

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

		infoLabel = toolkit.createLabel(parent, "");
		infoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		infoLabel.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_INFORMATION));
		infoLabel.setToolTipText(PARAMETER_DESCRIPTION);

		super.createSpecificElements(parent, toolkit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void disposeSpecificElements() {
		methodSignatureText.dispose();
		methodSignatureLabel.dispose();
		parameterIndexSpinner.dispose();
		parameterIndexLabel.dispose();
		fillLabel.dispose();
		infoLabel.dispose();
		super.disposeSpecificElements();
	}

	@Override
	protected void executeSpecificInitialization(AbstractExpression expression) {
		if (isValidExpression(expression)) {
			super.executeSpecificInitialization(expression);
			StringMatchingExpression strMatchingExpression = ((StringMatchingExpression) expression);
			MethodParameterValueSource methodParameterValueSource = (MethodParameterValueSource) strMatchingExpression.getStringValueSource();
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
	protected int getNumRows() {
		return super.getNumRows() + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setEnabledStateForSpecificElements() {
		methodSignatureText.setEnabled(isEditable());
		methodSignatureLabel.setEnabled(isEditable());
		parameterIndexSpinner.setEnabled(isEditable());
		parameterIndexLabel.setEnabled(isEditable());
		fillLabel.setEnabled(isEditable());
		infoLabel.setEnabled(isEditable());
		super.setEnabledStateForSpecificElements();
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
	protected boolean isValidExpression(AbstractExpression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodParameterValueSource;
	}

}
