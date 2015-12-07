package info.novatec.inspectit.rcp.ci.view.matchingrules;

/**
 * Interface for listeners that are modified when an instance of a
 * {@link AbstractRuleEditingElement} is modified or disposed.
 * 
 * @author Alexander Wert
 *
 */
public interface RuleEditingElementModifiedListener {
	/**
	 * Contents have been modified.
	 */
	void contentModified();

	/**
	 * Element has been disposed.
	 * 
	 * @param ruleComposite
	 *            {@link AbstractRuleEditingElement} to be disposed.
	 */
	void elementDisposed(AbstractRuleEditingElement ruleComposite);
}
