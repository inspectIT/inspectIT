package rocks.inspectit.ui.rcp.ci.form.editor;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.ci.AgentMapping;
import rocks.inspectit.shared.cs.ci.AgentMappings;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.ci.form.input.AgentMappingInput;
import rocks.inspectit.ui.rcp.ci.form.page.AgentMappingPage;
import rocks.inspectit.ui.rcp.ci.listener.IAgentMappingsChangeListener;
import rocks.inspectit.ui.rcp.ci.listener.IEnvironmentChangeListener;
import rocks.inspectit.ui.rcp.dialog.ProgressDialog;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Editor for the {@link AgentMapping}.
 *
 * @author Ivan Senic
 *
 */
public class AgentMappingEditor extends AbstractConfigurationInterfaceFormEditor implements IAgentMappingsChangeListener, IEnvironmentChangeListener {

	/**
	 * Id of the editor.
	 */
	public static final String ID = "rocks.inspectit.ui.rcp.ci.editor.agentMappingEditor";

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
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().addEnvironmentChangeListener(this);
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
		monitor.beginTask("Saving agent mappings..", IProgressMonitor.UNKNOWN);

		if (!checkValid()) {
			monitor.done();
			return;
		}

		AgentMappingInput profileEditorInput = (AgentMappingInput) getEditorInput();
		final CmrRepositoryDefinition cmrRepositoryDefinition = profileEditorInput.getCmrRepositoryDefinition();
		final AgentMappings mappings = profileEditorInput.getAgentMappings();

		if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			try {
				commitPages(true);

				ProgressDialog<AgentMappings> dialog = new ProgressDialog<AgentMappings>("Saving agent mappings..", IProgressMonitor.UNKNOWN) {
					@Override
					public AgentMappings execute(IProgressMonitor monitor) throws BusinessException {
						return cmrRepositoryDefinition.getConfigurationInterfaceService().saveAgentMappings(mappings);
					}
				};

				dialog.start(true, false);

				if (dialog.wasSuccessful()) {
					AgentMappings updated = dialog.getResult();

					// notify listeners
					if (null != updated) {
						InspectIT.getDefault().getInspectITConfigurationInterfaceManager().agentMappingsUpdated(updated, cmrRepositoryDefinition);
					}

					// set no exception and fire dirty state changed
					setExceptionOnSave(false);
					editorDirtyStateChanged();
				} else {
					setExceptionOnSave(true);
					editorDirtyStateChanged();
					InspectIT.getDefault().createErrorDialog("Saving of the agent mappings failed due to the exception on the CMR.", dialog.getThrownException(), -1);
				}
			} catch (Throwable t) { // NOPMD
				setExceptionOnSave(true);
				editorDirtyStateChanged();
				InspectIT.getDefault().createErrorDialog("Unexpected exception occurred during an attempt to save the agent mappings.", t, -1);
			}
		} else {
			InspectIT.getDefault().createErrorDialog("Saving of the agent mappings failed because CMR is currently not online.", -1);
		}

		monitor.done();
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
	public void environmentCreated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		// ignore
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentUpdated(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		// ignore
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void environmentDeleted(Environment environment, CmrRepositoryDefinition repositoryDefinition) {
		final AgentMappingInput input = (AgentMappingInput) getEditorInput();

		if (!Objects.equals(repositoryDefinition, input.getCmrRepositoryDefinition())) {
			return;
		}

		// check if deleted environment was mapped
		boolean update = false;
		AgentMappings agentMappings = input.getAgentMappings();
		for (AgentMapping mapping : agentMappings.getMappings()) {
			if (Objects.equals(mapping.getEnvironmentId(), environment.getId())) {
				update = true;
				break;
			}
		}

		if (update) {
			AgentMappings updatedAgentMappings = repositoryDefinition.getConfigurationInterfaceService().getAgentMappings();
			Collection<Environment> updatedEnvironments = repositoryDefinition.getConfigurationInterfaceService().getAllEnvironments();

			final AgentMappingInput newInput = new AgentMappingInput(repositoryDefinition, updatedAgentMappings, updatedEnvironments);
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
	public void dispose() {
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeAgentMappingsChangeListener(this);
		InspectIT.getDefault().getInspectITConfigurationInterfaceManager().removeEnvironmentChangeListener(this);
		super.dispose();
	}

}
