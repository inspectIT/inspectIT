package rocks.inspectit.ui.rcp.ci.form.editor;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.EnvironmentEditorInput;
import rocks.inspectit.ui.rcp.ci.form.page.EnvironmentSettingsPage;
import rocks.inspectit.ui.rcp.ci.listener.IEnvironmentChangeListener;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

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
		CmrRepositoryDefinition cmrRepositoryDefinition = environmentEditorInput.getCmrRepositoryDefinition();
		Environment environment = environmentEditorInput.getEnvironment();

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				commitPages(true);

				Environment updated = cmrRepositoryDefinition.getConfigurationInterfaceService().updateEnvironment(environment);

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
	public void dispose() {
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
		super.dispose();
	}
}
