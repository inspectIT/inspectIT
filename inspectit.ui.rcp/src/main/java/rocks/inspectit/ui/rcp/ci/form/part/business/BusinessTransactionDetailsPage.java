package rocks.inspectit.ui.rcp.ci.form.part.business;

import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import com.google.common.base.Objects;

import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.validation.AbstractValidationManager;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * @author Alexander Wert
 *
 */
public class BusinessTransactionDetailsPage implements IDetailsPage {

	/**
	 * Name of the business transaction mapping form part.
	 */
	private static final String BUSINESS_TRANSACTION_MAPPING_PART_NAME = "Business Transaction Mapping";

	/**
	 * The {@link FormPage} this details page belongs to.
	 */
	private final FormPage formPage;

	/**
	 * Corresponding {@link IManagedForm}.
	 */
	private IManagedForm managedForm;

	/**
	 * Part for managing the matching rules.
	 */
	private SelectiveRulesPart rulesPart;

	/**
	 * Part for managing dynamic name extraction.
	 */
	private DynamicNameExtractionPart nameExtractionPart;
	/**
	 * Selected business transaction.
	 */
	private BusinessTransactionDefinition selectedBusinessTransaction;

	/**
	 * The upstream validation manager to delegate aggregated validation messages to.
	 */
	private final AbstractValidationManager<BusinessTransactionDefinition> upstreamValidationManager;

	/**
	 * Constructor.
	 *
	 * @param formPage
	 *            The {@link FormPage} this details page belongs to.
	 * @param validationManager
	 *            {@link AbstractValidationManager} instance to be notified on validation state
	 *            changes.
	 */
	public BusinessTransactionDetailsPage(FormPage formPage, AbstractValidationManager<BusinessTransactionDefinition> validationManager) {
		this.formPage = formPage;
		this.upstreamValidationManager = validationManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		parent.setLayout(new GridLayout(2, true));
		BusinessTransactionPartsValidationManager<AbstractExpression> rulesValidationManager = new BusinessTransactionPartsValidationManager<AbstractExpression>(upstreamValidationManager);
		rulesPart = new SelectiveRulesPart(BUSINESS_TRANSACTION_MAPPING_PART_NAME, parent, formPage.getManagedForm(), rulesValidationManager);

		BusinessTransactionPartsValidationManager<String> nameExtractionValidationManager = new BusinessTransactionPartsValidationManager<String>(upstreamValidationManager);
		nameExtractionPart = new DynamicNameExtractionPart(parent, formPage.getManagedForm(), nameExtractionValidationManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(IManagedForm form) {
		this.managedForm = form;

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
	public void dispose() {
		if (null != rulesPart) {
			rulesPart.dispose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDirty() {
		return rulesPart.isDirty() || nameExtractionPart.isDirty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setFormInput(Object input) {
		return rulesPart.setFormInput(input);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		rulesPart.setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isStale() {
		return rulesPart.isStale();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh() {
		rulesPart.refresh();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		if (!selection.isEmpty()) {
			selectedBusinessTransaction = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
			if (null != selectedBusinessTransaction) {
				rulesPart.initContent(selectedBusinessTransaction);
				rulesPart.setDescriptionText("Define the matching rule that should be used to match the selected business transaction:");
				nameExtractionPart.init(selectedBusinessTransaction);
				nameExtractionPart.setEditable(selectedBusinessTransaction.getId() != BusinessTransactionDefinition.DEFAULT_ID);
			}
			managedForm.getForm().layout(true, true);
		}
	}

	/**
	 * Validation manager responsible for aggregating validation messages to the corresponding form
	 * parts of the business transaction details page.
	 *
	 * @author Alexander Wert
	 *
	 * @param <K>
	 */
	private class BusinessTransactionPartsValidationManager<K> extends AbstractValidationManager<K> {

		/**
		 * The upstream validation manager to provide messages to.
		 */
		private final AbstractValidationManager<BusinessTransactionDefinition> upstreamValidationManager;

		/**
		 * Constructor.
		 *
		 * @param upstreamValidationManager
		 *            the upstream validation manager to provide messages to
		 */
		BusinessTransactionPartsValidationManager(AbstractValidationManager<BusinessTransactionDefinition> upstreamValidationManager) {
			this.upstreamValidationManager = upstreamValidationManager;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void notifyUpstream(K key, Set<ValidationState> states) {
			String errorsText = TextFormatter.getValidationErrorsCountText(states, "field");
			String part = getGroupName(key);
			String message = null != errorsText ? part + " (" + errorsText + ")" : "";
			upstreamValidationManager.validationStateChanged(selectedBusinessTransaction, new ValidationState(key, null == errorsText, message));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void showMessage(K key, Set<ValidationState> states) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void hideMessage(K key) {
		}

		/**
		 * Retrieves the group name from the given key.
		 *
		 * @param key
		 *            The key from which to retrieve the validation group from.
		 * @return the string representation of the validation group
		 */
		private String getGroupName(K key) {
			if (key instanceof StringMatchingExpression) {
				return MatchingRulesEditingElementFactory.getMatchingRuleType((AbstractExpression) key).toString();
			} else if (key instanceof MatchingRulesEditingElementFactory.InvalidExpression) {
				return BUSINESS_TRANSACTION_MAPPING_PART_NAME;
			} else if (Objects.equal(key, DynamicNameExtractionPart.TITLE)) {
				return DynamicNameExtractionPart.TITLE;
			} else {
				return "";
			}
		}
	}
}
