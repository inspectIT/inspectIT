package rocks.inspectit.ui.rcp.ci.wizard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AbstractCiData;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceImportData;
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
	private ConfigurationInterfaceImportData importData;

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
					continueImport = checkOverwrite(existingProfile, profile);
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
				if (Objects.equals(environment.getId(), existingEnvironment.getId())) {
					continueImport = checkOverwrite(existingEnvironment, environment);
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

					Environment imported = cmrRepositoryDefinition.getConfigurationInterfaceService().importEnvironment(environment);
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
			cmrRepositoryDefinition = importSelectWizardPage.getCmrRepositoryDefinition();
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				@Override
				public void run() {
					try {
						loadImportData();
					} catch (IOException | BusinessException e) {
						InspectIT.getDefault().createErrorDialog("Import data not valid.", e, -1);
					}
				}
			});

			if (null == importData) {
				return importSelectWizardPage;
			}

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

		if (Objects.equals(page, importSelectWizardPage) && CollectionUtils.isEmpty(importData.getEnvironments())) {
			return selectProfilesWizardPage;
		}

		if (Objects.equals(page, selectEnvironmentsWizardPage) && CollectionUtils.isEmpty(importData.getProfiles())) {
			return null;
		}

		return super.getNextPage(page);
	}

	/**
	 * Loads import data.
	 *
	 * @throws IOException
	 *             If IO exception occurs during loading.
	 * @throws BusinessException
	 *             If business exception occurs during loading.
	 */
	private void loadImportData() throws IOException, BusinessException {
		String fileName = importSelectWizardPage.getFileName();

		if (StringUtils.isBlank(fileName)) {
			importData = null; // NOPMD
			return;
		}

		Path path = Paths.get(fileName);
		if (!Files.exists(path)) {
			importData = null; // NOPMD
			return;
		}

		byte[] data = Files.readAllBytes(path);
		importData = cmrRepositoryDefinition.getConfigurationInterfaceService().getImportData(data);
	}

	/**
	 * Checks if {@link AbstractCiData} should be overwritten on the import.
	 *
	 * @param existingData
	 *            Existing local data.
	 * @param importData
	 *            Import data.
	 * @return True if overwrite should be performed.
	 */
	private boolean checkOverwrite(AbstractCiData existingData, AbstractCiData importData) {
		if (!Objects.equals(existingData.getId(), importData.getId())) {
			return false;
		}

		String item = "item";
		if (existingData instanceof Profile) {
			item = "profile";
		} else if (existingData instanceof Environment) {
			item = "environment";
		}

		String message;
		if (null == existingData.getImportDate()) {
			// we did not import this data at any point
			message = "The " + item + " " + importData.getName() + " has the same ID as the item that already exists on the CMR. Would you like to overwrite existing " + item + "?";
		} else {
			// if we did not update the existing data after import then overwrite if there is update
			Date existingDataUpdateDate = (null != existingData.getUpdatedDate()) ? existingData.getUpdatedDate() : existingData.getCreatedDate();
			if (existingDataUpdateDate.before(existingData.getImportDate())) {
				Date importDataUpdateDate = (null != importData.getUpdatedDate()) ? importData.getUpdatedDate() : importData.getCreatedDate();
				if (Objects.equals(importDataUpdateDate, existingDataUpdateDate)) {
					// if there is no updates then check what the user wants
					message = "The " + item + " " + importData.getName()
					+ " already exists on the CMR. It seems that both existing data and importing data do not have updates since the time the data was originally imported. It's advised to skip the importing as the overwrite would not bring any changes. Would you like to overwrite existing "
					+ item + " anyway?";
				} else {
					// if profile to import has updates then overwrite
					return true;
				}
			}

			// in last case we both updated the existing and import, thus ask
			message = "The " + item + " " + importData.getName()
			+ " already exists on the CMR. It seems that existing data have updates since the time the data was originally imported. These updates can not be merged. Would you like to overwrite existing "
			+ item + " and lose all local updates?";

		}
		MessageDialog dialog = new MessageDialog(getShell(), StringUtils.capitalize(item) + " Exists", null, message, MessageDialog.QUESTION, new String[] { "Skip", "Overwrite" }, 0);
		return dialog.open() == 1;
	}

}