package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.view.impl.DataExplorerView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Opens the {@link RepositoryDefinition} in the {@link DataExplorerView}.
 * 
 * @author Ivan Senic
 * 
 */
public class ShowRepositoryHandler extends AbstractHandler implements IHandler {

	/**
	 * The corresponding command id.
	 */
	public static final String COMMAND = "info.novatec.inspectit.rcp.commands.showRepository";

	/**
	 * The repository to look up.
	 */
	public static final String REPOSITORY_DEFINITION = COMMAND + ".repository";

	/**
	 * The repository to look up.
	 */
	public static final String AGENT = COMMAND + ".agent";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the repository definition and agent out of the context
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		RepositoryDefinition repositoryDefinition = (RepositoryDefinition) context.getVariable(REPOSITORY_DEFINITION);
		PlatformIdent platformIdent = (PlatformIdent) context.getVariable(AGENT);

		if (null != repositoryDefinition) {
			// find view
			IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
			if (null == workbenchWindow) {
				workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			}

			IViewPart viewPart = workbenchWindow.getActivePage().findView(DataExplorerView.VIEW_ID);
			if (viewPart == null) {
				try {
					viewPart = workbenchWindow.getActivePage().showView(DataExplorerView.VIEW_ID);
				} catch (PartInitException e) {
					return null;
				}
			}
			if (viewPart instanceof DataExplorerView) {
				workbenchWindow.getActivePage().activate(viewPart);
				((DataExplorerView) viewPart).showRepository(repositoryDefinition, platformIdent);
			}
		}
		return null;
	}

}
