package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.ui.rcp.provider.ILocalStorageDataProvider;
import rocks.inspectit.ui.rcp.provider.IStorageDataProvider;
import rocks.inspectit.ui.rcp.wizard.ExportStorageWizard;

/**
 * Handler for exporting the local storage.
 *
 * @author Ivan Senic
 *
 */
public class ExportLocalStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection structuredSelection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		Object selected = structuredSelection.getFirstElement();
		if (selected instanceof ILocalStorageDataProvider) {
			LocalStorageData localStorageData = ((ILocalStorageDataProvider) selected).getLocalStorageData();
			new WizardDialog(HandlerUtil.getActiveShell(event), new ExportStorageWizard(localStorageData)).open();
		} else if (selected instanceof IStorageDataProvider) {
			IStorageDataProvider storageDataProvider = (IStorageDataProvider) selected;
			new WizardDialog(HandlerUtil.getActiveShell(event), new ExportStorageWizard(storageDataProvider.getStorageData(), storageDataProvider.getCmrRepositoryDefinition())).open();
		}
		return null;
	}
}
