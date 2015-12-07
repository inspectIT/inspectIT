/**
 *
 */
package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.business.impl.AbstractExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.ci.business.impl.NameExtractionExpression;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * @author Alexander Wert
 *
 */
public class BusinessTransactionDetailsPage implements IDetailsPage, IPropertyListener {

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
	private BusinessTransactionDefinition selectedBusinessTransaction;

	/**
	 * Constructor.
	 *
	 * @param formPage
	 *            The {@link FormPage} this details page belongs to.
	 */
	public BusinessTransactionDetailsPage(FormPage formPage) {
		this.formPage = formPage;
		this.formPage.getEditor().addPropertyListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createContents(Composite parent) {
		parent.setLayout(new GridLayout(2, true));
		rulesPart = new SelectiveRulesPart("Business Transaction Mapping", parent, formPage.getManagedForm());
		nameExtractionPart = new DynamicNameExtractionPart(parent, formPage.getManagedForm());
		rulesPart.addPropertyListeners(this);
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
	public void dispose() {
		if (null != rulesPart) {
			rulesPart.removePropertyListener(this);
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
	public void commit(boolean onSave) {
		if (null != selectedBusinessTransaction && selectedBusinessTransaction.getId() != BusinessTransactionDefinition.DEFAULT_ID) {
			AbstractExpression rule = rulesPart.constructMatchingRuleExpression();
			selectedBusinessTransaction.setMatchingRuleExpression(rule);
			NameExtractionExpression nameExtractionExpression = nameExtractionPart.constructNameExtractionExpression();
			selectedBusinessTransaction.setNameExtractionExpression(nameExtractionExpression);
		}
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
			commit(false);
			selectedBusinessTransaction = (BusinessTransactionDefinition) ((StructuredSelection) selection).getFirstElement();
			if (null != selectedBusinessTransaction) {
				rulesPart.setEditable(selectedBusinessTransaction.getId() != BusinessTransactionDefinition.DEFAULT_ID);
				nameExtractionPart.setEditable(selectedBusinessTransaction.getId() != BusinessTransactionDefinition.DEFAULT_ID);
				rulesPart.initContent(selectedBusinessTransaction.getMatchingRuleExpression());
				rulesPart.setDescriptionText("Define the matching rule that should be used to match the selected business transaction:");
				nameExtractionPart.init(selectedBusinessTransaction.getNameExtractionExpression());
			}
			managedForm.getForm().layout(true, true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_INPUT && null != selectedBusinessTransaction) {
			ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) formPage.getEditor().getEditorInput();
			ApplicationDefinition application = input.getApplication();

			try {
				selectedBusinessTransaction = application.getBusinessTransactionDefinition(selectedBusinessTransaction.getId());
			} catch (BusinessException e) {
				selectedBusinessTransaction = application.getDefaultBusinessTransactionDefinition();
			}

			if (null != selectedBusinessTransaction) {
				rulesPart.initContent(selectedBusinessTransaction.getMatchingRuleExpression());
			}
		} else if (propId == SelectiveRulesPart.ADVANCED_VIEW_TOGGLE_PROP_ID) {
			commit(false);
			rulesPart.initContent(selectedBusinessTransaction.getMatchingRuleExpression());
		}
	}

}
