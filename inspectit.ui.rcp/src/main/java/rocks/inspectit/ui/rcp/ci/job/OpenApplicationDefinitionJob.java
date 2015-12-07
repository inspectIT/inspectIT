package rocks.inspectit.ui.rcp.ci.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.form.editor.ApplicationDefinitionEditor;
import rocks.inspectit.ui.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * This Job opens the {@link ApplicationDefinitionEditor} for the selected application definition.
 *
 * @author Alexander Wert
 *
 */
public class OpenApplicationDefinitionJob extends Job {
	/**
	 * {@link CmrRepositoryDefinition} the target {@link ApplicationDefinition} is defined in.
	 */
	private final CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Application id.
	 */
	private final int applicationId;

	/**
	 * {@link IWorkbenchPage} to open editor in.
	 */
	private final IWorkbenchPage page;

	/**
	 * Default constructor.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} the target {@link ApplicationDefinition} is
	 *            defined in.
	 * @param applicationId
	 *            the id of the {@link ApplicationDefinition} to open the editor for.
	 * @param page
	 *            {@link IWorkbenchPage} to open editor in.
	 */
	public OpenApplicationDefinitionJob(CmrRepositoryDefinition cmrRepositoryDefinition, int applicationId, IWorkbenchPage page) {
		super("Loading application..");
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.applicationId = applicationId;
		this.page = page;
		setUser(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
			return Status.CANCEL_STATUS;
		}
		ApplicationDefinition application;
		try {
			application = cmrRepositoryDefinition.getConfigurationInterfaceService().getApplicationDefinition(applicationId);
		} catch (BusinessException e) {
			return new Status(IStatus.ERROR, InspectIT.ID, "Exception occurred loading the application from the CMR.", e);
		}

		final ApplicationDefinitionEditorInput applicationDefinitionEditorInput = new ApplicationDefinitionEditorInput(application, cmrRepositoryDefinition);

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					page.openEditor(applicationDefinitionEditorInput, ApplicationDefinitionEditor.ID, true);
				} catch (PartInitException e) {
					InspectIT.getDefault().createErrorDialog("Exception occurred opening the Application Definition editor.", e, -1);
				}
			}
		});
		return Status.OK_STATUS;

	}
}
