package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.ci.form.editor.AgentMappingEditor;
import info.novatec.inspectit.rcp.ci.job.OpenMappingsJob;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
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

}
