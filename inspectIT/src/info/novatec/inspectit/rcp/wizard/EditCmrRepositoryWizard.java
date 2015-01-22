package info.novatec.inspectit.rcp.wizard;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.page.DefineCmrWizardPage;
import info.novatec.inspectit.rcp.wizard.page.PreviewCmrDataWizardPage;

import java.util.Objects;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for editing the {@link info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition}.
 * 
 * @author Ivan Senic
 * 
 */
public class EditCmrRepositoryWizard extends Wizard implements INewWizard {

	/**
	 * {@link DefineCmrWizardPage}.
	 */
	private DefineCmrWizardPage defineCmrWizardPage;

	/**
	 * {@link PreviewCmrDataWizardPage}.
	 */
	private PreviewCmrDataWizardPage previewCmrDataWizardPage;

	/**
	 * Repository to edit.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            Repository to edit
	 */
	public EditCmrRepositoryWizard(CmrRepositoryDefinition cmrRepositoryDefinition) {
		Assert.isNotNull(cmrRepositoryDefinition);
		this.setWindowTitle("Edit Central Management Repository (CMR)");
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_EDIT));
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
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
		defineCmrWizardPage = new DefineCmrWizardPage("Edit CMR Repository", cmrRepositoryDefinition);
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
		CmrRepositoryDefinition editedRepository = defineCmrWizardPage.getCmrRepositoryDefinition();
		if (Objects.equals(editedRepository, cmrRepositoryDefinition)) {
			// if they equal port and IP did not change just update the name/desc
			cmrRepositoryDefinition.setName(editedRepository.getName());
			cmrRepositoryDefinition.setDescription(editedRepository.getDescription());
			InspectIT.getDefault().getCmrRepositoryManager().updateCmrRepositoryDefinitionData(cmrRepositoryDefinition);
		} else {
			// if not then remove and add
			InspectIT.getDefault().getCmrRepositoryManager().removeCmrRepositoryDefinition(cmrRepositoryDefinition);
			InspectIT.getDefault().getCmrRepositoryManager().addCmrRepositoryDefinition(editedRepository);
		}

		return true;
	}

}
