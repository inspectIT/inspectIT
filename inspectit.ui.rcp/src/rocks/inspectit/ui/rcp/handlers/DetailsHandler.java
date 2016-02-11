package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.dialog.DetailsDialog;
import info.novatec.inspectit.rcp.provider.IInputDefinitionProvider;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for displaying new details window.
 * 
 * @author Ivan Senic
 * 
 */
public class DetailsHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);

		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object selected = selection.getFirstElement();

		RepositoryDefinition repositoryDefinition = null;
		IWorkbenchPart editor = HandlerUtil.getActivePart(event);
		if (editor instanceof IInputDefinitionProvider) {
			IInputDefinitionProvider inputDefinitionProvider = (IInputDefinitionProvider) editor;
			repositoryDefinition = inputDefinitionProvider.getInputDefinition().getRepositoryDefinition();
		}

		if (selected instanceof DefaultData && null != repositoryDefinition) {
			DetailsDialog detailsDialog = new DetailsDialog(shell, (DefaultData) selected, repositoryDefinition);
			detailsDialog.open();

			final Command commandOnClose = detailsDialog.getCommandOnClose();
			if (null != commandOnClose) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							commandOnClose.executeWithChecks(event);
						} catch (Exception e) { // NOPMD NOCHK
							InspectIT.getDefault().createErrorDialog("Error occurred executing the command in the details dialog.", e, -1);
						}
					}
				});

			}
		}

		return null;
	}

}
