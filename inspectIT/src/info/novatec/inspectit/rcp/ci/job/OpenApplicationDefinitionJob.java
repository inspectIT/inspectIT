package info.novatec.inspectit.rcp.ci.job;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.form.editor.ApplicationDefinitionEditor;
import info.novatec.inspectit.rcp.ci.form.input.ApplicationDefinitionEditorInput;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

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
	 * Environment id.
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
		super("Loading environment..");
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
			application = cmrRepositoryDefinition.getBusinessContextMangementService().getApplicationDefinition(applicationId);
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
