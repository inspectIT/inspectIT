package rocks.inspectit.ui.rcp.ci.form.editor;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import rocks.inspectit.ui.rcp.ci.form.page.ApplicationDefinitionPage;
import rocks.inspectit.ui.rcp.ci.form.page.BusinessTransactionPage;
import rocks.inspectit.ui.rcp.ci.listener.IApplicationDefinitionChangeListener;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Editor for the {@link ApplicationDefinition}.
 *
 * @author Alexander Wert
 *
 */
public class ApplicationDefinitionEditor extends AbstractConfigurationInterfaceFormEditor implements IApplicationDefinitionChangeListener {

	/**
	 * Editor ID.
	 */
	public static final String ID = "rocks.inspectit.ui.rcp.ci.editor.applicationDefinitionEditor";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof ApplicationDefinitionEditorInput)) {
			throw new PartInitException("Editor input must be of a type: " + ApplicationDefinitionEditorInput.class.getName());
		}

		setSite(site);
		setInput(input);

		ApplicationDefinitionEditorInput applicationDefinitionEditorInput = (ApplicationDefinitionEditorInput) input;
		setPartName(applicationDefinitionEditorInput.getName());
		setTitleImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BUSINESS_CONTEXT));

		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addApplicationDefinitionChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addPages() {
		try {
			int pageIndex = addPage(new ApplicationDefinitionPage(this));
			setPageImage(pageIndex, InspectIT.getDefault().getImage(InspectITImages.IMG_APPLICATION));
			pageIndex = addPage(new BusinessTransactionPage(this));
			setPageImage(pageIndex, InspectIT.getDefault().getImage(InspectITImages.IMG_BUSINESS_TRANSACTION));
		} catch (PartInitException e) {
			InspectIT.getDefault().log(IStatus.ERROR, "Error occurred trying to open the Application Definition editor.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask("Saving appplication definition..", IProgressMonitor.UNKNOWN);

		if (!checkValid()) {
			monitor.setCanceled(true);
			monitor.done();
			return;
		}

		ApplicationDefinitionEditorInput applicationDefinitionEditorInput = (ApplicationDefinitionEditorInput) getEditorInput();
		final CmrRepositoryDefinition cmrRepositoryDefinition = applicationDefinitionEditorInput.getCmrRepositoryDefinition();
		final ApplicationDefinition applicationDefinition = applicationDefinitionEditorInput.getApplication();

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				commitPages(true);

				ProgressDialog<ApplicationDefinition> dialog = new ProgressDialog<ApplicationDefinition>("Saving appplication definition..", IProgressMonitor.UNKNOWN) {
					@Override
					public ApplicationDefinition execute(IProgressMonitor monitor) throws BusinessException {
						return cmrRepositoryDefinition.getConfigurationInterfaceService().updateApplicationDefinition(applicationDefinition);
					}
				};

				dialog.start(true, false);

				// store changes to CMR
				ApplicationDefinition updatedApplicationDefinition = dialog.getResult();

				// update listeners
				if (updatedApplicationDefinition != null) {
					InspectIT.getDefault().getInspectITConfigurationInterfaceManager().applicationUpdated(updatedApplicationDefinition, cmrRepositoryDefinition);
				}

				// set no exception and fire dirty state changed
				setExceptionOnSave(false);
				editorDirtyStateChanged();
			} catch (BusinessException e) {
				setExceptionOnSave(true);
				editorDirtyStateChanged();
				InspectIT.getDefault().createErrorDialog("Saving of the application definition '" + applicationDefinition.getApplicationName() + "' failed due to the exception on the CMR.", e, -1);
			} catch (Throwable t) { // NOPMD
				setExceptionOnSave(true);
				editorDirtyStateChanged();
				InspectIT.getDefault().createErrorDialog("Unexpected exception occurred during an attempt to save the application '" + applicationDefinition.getApplicationName() + "'.", t, -1);
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Saving of the application '" + applicationDefinition.getApplicationName() + "' failed because CMR is currently not online.", -1);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationCreated(ApplicationDefinition application, int positionIndex, CmrRepositoryDefinition repositoryDefinition) {
		// nothing to do here

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationMoved(ApplicationDefinition application, int oldPositionIndex, int newPositionIndex, CmrRepositoryDefinition repositoryDefinition) {
		applicationUpdated(application, repositoryDefinition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationUpdated(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
		ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		if (Objects.equals(input.getApplication().getId(), application.getId())) {
			final ApplicationDefinitionEditorInput newInput = new ApplicationDefinitionEditorInput(application, repositoryDefinition);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					setPartName(newInput.getName());
					setInputWithNotify(newInput);
				}
			});
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void applicationDeleted(ApplicationDefinition application, CmrRepositoryDefinition repositoryDefinition) {
		ApplicationDefinitionEditorInput input = (ApplicationDefinitionEditorInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		if (Objects.equals(input.getApplication().getId(), application.getId())) {
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
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeApplicationDefinitionChangeListener(this);
		super.dispose();
	}

}
