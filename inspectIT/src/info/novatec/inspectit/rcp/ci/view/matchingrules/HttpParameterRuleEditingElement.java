package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.cmr.configuration.business.expression.Expression;
import info.novatec.inspectit.cmr.configuration.business.expression.StringValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.HttpParameterValueSource;
import info.novatec.inspectit.cmr.configuration.business.expression.impl.StringMatchingExpression;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Editing element for a HTTP parameter matching expression.
 * 
 * @author Alexander Wert
 *
 */
public class HttpParameterRuleEditingElement extends AbstractStringMatchingRuleEditingElement {

	/**
	 * Property for the name of the HTTP parameter.
	 */
	private String parameterName = "";

	/**
	 * Text editing field for the {@link #parameterName} property.
	 */
	private Text parameterNameText;

	/**
	 * Default constructor.
	 */
	public HttpParameterRuleEditingElement() {
		super("HTTP Parameter Matching", "the value ", true);
	}

	@Override
	protected void createSpecificElements(final Composite parent) {
		Composite elementsContainer = new Composite(parent, SWT.NONE);
		elementsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		elementsContainer.setLayout(new GridLayout(3, false));
		Label label = new Label(elementsContainer, SWT.NONE);
		label.setText("For the HTTP parameter with the name ");

		parameterNameText = new Text(elementsContainer, SWT.BORDER);
		parameterNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		parameterNameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				parameterName = ((Text) e.getSource()).getText();
				notifyModifyListeners();
			}
		});

		super.createSpecificElements(parent);
	}

	@Override
	protected void executeSpecificInitialization(Expression expression) {
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
	protected boolean isValidExpression(Expression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpParameterValueSource;
	}

}
