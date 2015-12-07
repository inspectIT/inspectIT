package info.novatec.inspectit.rcp.ci.wizard;

import info.novatec.inspectit.ci.business.BooleanExpression;
import info.novatec.inspectit.ci.business.BusinessTransactionDefinition;
import info.novatec.inspectit.ci.business.MatchingRule;
import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
import info.novatec.inspectit.cmr.configuration.business.IBusinessTransactionDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.view.BusinessContextManagerViewPart;
import info.novatec.inspectit.rcp.ci.wizard.page.NameDescriptionInsertBeforeWizardPage;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for creating new {@link IBusinessTransactionDefinition}.
 * 
 * @author Alexander Wert
 * 
 */
public class CreateBusinessTransactionWizard extends Wizard {
	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Create New Business Transaction";

	/**
	 * Wizard default message.
	 */
	private static final String MESSAGE = "Define the information for the new business transaction";

	/**
	 * {@link IApplicationDefinition} the new {@link IBusinessTransactionDefinition} shell be added
	 * to.
	 */
	private IApplicationDefinition application;

	/**
	 * The resulting {@link IBusinessTransactionDefinition} instance of this wizard.
	 */
	private IBusinessTransactionDefinition newBusinessTransaction;
	/**
	 * {@link NameDescriptionInsertBeforeWizardPage}.
	 */
	private NameDescriptionInsertBeforeWizardPage newItemWizardPage;

	/**
	 * Default constructor.
	 * 
	 * @param application
	 *            {@link IApplicationDefinition} the new {@link IBusinessTransactionDefinition}
	 *            shell be added to.
	 */
	public CreateBusinessTransactionWizard(IApplicationDefinition application) {
		this.setWindowTitle(TITLE);
		this.application = application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {

		List<IBusinessTransactionDefinition> businessTransactions = application.getBusinessTransactionDefinitions();
		String[] items = new String[businessTransactions.size()];
		int i = 0;
		for (IBusinessTransactionDefinition businessTx : businessTransactions) {
			items[i] = businessTx.getBusinessTransactionName();
			i++;
		}
		newItemWizardPage = new NameDescriptionInsertBeforeWizardPage(TITLE, MESSAGE, items, BusinessContextManagerViewPart.B_TX_ORDER_INFO_TOOLTIP);
		addPage(newItemWizardPage);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		String name = newItemWizardPage.getName();
		String description = newItemWizardPage.getDescription();
		int insertBeforeIndex = newItemWizardPage.getInsertedBeforeIndex();

		MatchingRule matchingRule = new MatchingRule(new BooleanExpression(false));
		newBusinessTransaction = new BusinessTransactionDefinition(name);
		if (StringUtils.isNotBlank(description)) {
			newBusinessTransaction.setDescription(description);
		}
		newBusinessTransaction.setMatchingRule(matchingRule);
		try {
			application.addBusinessTransactionDefinition(newBusinessTransaction, insertBeforeIndex);
		} catch (BusinessException e) {
			InspectIT.getDefault().createErrorDialog("Adding the business transaction definition '" + newBusinessTransaction.getBusinessTransactionName() + "' failed due to the following exception.",
					e, -1);
			return false;
		}
		return true;
	}

	@Override
	public boolean canFinish() {
		return newItemWizardPage.isPageComplete();
	}

	/**
	 * Gets {@link #newBusinessTransaction}.
	 * 
	 * @return {@link #newBusinessTransaction}
	 */
	public IBusinessTransactionDefinition getNewBusinessTransaction() {
		return newBusinessTransaction;
	}

}
