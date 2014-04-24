package info.novatec.inspectit.rcp.ci.form.editor;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.form.input.EnvironmentEditorInput;
import info.novatec.inspectit.rcp.ci.form.page.EnvironmentSettingsPage;
import info.novatec.inspectit.rcp.ci.listener.IEnvironmentChangeListener;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * Editor for the {@link Environment}.
 * 
 * @author Ivan Senic
 * 
 */
public class EnvironmentEditor extends AbstractConfigurationInterfaceFormEditor implements IEnvironmentChangeListener {

	/**
	 * Editor ID.
	 */
	public static final String ID = "info.novatec.inspectit.rcp.ci.editor.environmentEditor";

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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addPages() {
		try {
			addPage(new EnvironmentSettingsPage(this));
		} catch (PartInitException e) {
			InspectIT.getDefault().log(IStatus.ERROR, "Error occurred trying to open the Environment editor.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!checkValid()) {
			return;
		}

		commitPages(true);

		final EnvironmentEditorInput environmentEditorInput = (EnvironmentEditorInput) getEditorInput();

		Job saveProfileJob = new Job("Saving environment..") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				CmrRepositoryDefinition cmrRepositoryDefinition = environmentEditorInput.getCmrRepositoryDefinition();
				Environment environment = environmentEditorInput.getEnvironment();

				if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					try {
						Environment updated = cmrRepositoryDefinition.getConfigurationInterfaceService().updateEnvironment(environment);

						// notify listeners
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentUpdated(updated, cmrRepositoryDefinition);
					} catch (BusinessException e) {
						return new Status(IStatus.ERROR, InspectIT.ID, "Updating of the environment '" + environment.getName() + "' failed due to the exception on the CMR.", e);
					}
				} else {
					return new Status(IStatus.ERROR, InspectIT.ID, "Updating of the environment '" + environment.getName() + "' failed because CMR is currently not online.");
				}

				return Status.OK_STATUS;
			}
		};
		saveProfileJob.setUser(true);
		saveProfileJob.schedule();
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
	public void dispose() {
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
		super.dispose();
	}
}
