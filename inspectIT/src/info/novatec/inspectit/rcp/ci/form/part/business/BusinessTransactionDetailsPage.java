package info.novatec.inspectit.rcp.ci.form.part.business;

import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.rcp.validation.IValidatorRegistry;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * @author Alexander Wert
 *
 */
public class BusinessTransactionDetailsPage implements IDetailsPage {

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
	 * Id of the currently selected business transaction.
	 */
	private int selectedBusinessTransactionId = 0;
	/**
	 * The {@link IValidatorRegistry} instance to delegate validator events to.
	 */
	private final IValidatorRegistry validatorRegistry;

	/**
	 * Constructor.
	 *
	 * @param formPage
	 *            The {@link FormPage} this details page belongs to.
	 * @param validatorRegistry
	 *            {@link IValidatorRegistry} instance to be notified on validation state changes and
	 *            to register {@link ValidationControlDecoration} to.
	 */
	public BusinessTransactionDetailsPage(FormPage formPage, IValidatorRegistry validatorRegistry) {
		this.formPage = formPage;
		this.validatorRegistry = validatorRegistry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		parent.setLayout(new GridLayout(2, true));
		rulesPart = new SelectiveRulesPart("Business Transaction Mapping", parent, formPage.getManagedForm(), validatorRegistry);
		nameExtractionPart = new DynamicNameExtractionPart(parent, formPage.getManagedForm(), validatorRegistry);
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
		nameExtractionPart.disposeValidatorDecorations(false);
		if (!selection.isEmpty()) {
			BusinessTransactionDefinition businessTransaction = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
			if (null != businessTransaction) {
				selectedBusinessTransactionId = businessTransaction.getId();
				rulesPart.initContent(businessTransaction);
				rulesPart.setDescriptionText("Define the matching rule that should be used to match the selected business transaction:");
				nameExtractionPart.init(businessTransaction);
				nameExtractionPart.setEditable(selectedBusinessTransactionId != BusinessTransactionDefinition.DEFAULT_ID);
			}
			managedForm.getForm().layout(true, true);
		}
	}

}
