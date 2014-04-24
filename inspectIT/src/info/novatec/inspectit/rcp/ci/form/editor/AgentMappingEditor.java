package info.novatec.inspectit.rcp.ci.form.editor;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.form.input.AgentMappingInput;
import info.novatec.inspectit.rcp.ci.form.page.AgentMappingPage;
import info.novatec.inspectit.rcp.ci.listener.IAgentMappingsChangeListener;
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
 * Editor for the {@link AgentMapping}.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentMappingEditor extends AbstractConfigurationInterfaceFormEditor implements IAgentMappingsChangeListener {

	/**
	 * Id of the editor.
	 */
	public static final String ID = "info.novatec.inspectit.rcp.ci.editor.agentMappingEditor";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (!(input instanceof AgentMappingInput)) {
			throw new PartInitException("Editor input must be of a type: " + AgentMappingInput.class.getName());
		}

		setSite(site);
		setInput(input);

		AgentMappingInput agentMappingEditorInput = (AgentMappingInput) input;
		setPartName(agentMappingEditorInput.getName());

		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addAgentMappingsChangeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addPages() {
		try {
			addPage(new AgentMappingPage(this));
		} catch (PartInitException e) {
			InspectIT.getDefault().log(IStatus.ERROR, "Error occurred trying to open the Agent Mappings editor.", e);
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

		final AgentMappingInput profileEditorInput = (AgentMappingInput) getEditorInput();

		Job saveMappingsJob = new Job("Saving agent mappings..") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				CmrRepositoryDefinition cmrRepositoryDefinition = profileEditorInput.getCmrRepositoryDefinition();
				AgentMappings mappings = profileEditorInput.getAgentMappings();

				if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					try {
						AgentMappings updated = cmrRepositoryDefinition.getConfigurationInterfaceService().saveAgentMappings(mappings);

						// notify listeners
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().agentMappingsUpdated(updated, cmrRepositoryDefinition);
					} catch (BusinessException e) {
						return new Status(IStatus.ERROR, InspectIT.ID, "Updating of the agent mappings failed due to the exception on the CMR.", e);
					}
				} else {
					return new Status(IStatus.ERROR, InspectIT.ID, "Updating of the agent mappings failed because CMR is currently not online.");
				}

				return Status.OK_STATUS;
			}
		};
		saveMappingsJob.setUser(true);
		saveMappingsJob.schedule();
	}

	@Override
	public void agentMappingsUpdated(AgentMappings agentMappings, CmrRepositoryDefinition repositoryDefinition) {
		AgentMappingInput input = (AgentMappingInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		final AgentMappingInput newInput = new AgentMappingInput(input.getCmrRepositoryDefinition(), agentMappings, input.getEnvironments());
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				setPartName(newInput.getName());
				setInputWithNotify(newInput);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeAgentMappingsChangeListener(this);
		super.dispose();
	}
}
