package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.page.DefineCmrWizardPage;
import info.novatec.inspectit.rcp.wizard.page.PreviewCmrDataWizardPage;

import java.util.Objects;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for adding the {@link info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition}.
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
