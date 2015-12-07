package info.novatec.inspectit.rcp.ci.wizard;

import info.novatec.inspectit.cmr.configuration.business.IBusinessTransactionDefinition;
import info.novatec.inspectit.rcp.ci.wizard.page.DefineNameAndDescriptionWizardPage;
import info.novatec.inspectit.rcp.ci.wizard.page.NameDescriptionInsertBeforeWizardPage;

import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for editing the name and description of a {@link IBusinessTransactionDefinition}.
 * 
 * @author Alexander Wert
 * 
 */
public class EditBusinessTransactionWizard extends Wizard {
	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Create New Business Transaction";

	/**
	 * Wizard default message.
	 */
	private static final String MESSAGE = "Define the information for the new business transaction";

	/**
	 * {@link IBusinessTransactionDefinition} to modify.
	 */
	private IBusinessTransactionDefinition businessTransaction;

	/**
	 * {@link NameDescriptionInsertBeforeWizardPage}.
	 */
	private DefineNameAndDescriptionWizardPage editNameAndDescriptionWizardPage;

	/**
	 * Resulting name.
	 */
	private String name;

	/**
	 * Resulting description.
	 */
	private String description;

	/**
	 * Default constructor.
	 * 
	 * @param businessTransaction
	 *            {@link IBusinessTransactionDefinition} to modify.
	 */
	public EditBusinessTransactionWizard(IBusinessTransactionDefinition businessTransaction) {
		this.setWindowTitle(TITLE);
		this.businessTransaction = businessTransaction;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		editNameAndDescriptionWizardPage = new DefineNameAndDescriptionWizardPage(TITLE, MESSAGE, businessTransaction.getBusinessTransactionName(), businessTransaction.getDescription());
		addPage(editNameAndDescriptionWizardPage);

	}

	@Override
	public boolean canFinish() {
		return editNameAndDescriptionWizardPage.isPageComplete();
	}

	/**
	 * Gets {@link #name}.
	 * 
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets {@link #description}.
	 * 
	 * @return {@link #description}
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public boolean performFinish() {
		name = editNameAndDescriptionWizardPage.getName();
		description = editNameAndDescriptionWizardPage.getDescription();
		return true;
	}

}
