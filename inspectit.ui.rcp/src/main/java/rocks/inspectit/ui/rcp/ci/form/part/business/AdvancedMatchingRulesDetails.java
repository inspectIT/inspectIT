package rocks.inspectit.ui.rcp.ci.form.part.business;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.IContainerExpression;
import rocks.inspectit.ui.rcp.ci.form.part.business.AdvancedMatchingRulesPart.RemoveSelection;
import rocks.inspectit.ui.rcp.ci.form.part.business.rules.AbstractRuleEditingElement;

/**
 * Details part for individual matching rules.
 *
 * @author Alexander Wert
 *
 */
public class AdvancedMatchingRulesDetails implements IDetailsPage {

	/**
	 * The {@link AbstractRuleEditingElement} to display and edit rule contents.
	 */
	private AbstractRuleEditingElement<?> ruleEditingElement;

	/**
	 * Main composite.
	 */
	private Composite main;

	/**
	 * Current {@link AbstractExpression} instance under modification.
	 */
	private AbstractExpression currentExpression;

	/**
	 * The master {@link AdvancedMatchingRulesPart}.
	 */
	private final AdvancedMatchingRulesPart master;

	/**
	 * {@link IManagedForm}.
	 */
	private IManagedForm managedForm;

	/**
	 * Constructor.
	 *
	 * @param master
	 *            the master {@link AdvancedMatchingRulesPart}.
	 */
	public AdvancedMatchingRulesDetails(AdvancedMatchingRulesPart master) {
		this.master = master;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
		managedForm = form;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit(boolean onSave) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStale() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (!selection.isEmpty()) {
			if (structuredSelection instanceof RemoveSelection) {
				AbstractExpression deletedExpression = (AbstractExpression) structuredSelection.getFirstElement();
				expressionDeleted(deletedExpression);
			} else {
				if (null != ruleEditingElement) {
					ruleEditingElement.dispose();
				}
				currentExpression = (AbstractExpression) structuredSelection.getFirstElement();
				ruleEditingElement = MatchingRulesEditingElementFactory.createRuleComposite(currentExpression, master.isEditable(), master.getValidationManager());
				ruleEditingElement.addModifyListener(master);
				ruleEditingElement.addDisposeListener(master);
				ruleEditingElement.createControls(main, managedForm.getToolkit(), false);
				main.layout(true, true);
				ruleEditingElement.initialize();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		main = managedForm.getToolkit().createComposite(parent);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(AbstractRuleEditingElement.NUM_GRID_COLUMNS, false);
		layout.horizontalSpacing = 8;
		main.setLayout(layout);
	}

	/**
	 * This method needs to be notified whenever an {@link AbstractExpression} instance has been
	 * deleted from the tree. This method performs an update of corresponding control validators.
	 *
	 * @param deletedExpression
	 *            deleted {@link AbstractExpression} instance
	 */
	private void expressionDeleted(AbstractExpression deletedExpression) {
		if (deletedExpression.equals(currentExpression)) {
			ruleEditingElement.dispose();
		} else if (deletedExpression instanceof IContainerExpression) {
			for (AbstractExpression childExpression : ((IContainerExpression) deletedExpression).getOperands()) {
				expressionDeleted(childExpression);
			}
		}
	}

}
