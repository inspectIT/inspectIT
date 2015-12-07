package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.ci.business.Expression;
import info.novatec.inspectit.ci.business.MethodSignatureValueSource;
import info.novatec.inspectit.ci.business.StringMatchingExpression;
import info.novatec.inspectit.ci.business.StringValueSource;

/**
 * Editing element for a method signature matching expression.
 * 
 * @author Alexander Wert
 *
 */
public class MethodSignatureRuleEditingElement extends AbstractStringMatchingRuleEditingElement {

	/**
	 * Default constructor.
	 */
	public MethodSignatureRuleEditingElement() {
		super("Method Signature Matching", "Method signature ", true);
	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new MethodSignatureValueSource();
	}

	@Override
	protected boolean isValidExpression(Expression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof MethodSignatureValueSource;
	}

}
