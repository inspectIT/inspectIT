package rocks.inspectit.ui.rcp.wizard;

import java.util.Objects;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.wizard.page.DefineCmrWizardPage;
import rocks.inspectit.ui.rcp.wizard.page.PreviewCmrDataWizardPage;

/**
 * Wizard for adding the {@link rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition}.
 *
 * @author Ivan Senic
 *
 */
public class AddCmrRepositoryWizard extends Wizard implements INewWizard {

	/**
	 * {@link DefineCmrWizardPage}.
	 */
	private DefineCmrWizardPage defineCmrWizardPage;

	/**
	 * {@link PreviewCmrDataWizardPage}.
	 */
	private PreviewCmrDataWizardPage previewCmrDataWizardPage;

	/**
	 * Default constructor.
	 */
	public AddCmrRepositoryWizard() {
		this.setWindowTitle("Add Central Management Repository (CMR)");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_SERVER));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		defineCmrWizardPage = new DefineCmrWizardPage("Add New CMR Repository");
		addPage(defineCmrWizardPage);
		previewCmrDataWizardPage = new PreviewCmrDataWizardPage();
		addPage(previewCmrDataWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (Objects.equals(page, defineCmrWizardPage)) {
			previewCmrDataWizardPage.cancel();
			previewCmrDataWizardPage.update(defineCmrWizardPage.getCmrRepositoryDefinition());
		}
		return super.getNextPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		CmrRepositoryDefinition cmrRepositoryDefinition = defineCmrWizardPage.getCmrRepositoryDefinition();
		InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryDefinition(cmrRepositoryDefinition);
		return true;
	}

}
