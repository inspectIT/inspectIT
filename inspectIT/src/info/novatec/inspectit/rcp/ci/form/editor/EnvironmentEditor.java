package info.novatec.inspectit.rcp.ci.form.editor;

import info.novatec.inspectit.ci.Environment;
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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Editor for the {@link Environment}.
 * 
 * @author Ivan Senic
 * 
 */
public class EnvironmentEditor extends FormEditor implements IEnvironmentChangeListener {

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

		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
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
	 * <p>
	 * A small fix so that the tabs are not displayed if only one page is existing in the editor.
	 */
	@Override
	protected void createPages() {
		super.createPages();
		if (getPageCount() == 1 && getContainer() instanceof CTabFolder) {
			((CTabFolder) getContainer()).setTabHeight(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		commitPages(true);

		final EnvironmentEditorInput environmentEditorInput = (EnvironmentEditorInput) getEditorInput();

		Job saveProfileJob = new Job("Saving profile..") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final CmrRepositoryDefinition cmrRepositoryDefinition = environmentEditorInput.getCmrRepositoryDefinition();
				final Environment environment = environmentEditorInput.getEnvironment();

				if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					try {
						Environment updated = cmrRepositoryDefinition.getConfigurationInterfaceService().updateEnvironment(environment);
						
						// notify listeners
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().environmentEdited(updated);
					} catch (final Exception e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Updating of the environment '" + environment.getName() + "' failed due to the exception on the CMR.", e, -1);
							}
						});
					}
				} else {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							InspectIT.getDefault().createErrorDialog("Updating of the environment '" + environment.getName() + "' failed because CMR is not currently online.", -1);
						}
					});
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
	public void doSaveAs() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		getActivePageInstance().setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentAdded(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		// not interesting
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentEdited(Environment environment) {
		EnvironmentEditorInput input = (EnvironmentEditorInput) getEditorInput();
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
	public void environmentDeleted(Environment environment) {
		EnvironmentEditorInput input = (EnvironmentEditorInput) getEditorInput();
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
