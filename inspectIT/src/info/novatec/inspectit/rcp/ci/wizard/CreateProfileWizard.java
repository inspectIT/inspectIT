package info.novatec.inspectit.rcp.ci.wizard;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.job.OpenProfileJob;
import info.novatec.inspectit.rcp.ci.wizard.page.DefineNameAndDescriptionWizardPage;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for creating new profile.
 * 
 * @author Ivan Senic
 * 
 */
public class CreateProfileWizard extends Wizard implements INewWizard {

	/**
	 * Wizard title.
	 */
	private static final String TITLE = "Create New Profile";

	/**
	 * Wizard title if duplicate mode is on.
	 */
	private static final String DUPLICATE_TITLE = "Duplicate Profile";

	/**
	 * Wizard default message.
	 */
	private static final String MESSAGE = "Define the information for the new Profile";

	/**
	 * Profile to duplicate if duplicate mode is on.
	 */
	private Profile duplicateProfile;

	/**
	 * {@link CmrRepositoryDefinition} to create profile on.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * {@link DefineNameAndDescriptionWizardPage}.
	 */
	private DefineNameAndDescriptionWizardPage defineNameAndDescriptionWizardPage;

	/**
	 * Workbench of the wizard.
	 */
	private IWorkbench workbench;

	/**
	 * Default constructor.
	 */
	public CreateProfileWizard() {
		this.setWindowTitle(TITLE);
	}

	/**
	 * Constructor that can be used for duplicate profile mode.
	 * <p>
	 * Note that {@link #init(IWorkbench, IStructuredSelection)} must be called if this constructor
	 * is used.
	 * 
	 * @param profile
	 *            Profile to be duplicated.
	 */
	public CreateProfileWizard(Profile profile) {
		Assert.isNotNull(profile);

		this.duplicateProfile = profile;

		this.setWindowTitle(getTitle());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addPages() {
		defineNameAndDescriptionWizardPage = new DefineNameAndDescriptionWizardPage(getTitle(), MESSAGE);
		addPage(defineNameAndDescriptionWizardPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) structuredSelection.getFirstElement()).getCmrRepositoryDefinition();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performFinish() {
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			InspectIT.getDefault().createErrorDialog("Profile can not be created. Selected CMR repository is currently not available.", -1);
			return false;
		}

		String name = defineNameAndDescriptionWizardPage.getName();
		String description = defineNameAndDescriptionWizardPage.getDescription();
		Profile profile = new Profile();
		profile.setName(name);
		if (StringUtils.isNotBlank(description)) {
			profile.setDescription(description);
		}

		if (isDuplicate()) {
			profile.setMethodSensorAssignments(duplicateProfile.getMethodSensorAssignments());
			profile.setExceptionSensorAssignments(duplicateProfile.getExceptionSensorAssignments());
			profile.setExcludeRules(duplicateProfile.getExcludeRules());
			profile.setActive(duplicateProfile.isActive());
			profile.setDefaultProfile(duplicateProfile.isDefaultProfile());
		}

		try {
			Profile created = cmrRepositoryDefinition.getConfigurationInterfaceService().createProfile(profile);

			// open the created one immediately
			new OpenProfileJob(cmrRepositoryDefinition, created.getId(), workbench.getActiveWorkbenchWindow().getActivePage()).schedule();

			// notify listeners
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileCreated(created, cmrRepositoryDefinition);
		} catch (BusinessException e) {
			InspectIT.getDefault().createErrorDialog("Profile can not be created.", e, -1);
			return false;
		}

		return true;
	}

	/**
	 * @return Title for the wizard page.
	 */
	private String getTitle() {
		if (isDuplicate()) {
			return DUPLICATE_TITLE;
		} else {
			return TITLE;
		}
	}

	/**
	 * @return If mode of wizard is duplicate of the profile.
	 */
	private boolean isDuplicate() {
		return duplicateProfile != null;
	}
}
