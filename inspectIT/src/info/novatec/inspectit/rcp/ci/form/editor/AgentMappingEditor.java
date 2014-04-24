package info.novatec.inspectit.rcp.ci.form.editor;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.form.input.AgentMappingInput;
import info.novatec.inspectit.rcp.ci.form.page.AgentMappingPage;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

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
 * Editor for the {@link AgentMapping}.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentMappingEditor extends FormEditor {

	/**
	 * Id of the editor.
	 */
	public static final String ID = "info.novatec.inspectit.rcp.ci.editor.agentMappingEditor";;

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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addPages() {
		try {
			addPage(new AgentMappingPage(this));
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

		final AgentMappingInput profileEditorInput = (AgentMappingInput) getEditorInput();

		Job saveMappingsJob = new Job("Saving agent mappings..") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final CmrRepositoryDefinition cmrRepositoryDefinition = profileEditorInput.getCmrRepositoryDefinition();
				final AgentMappings mappings = profileEditorInput.getAgentMappings();

				if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					try {
						cmrRepositoryDefinition.getConfigurationInterfaceService().setAgentMappings(mappings);
						// we need to manually update the revision here
						// yes it's bad, but save can occur many times while editor is open
						mappings.setRevision(mappings.getRevision() + 1);

					} catch (final Exception e) {
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								InspectIT.getDefault().createErrorDialog("Updating of the agent mappings failed due to the exception on the CMR.", e, -1);
							}
						});
					}
				} else {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							InspectIT.getDefault().createErrorDialog("Updating of the agent mappings failed because CMR is not currently online.", -1);
						}
					});
				}

				return Status.OK_STATUS;
			}
		};
		saveMappingsJob.setUser(true);
		saveMappingsJob.schedule();
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

}
