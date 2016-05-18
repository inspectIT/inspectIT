package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.export.ConfigurationInterfaceExportData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.wizard.page.ImportCiSelectWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.SelectEnvironmentsWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.SelectProfilesWizardPage;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

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
		// first profiles so that they are available for the environment if needed
		Collection<Profile> profiles = selectProfilesWizardPage.getProfiles();
		for (Profile profile : profiles) {
			try {
				Profile created = cmrRepositoryDefinition.getConfigurationInterfaceService().createProfile(profile);
				// notify listeners
				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileCreated(created, cmrRepositoryDefinition);
			} catch (BusinessException e) {
				InspectIT.getDefault().createErrorDialog("Profile can not be imported.", e, -1);
				return false;
			}
		}

		Collection<Environment> environments = selectEnvironmentsWizardPage.getEnvironments();
		for (Environment environment : environments) {
			try {
				Environment created = cmrRepositoryDefinition.getConfigurationInterfaceService().createEnvironment(environment);
				// notify listeners
				InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentCreated(created, cmrRepositoryDefinition);
			} catch (BusinessException e) {
				InspectIT.getDefault().createErrorDialog("Profile can not be imported.", e, -1);
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

			// then load data from the repository and set disallowed ids
			// repository must be online to reach this step
			if (!ObjectUtils.equals(cmrRepositoryDefinition, importSelectWizardPage.getCmrRepositoryDefinition())) {
				cmrRepositoryDefinition = importSelectWizardPage.getCmrRepositoryDefinition();
				Collection<Environment> allEnvironments = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();
				Collection<String> environmentIds = new HashSet<>();
				for (Environment environment : allEnvironments) {
					environmentIds.add(environment.getId());
				}
				selectEnvironmentsWizardPage.setDisallowedIds(environmentIds, "Selected environment(s) already exists on the import location");

				List<Profile> allProfiles = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();
				Collection<String> profileIds = new HashSet<>();
				for (Profile profile : allProfiles) {
					profileIds.add(profile.getId());
				}
				selectProfilesWizardPage.setDisallowedIds(profileIds, "Selected profiles(s) already exists on the import location");
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

}