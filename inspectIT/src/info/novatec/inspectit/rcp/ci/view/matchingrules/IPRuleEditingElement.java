package info.novatec.inspectit.rcp.ci.view.matchingrules;

import info.novatec.inspectit.ci.business.Expression;
import info.novatec.inspectit.ci.business.HostValueSource;
import info.novatec.inspectit.ci.business.StringMatchingExpression;
import info.novatec.inspectit.ci.business.StringValueSource;

/**
 * Editing element for a IP matching expression.
 * 
 * @author Alexander Wert
 *
 */
public class IPRuleEditingElement extends AbstractStringMatchingRuleEditingElement {

	/**
	 * Default constructor.
	 */
	public IPRuleEditingElement() {
		super("IP Matching", "IP ", false);
	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new HostValueSource();
	}

	@Override
	protected boolean isValidExpression(Expression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HostValueSource;
	}

}
