package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.IMatchingRuleProvider;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;

/**
 * @author Alexander Wert
 *
 */
public interface IMatchingRulesPart extends IFormPart {
	/**
	 * Creates the part content.
	 *
	 * @param managedForm
	 *            the {@link IManagedForm} to add this part to
	 * @param parent
	 *            the parent composite
	 */
	void createContent(IManagedForm managedForm, Composite parent);

	/**
	 * Reinitializes the contents of the sub-elements according to the {@link AbstractExpression}
	 * retrieved from the provided {@link IMatchingRuleProvider} instance.
	 *
	 * @param ruleProvider
	 *            {@link IMatchingRuleProvider} instance providing the {@link AbstractExpression}
	 *            that describes the content of this form part.
	 */
	void initContent(IMatchingRuleProvider ruleProvider);

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

	/**
	 * Sets editable state.
	 *
	 * @param editable
	 *            editable state.
	 */
	void setEditable(boolean editable);
}
