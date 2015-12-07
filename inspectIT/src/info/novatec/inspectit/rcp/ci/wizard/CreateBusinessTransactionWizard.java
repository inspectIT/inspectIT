package info.novatec.inspectit.rcp.ci.wizard;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.ci.business.expression.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.view.BusinessContextManagerViewPart;
import info.novatec.inspectit.rcp.ci.wizard.page.NameDescriptionInsertBeforeWizardPage;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for creating new {@link BusinessTransactionDefinition}.
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
	 * {@link ApplicationDefinition} the new {@link BusinessTransactionDefinition} shell be added
	 * to.
	 */
	private final ApplicationDefinition application;

	/**
	 * The resulting {@link BusinessTransactionDefinition} instance of this wizard.
	 */
	private BusinessTransactionDefinition newBusinessTransaction;

	/**
	 * {@link NameDescriptionInsertBeforeWizardPage}.
	 */
	private NameDescriptionInsertBeforeWizardPage newItemWizardPage;

	/**
	 * Default constructor.
	 *
	 * @param application
	 *            {@link ApplicationDefinition} the new {@link BusinessTransactionDefinition} shell
	 *            be added to.
	 */
	public CreateBusinessTransactionWizard(ApplicationDefinition application) {
		this.setWindowTitle(TITLE);
		this.application = application;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		List<BusinessTransactionDefinition> businessTransactions = application.getBusinessTransactionDefinitions();
		String[] items = new String[businessTransactions.size()];
		int i = 0;
		for (BusinessTransactionDefinition businessTx : businessTransactions) {
			items[i] = businessTx.getBusinessTransactionDefinitionName();
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

		AbstractExpression matchingRuleExpression = new BooleanExpression(false);
		newBusinessTransaction = new BusinessTransactionDefinition(name);
		if (StringUtils.isNotBlank(description)) {
			newBusinessTransaction.setDescription(description);
		}
		newBusinessTransaction.setMatchingRuleExpression(matchingRuleExpression);
		try {
			application.addBusinessTransactionDefinition(newBusinessTransaction, insertBeforeIndex);
		} catch (BusinessException e) {
			InspectIT.getDefault().createErrorDialog(
					"Adding the business transaction definition '" + newBusinessTransaction.getBusinessTransactionDefinitionName() + "' failed due to the following exception.", e, -1);
			return false;
		}
		return true;
	}

	/**
	 * Gets {@link #newBusinessTransaction}.
	 *
	 * @return {@link #newBusinessTransaction}
	 */
	public BusinessTransactionDefinition getNewBusinessTransaction() {
		return newBusinessTransaction;
	}

}
