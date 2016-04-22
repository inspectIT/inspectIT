package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.provider.IStorageDataProvider;
import rocks.inspectit.ui.rcp.view.impl.StorageManagerView;
import rocks.inspectit.ui.rcp.wizard.AddStorageLabelWizard;

/**
 * Handler for adding a label to storage.
 *
 * @author Ivan Senic
 *
 */
public class AddStorageLabelHandler extends AbstractHandler implements IHandler {

	/**
	 * The corresponding command id.
	 */
	public static final String COMMAND = "rocks.inspectit.ui.rcp.commands.addStorageLabel";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStorageDataProvider storageProvider = null;

		// try to get it from selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof IStorageDataProvider) {
				storageProvider = (IStorageDataProvider) ((StructuredSelection) selection).getFirstElement();
			}
		}

		if (null != storageProvider) {
			AddStorageLabelWizard addStorageLabelWizard = new AddStorageLabelWizard(storageProvider);
			WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), addStorageLabelWizard);
			wizardDialog.open();
			if (wizardDialog.getReturnCode() == Window.OK) {
				IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
				if (viewPart instanceof StorageManagerView) {
					((StorageManagerView) viewPart).refresh(storageProvider.getCmrRepositoryDefinition());
				}
			}
		}

		return null;
	}

}
