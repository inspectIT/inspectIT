package rocks.inspectit.ui.rcp.ci.wizard;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.job.OpenProfileJob;
import rocks.inspectit.ui.rcp.ci.wizard.page.DefineNameAndDescriptionWizardPage;
import rocks.inspectit.ui.rcp.ci.wizard.page.DefineProfileWizardPage;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.job.BlockingJob;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

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
	private DefineProfileWizardPage defineProfileWizardPage;

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
		BlockingJob<Collection<Profile>> job = new BlockingJob<>("Fetching profiles..", new Callable<Collection<Profile>>() {
			@Override
			public Collection<Profile> call() throws Exception {
				return cmrRepositoryDefinition.getConfigurationInterfaceService().getAllProfiles();
			}
		});

		Collection<Profile> profiles = job.scheduleAndJoin();
		defineProfileWizardPage = new DefineProfileWizardPage(getTitle(), MESSAGE, duplicateProfile, Profile.convertProfileListToNameStringList(profiles));
		addPage(defineProfileWizardPage);
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

		String name = defineProfileWizardPage.getName();
		String description = defineProfileWizardPage.getDescription();
		final Profile profile = new Profile();
		profile.setName(name);
		if (StringUtils.isNotBlank(description)) {
			profile.setDescription(description);
		}

		if (isDuplicate()) {
			profile.setProfileData(duplicateProfile.getProfileData());
			profile.setActive(duplicateProfile.isActive());
			profile.setDefaultProfile(duplicateProfile.isDefaultProfile());
		} else {
			profile.setProfileData(defineProfileWizardPage.getProfileData());
		}

		ProgressDialog<Profile> dialog = new ProgressDialog<Profile>("Creating new profile..", IProgressMonitor.UNKNOWN) {
			@Override
			public Profile execute(IProgressMonitor monitor) throws BusinessException {
				return cmrRepositoryDefinition.getConfigurationInterfaceService().createProfile(profile);
			}
		};

		dialog.start(true, false);

		if (dialog.wasSuccessful()) {
			Profile created = dialog.getResult();

			// open the created one immediately
			new OpenProfileJob(cmrRepositoryDefinition, created.getId(), workbench.getActiveWorkbenchWindow().getActivePage()).schedule();

			// notify listeners
			InspectIT.getDefault().getInspectITConfigurationInterfaceManager().profileCreated(created, cmrRepositoryDefinition);
			return true;
		} else {
			InspectIT.getDefault().createErrorDialog("Profile can not be created.", dialog.getThrownException(), -1);
			return false;
		}
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
