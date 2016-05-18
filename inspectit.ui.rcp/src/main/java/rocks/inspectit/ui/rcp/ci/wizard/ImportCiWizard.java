package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceExportData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.editor.EnvironmentEditor;
import rocks.inspectit.ui.rcp.ci.form.editor.ProfileEditor;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;
import rocks.inspectit.ui.rcp.ci.form.input.ProfileEditorInput;
import rocks.inspectit.ui.rcp.ci.wizard.page.ImportCiSelectWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.SelectEnvironmentsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.SelectProfilesWizardPage;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Wizard for importing configuration interface data.
 *
 * @author Ivan Senic
 *
 */
public class ImportCiWizard extends Wizard implements IImportWizard {

	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Import Instrumentation Configuration";

	/**
	 * {@link CmrRepositoryDefinition} to import data to.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Data to be imported.
	 */
	private ConfigurationInterfaceExportData importData;

	/**
	 * Selection page for file and {@link CmrRepositoryDefinition}.
	 */
	private ImportCiSelectWizardPage importSelectWizardPage;

	/**
	 * Selection for environments to import.
	 */
	private SelectEnvironmentsWizardPage selectEnvironmentsWizardPage;

	/**
	 * Selection for the profiles to import.
	 */
	private SelectProfilesWizardPage selectProfilesWizardPage;

	/**
	 * Default constructor.
	 */
	public ImportCiWizard() {
		this.setWindowTitle(TITLE);
		this.setDefaultPageImageDescriptor(InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_WIZBAN_IMPORT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) structuredSelection.getFirstElement()).getCmrRepositoryDefinition();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		importSelectWizardPage = new ImportCiSelectWizardPage("Import Location", cmrRepositoryDefinition);
		addPage(importSelectWizardPage);

		selectEnvironmentsWizardPage = new SelectEnvironmentsWizardPage("Import Environment(s)", "Select environments to import from file.", Collections.<Environment> emptyList(),
				Collections.<String> emptyList());
		addPage(selectEnvironmentsWizardPage);

		selectProfilesWizardPage = new SelectProfilesWizardPage("Import Profile(s)", "Select profiles to import from file.", Collections.<Profile> emptyList(), Collections.<String> emptyList());
		addPage(selectProfilesWizardPage);

