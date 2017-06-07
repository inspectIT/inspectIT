package rocks.inspectit.ui.rcp.ci.form.editor;

import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;
import rocks.inspectit.ui.rcp.ci.form.page.EnvironmentSettingsPage;
import rocks.inspectit.ui.rcp.ci.listener.IEnvironmentChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IProfileChangeListener;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Editor for the {@link Environment}.
 *
 * @author Ivan Senic
 *
 */
public class EnvironmentEditor extends AbstractConfigurationInterfaceFormEditor implements IEnvironmentChangeListener, IProfileChangeListener {

	/**
	 * Editor ID.
	 */
	public static final String ID = "rocks.inspectit.ui.rcp.ci.editor.environmentEditor";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof EnvironmentEditorInput)) {
			throw new PartInitException("Editor input must be of a type: " + EnvironmentEditorInput.class.getName());
		}

		setSite(site);
		setInput(input);

		EnvironmentEditorInput environmentEditorInput = (EnvironmentEditorInput) input;
		setPartName(environmentEditorInput.getName());
		setTitleImage(ImageFormatter.getEnvironmentImage(environmentEditorInput.getEnvironment()));

		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addEnvironmentChangeListener(this);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addProfileChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addPages() {
		try {
			addPage(new EnvironmentSettingsPage(this));
			setPageImage(0, InspectIT.getDefault().getImage(InspectITImages.IMG_TOOL));
		} catch (PartInitException e) {
			InspectIT.getDefault().log(IStatus.ERROR, "Error occurred trying to open the Environment editor.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask("Saving environment..", IProgressMonitor.UNKNOWN);

		if (!checkValid()) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}

		EnvironmentEditorInput environmentEditorInput = (EnvironmentEditorInput) getEditorInput();
		final CmrRepositoryDefinition cmrRepositoryDefinition = environmentEditorInput.getCmrRepositoryDefinition();
		final Environment environment = environmentEditorInput.getEnvironment();

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				commitPages(true);

				ProgressDialog<Environment> progressDialog = new ProgressDialog<Environment>("Saving environment..", IProgressMonitor.UNKNOWN) {
					@Override
					public Environment execute(IProgressMonitor monitor) throws BusinessException {
						return cmrRepositoryDefinition.getConfigurationInterfaceService().updateEnvironment(environment);
					}
				};
				progressDialog.start(true, false);

				Environment updated = progressDialog.getResult();

				// notify listeners
				if (null != updated) {
					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentUpdated(updated, cmrRepositoryDefinition);
				}

				// set no exception and fire dirty state changed
				setExceptionOnSave(false);
				editorDirtyStateChanged();
			} catch (BusinessException e) {
				monitor.setCanceled(true);
				setExceptionOnSave(true);
				editorDirtyStateChanged();
				InspectIT.getDefault().createErrorDialog("Saving of the environment '" + environment.getName() + "' failed due to the exception on the CMR.", e, -1);
			} catch (Throwable t) { // NOPMD
				monitor.setCanceled(true);
				setExceptionOnSave(true);
				editorDirtyStateChanged();
				InspectIT.getDefault().createErrorDialog("Unexpected exception occurred during an attempt to save the environment '" + environment.getName() + "'.", t, -1);
			}
		} else {
			monitor.setCanceled(true);
			InspectIT.getDefault().createErrorDialog("Saving of the environment '" + environment.getName() + "' failed because CMR is currently not online.", -1);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentCreated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		// not interesting
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentUpdated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		EnvironmentEditorInput input = (EnvironmentEditorInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		if (Objects.equals(input.getEnvironment().getId(), environment.getId())) {
			final EnvironmentEditorInput newInput = new EnvironmentEditorInput(environment, input.getProfiles(), input.getCmrRepositoryDefinition());
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					setPartName(newInput.getName());
					setTitleImage(ImageFormatter.getEnvironmentImage(newInput.getEnvironment()));
					setInputWithNotify(newInput);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentDeleted(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		EnvironmentEditorInput input = (EnvironmentEditorInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		if (Objects.equals(input.getEnvironment().getId(), environment.getId())) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					close(false);
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileCreated(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		// ignore
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileUpdated(Profile profile, CmrRepositoryDefinition repositoryDefinition, boolean onlyProperties) {
		// ignore
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void profileDeleted(Profile profile, CmrRepositoryDefinition repositoryDefinition) {
		EnvironmentEditorInput input = (EnvironmentEditorInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		// check if deleted profile was used
		boolean update = false;
		for (String profileId : input.getEnvironment().getProfileIds()) {
			if (Objects.equals(profileId, profile.getId())) {
				update = true;
				break;
			}
		}

		if (update) {
			try {
				Environment updatedEnvironment = repositoryDefinition.getConfigurationInterfaceService().getEnvironment(input.getEnvironment().getId());
				List<Profile> updatedProfiles = repositoryDefinition.getConfigurationInterfaceService().getAllProfiles();

				final EnvironmentEditorInput newInput = new EnvironmentEditorInput(updatedEnvironment, updatedProfiles, repositoryDefinition);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						setPartName(newInput.getName());
						setTitleImage(ImageFormatter.getEnvironmentImage(newInput.getEnvironment()));
						setInputWithNotify(newInput);
					}
				});
			} catch (BusinessException e) {
				InspectIT.getDefault().log(IStatus.WARNING, "Error updating the Environment editor on profile update", e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeProfileChangeListener(this);
		super.dispose();
	}

}
