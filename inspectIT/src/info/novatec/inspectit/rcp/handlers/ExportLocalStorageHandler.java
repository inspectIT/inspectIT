package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.provider.ILocalStorageDataProvider;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.wizard.ExportStorageWizard;
import info.novatec.inspectit.storage.LocalStorageData;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

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