		// reset CMR
		cmrRepositoryDefinition = null; // NOPMD
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			InspectIT.getDefault().createInfoDialog("Selected CMR is currently offline.", -1);
			return false;
		}

		// get active pages
		IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();

		List<Profile> existingProfiles = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();
		Collection<Environment> existingEnvironments = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();

		// first profiles so that they are available for the environment if needed
		Collection<Profile> profiles = selectProfilesWizardPage.getProfiles();
		for (Profile profile : profiles) {
			boolean continueImport = true;
			boolean overwrite = false;

			// check if profile already exists
			for (Profile existingProfile : existingProfiles) {
				if (Objects.equals(profile.getId(), existingProfile.getId())) {
					int revisionToImport = profile.getRevision();
					int existingRevision = existingProfile.getRevision();

					String message = "The profile '" + profile.getName() + "' already exists on the CMR. The existing profile has revision version " + existingRevision
							+ ", while the profile to import has the revision version " + revisionToImport + ". Would you like to overwrite existing profile?";
					int selection = revisionToImport > existingRevision ? 1 : 0;
					MessageDialog dialog = new MessageDialog(getShell(), "Profile Exists", null, message, MessageDialog.QUESTION, new String[] { "Skip", "Overwrite" }, selection);
					continueImport = dialog.open() == 1;
					overwrite = continueImport;
					break;
				}
			}

			if (continueImport) {
				// check if we are currently editing this profile
				if (overwrite) {
					for (IWorkbenchPage page : pages) {
						for (IEditorReference reference : page.getEditorReferences()) {
							IEditorPart editor = reference.getEditor(false);
							if (editor instanceof ProfileEditor) {
								ProfileEditorInput input = (ProfileEditorInput) editor.getEditorInput();
								if (Objects.equals(profile.getId(), input.getProfile().getId())) {
									MessageDialog.openError(getShell(), "Owerwrite Profile",
											"The profile '" + profile.getName() + "' can not be overwritten as it's curently being edited. Please close the editor for the profile and try again.");
								}
							}
						}
					}
				}

				try {
					Profile imported = cmrRepositoryDefinition.getConfigurationInterfaceService().importProfile(profile);
					// notify listeners
					if (overwrite) {
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileUpdated(imported, cmrRepositoryDefinition, false);
					} else {
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileCreated(imported, cmrRepositoryDefinition);
					}
				} catch (BusinessException e) {
					InspectIT.getDefault().createErrorDialog("Profile can not be imported.", e, -1);
					return false;
				}
			}
		}

		// then environments
		Collection<Environment> environments = selectEnvironmentsWizardPage.getEnvironments();
		for (Environment environment : environments) {
			boolean continueImport = true;
			boolean overwrite = false;

			// check if profile already exists
			for (Environment existingEnvironment : existingEnvironments) {
				if (Objects.equals(existingEnvironment.getId(), existingEnvironment.getId())) {
					int revisionToImport = environment.getRevision();
					int existingRevision = existingEnvironment.getRevision();

					String message = "The environment '" + environment.getName() + "' already exists on the CMR. The existing environment has revision version " + existingRevision
							+ ", while the environment to import has the revision version " + revisionToImport + ". Would you like to overwrite existing environment?";
					int selection = revisionToImport > existingRevision ? 1 : 0;
					MessageDialog dialog = new MessageDialog(getShell(), "Environment Exists", null, message, MessageDialog.QUESTION, new String[] { "Skip", "Overwrite" }, selection);
					continueImport = dialog.open() == 1;
					overwrite = continueImport;
					break;
				}
			}

			try {
				if (continueImport) {
					// check if we are currently editing this environment
					if (overwrite) {
						for (IWorkbenchPage page : pages) {
							for (IEditorReference reference : page.getEditorReferences()) {
								IEditorPart editor = reference.getEditor(false);
								if (editor instanceof EnvironmentEditor) {
									EnvironmentEditorInput input = (EnvironmentEditorInput) editor.getEditorInput();
									if (Objects.equals(environment.getId(), input.getEnvironment().getId())) {
										MessageDialog.openError(getShell(), "Owerwrite Environment", "The environment '" + environment.getName()
										+ "' can not be overwritten as it's curently being edited. Please close the editor for the environment and try again.");
									}
								}
							}
						}
					}

					Environment imported = cmrRepositoryDefinition.getConfigurationInterfaceService().createEnvironment(environment);
					// notify listeners
					if (overwrite) {
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentUpdated(imported, cmrRepositoryDefinition);
					} else {
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentCreated(imported, cmrRepositoryDefinition);
					}
				}
			} catch (BusinessException e) {
				InspectIT.getDefault().createErrorDialog("Environment can not be imported.", e, -1);
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (Objects.equals(page, importSelectWizardPage)) {
			// first get what we are trying to import
			if (!Objects.equals(importData, importSelectWizardPage.getImportData())) {
				importData = importSelectWizardPage.getImportData();
				if (CollectionUtils.isNotEmpty(importData.getEnvironments())) {
					Collection<String> environmentIds = new HashSet<>();
					for (Environment environment : importData.getEnvironments()) {
						environmentIds.add(environment.getId());
					}
					selectEnvironmentsWizardPage.setEnvironments(importData.getEnvironments());
					selectEnvironmentsWizardPage.setSelectedIds(environmentIds);
				} else {
					selectEnvironmentsWizardPage.setEnvironments(Collections.<Environment> emptyList());
					selectEnvironmentsWizardPage.setSelectedIds(Collections.<String> emptyList());
				}
				if (CollectionUtils.isNotEmpty(importData.getProfiles())) {
					Collection<String> profileIds = new HashSet<>();
					for (Profile profile : importData.getProfiles()) {
						profileIds.add(profile.getId());
					}
					selectProfilesWizardPage.setProfiles(importData.getProfiles());
					selectProfilesWizardPage.setSelectedIds(profileIds);
				} else {
					selectProfilesWizardPage.setProfiles(Collections.<Profile> emptyList());
					selectProfilesWizardPage.setSelectedIds(Collections.<String> emptyList());
				}
			}

			cmrRepositoryDefinition = importSelectWizardPage.getCmrRepositoryDefinition();
		}

		if (Objects.equals(page, importSelectWizardPage) && CollectionUtils.isEmpty(importData.getEnvironments())) {
			return selectProfilesWizardPage;
		}

		if (Objects.equals(page, selectEnvironmentsWizardPage) && CollectionUtils.isEmpty(importData.getProfiles())) {
			return null;
		}

		return super.getNextPage(page);
	}

}