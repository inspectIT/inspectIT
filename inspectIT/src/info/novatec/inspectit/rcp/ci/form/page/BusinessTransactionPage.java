package info.novatec.inspectit.rcp.ci.form.page;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.ci.form.part.business.BusinessTransactionMasterBlock;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * Edit page for {@link BusinessTransactionDefinition} instances of an {@link ApplicationDefinition}
 * .
 *
 * @author Alexander Wert
 *
 */
public class BusinessTransactionPage extends FormPage {

	/**
	 * Id of the page.
	 */
	private static final String ID = BusinessTransactionPage.class.getName();

	/**
	 * Page title.
	 */
	private static final String TITLE = "Business Transaction Definitions";

	/**
	 * Main form of this editor page.
	 */
	private ScrolledForm mainForm;

	/**
	 * business transactions master block.
	 */
	private final BusinessTransactionMasterBlock businessTransactionMasterBlock;

	/**
	 * Default constructor.
	 *
	 * @param editor
	 *            {@link FormEditor} page belongs to.
	 */
	public BusinessTransactionPage(FormEditor editor) {
		super(editor, ID, TITLE);
		this.businessTransactionMasterBlock = new BusinessTransactionMasterBlock(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		mainForm = managedForm.getForm();
		mainForm.setText(TITLE);
		mainForm.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BUSINESS_TRANSACTION));
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(mainForm.getForm());

		businessTransactionMasterBlock.createContent(managedForm);
	}
}
