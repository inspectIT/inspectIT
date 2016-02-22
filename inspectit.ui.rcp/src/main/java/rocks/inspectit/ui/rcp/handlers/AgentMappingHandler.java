package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.ci.form.editor.AgentMappingEditor;
import rocks.inspectit.ui.rcp.ci.job.OpenMappingsJob;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

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
