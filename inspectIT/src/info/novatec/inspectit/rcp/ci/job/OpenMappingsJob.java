package info.novatec.inspectit.rcp.ci.job;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.form.editor.AgentMappingEditor;
import info.novatec.inspectit.rcp.ci.form.input.AgentMappingInput;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Job for loading of the {@link AgentMapping}s from the CMR and opening the editor.
 * 
 * @author Ivan Senic
 * 
 */
public class OpenMappingsJob extends Job {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Active page.
	 */
	private IWorkbenchPage activePage;

	/**
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}
	 * @param activePage
	 *            active page from the handler
	 */
	public OpenMappingsJob(CmrRepositoryDefinition cmrRepositoryDefinition, IWorkbenchPage activePage) {
		super("Loading mappings..");
		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.activePage = activePage;
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

		try {
			AgentMappings mappings = cmrRepositoryDefinition.getConfigurationInterfaceService().getAgentMappings();
			Collection<Environment> environments = cmrRepositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();
			final AgentMappingInput input = new AgentMappingInput(cmrRepositoryDefinition, mappings, environments);
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						activePage.openEditor(input, AgentMappingEditor.ID, true);
					} catch (PartInitException e) {
						InspectIT.getDefault().createErrorDialog("Exception occurred opening the Agent mapping settings editor.", e, -1);
					}
				}
			});
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, InspectIT.ID, "Exception occurred loading the data from the CMR.", e);
		}
	}

}