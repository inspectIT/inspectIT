package info.novatec.inspectit.rcp.ci.form.part.matchingrules;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.HostValueSource;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.impl.StringValueSource;
import info.novatec.inspectit.rcp.ci.form.part.matchingrules.MatchingRulesEditingElementFactory.MatchingRuleType;

/**
 * Editing element for a IP matching expression.
 *
 * @author Alexander Wert
 *
 */
public class IPRuleEditingElement extends AbstractStringMatchingRuleEditingElement {
	/**
	 * Description text for the IP/Host matching rule.
	 */
	private static final String DESCRIPTION = "This rule applies if the host name or IP of the server serving the corresponding request\n"
			+ "matches (equals, starts with, etc.) the specified String value.";

	/**
	 * Constructor.
	 *
	 * @param editable
	 *            indicates whether this editing element should be editable or read-only. If false,
	 *            this element will be read only.
	 */
	public IPRuleEditingElement(boolean editable) {
		super(MatchingRuleType.IP.toString(), DESCRIPTION, "IP ", false, editable);
	}

	@Override
	protected StringValueSource getStringValueSource() {
		return new HostValueSource();
	}

	@Override
	protected boolean isValidExpression(AbstractExpression expression) {
		return expression instanceof StringMatchingExpression && ((StringMatchingExpression) expression).getStringValueSource() instanceof HostValueSource;
	}

}
