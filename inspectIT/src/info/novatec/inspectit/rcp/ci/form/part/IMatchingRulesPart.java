/**
 *
 */
package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormPart;

/**
 * @author Alexander Wert
 *
 */
public interface IMatchingRulesPart extends IFormPart {
	/**
	 * Constructs a {@link AbstractExpression} instance from the contents of this element controls.
	 *
	 * @return Returns a {@link AbstractExpression} instance.
	 */
	AbstractExpression constructMatchingRuleExpression();

	/**
	 * Reinitializes the contents of the sub-elements according to the {@link AbstractExpression}.
	 *
	 * @param matchingRuleExpression
	 *            {@link AbstractExpression} instance describing the content.
	 */
	void initContent(AbstractExpression matchingRuleExpression);

	/**
	 * Sets the description text for this section.
	 *
	 * @param description
	 *            new description text.
	 */
	void setDescriptionText(String description);

	/**
	 * Returns the control of the {@link IMatchingRulesPart}.
	 *
	 * @return the control of the {@link IMatchingRulesPart}.
	 */
	Control getControl();

	/**
	 * Returns the {@link ToolBarManager} of this part.
	 *
	 * @return the {@link ToolBarManager} of this part.
	 */
	ToolBarManager getToolbarManager();

	/**
	 * Marks this part as dirty.
	 */
	void markDirty();
}
