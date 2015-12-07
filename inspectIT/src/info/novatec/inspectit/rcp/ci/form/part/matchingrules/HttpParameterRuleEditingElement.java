package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.HttpParameterValueSource;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Editing element for a HTTP parameter matching expression.
 *
 * @author Alexander Wert
 *
 */
public class HttpParameterRuleEditingElement extends AbstractStringMatchingRuleEditingElement {

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
	 * Property for the name of the HTTP parameter.
	 */
	private String parameterName = "";

	/**
	 * Text editing field for the {@link #parameterName} property.
	 */
	private Text parameterNameText;

	/**
	 * Label for parameter name.
	 */
	private Label parameterLabel;

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
	public HttpParameterRuleEditingElement(boolean editable) {
		super(MatchingRuleType.HTTP_PARAMETER.toString(), DESCRIPTION, "the value", true, editable);
	}

	@Override
	protected void createSpecificElements(final Composite parent, FormToolkit toolkit) {
		fillLabel = toolkit.createLabel(parent, "");
		fillLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

		parameterLabel = toolkit.createLabel(parent, "For the HTTP parameter");
		parameterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		parameterNameText = new Text(parent, SWT.BORDER);
		parameterNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		parameterNameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				parameterName = ((Text) e.getSource()).getText();
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
		parameterLabel.dispose();
		parameterNameText.dispose();
		fillLabel.dispose();
		infoLabel.dispose();
		super.disposeSpecificElements();
	}

	@Override
	protected void executeSpecificInitialization(AbstractExpression expression) {
		if (isValidExpression(expression)) {
			super.executeSpecificInitialization(expression);
			StringMatchingExpression strMatchingExpression = ((StringMatchingExpression) expression);
			parameterName = ((HttpParameterValueSource) strMatchingExpression.getStringValueSource()).getParameterName();
			parameterNameText.setText(parameterName);
		}

	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new HttpParameterValueSource(parameterName);
	}

	@Override
	protected boolean isValidExpression(AbstractExpression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpParameterValueSource;
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
		parameterLabel.setEnabled(isEditable());
		parameterNameText.setEnabled(isEditable());
		fillLabel.setEnabled(isEditable());
		infoLabel.setEnabled(isEditable());
		super.setEnabledStateForSpecificElements();
	}

}
