package info.novatec.inspectit.rcp.ci.job;

import info.novatec.inspectit.cmr.configuration.business.IApplicationDefinition;
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
	 * {@link CmrRepositoryDefinition} the target {@link IApplicationDefinition} is defined in.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Environment id.
	 */
	private IApplicationDefinition application;

	/**
	 * {@link IWorkbenchPage} to open editor in.
	 */
	private IWorkbenchPage page;

	/**
	 * Default constructor.
	 * 
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} the target {@link IApplicationDefinition} is
	 *            defined in.
	 * @param application
	 *            {@link IApplicationDefinition} to open the editor for.
	 * @param page
	 *            {@link IWorkbenchPage} to open editor in.
	 */
	public OpenApplicationDefinitionJob(CmrRepositoryDefinition cmrRepositoryDefinition, IApplicationDefinition application, IWorkbenchPage page) {
		super("Loading environment..");
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.application = application;
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
