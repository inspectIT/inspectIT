package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.ci.form.editor.AgentMappingEditor;
import info.novatec.inspectit.rcp.ci.form.input.AgentMappingInput;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler that opens {@link AgentMappingEditor}.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentMappingHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = window.getActivePage();

		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object selected = selection.getFirstElement();
		if (selected instanceof ICmrRepositoryProvider) {
			CmrRepositoryDefinition repositoryDefinition = ((ICmrRepositoryProvider) selected).getCmrRepositoryDefinition();
			new OpenMappingsJob(repositoryDefinition, page).schedule();
		}

		return null;
	}

	/**
	 * Job for loading of the {@link AgentMapping}s from the CMR and opening the editor.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public static class OpenMappingsJob extends Job {

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
			} catch (final Exception e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						InspectIT.getDefault().createErrorDialog("Exception occurred loading the data from the CMR.", e, -1);
					}
				});
				return Status.CANCEL_STATUS;
			}
		}

	}

}
