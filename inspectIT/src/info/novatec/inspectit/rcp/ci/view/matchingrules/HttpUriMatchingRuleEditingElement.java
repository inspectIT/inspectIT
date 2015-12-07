package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.ci.business.Expression;
import info.novatec.inspectit.ci.business.HttpUriValueSource;
import info.novatec.inspectit.ci.business.StringMatchingExpression;
import info.novatec.inspectit.ci.business.StringValueSource;

/**
 * Editing element for a HTTP URI matching expression.
 * 
 * @author Alexander Wert
 *
 */
public class HttpUriMatchingRuleEditingElement extends AbstractStringMatchingRuleEditingElement {

	/**
	 * Default constructor.
	 */
	public HttpUriMatchingRuleEditingElement() {
		super("HTTP URI Matching", "HTTP URI ", true);
	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new HttpUriValueSource();
	}

	@Override
	protected boolean isValidExpression(Expression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HttpUriValueSource;
	}
}
