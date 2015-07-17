package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.ILocalStorageDataProvider;
import info.novatec.inspectit.storage.LocalStorageData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for deleting the local storage.
 * 
 * @author Ivan Senic
 * 
 */
public class DeleteLocalStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection structuredSelection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		List<LocalStorageData> localStoragesToDelete = new ArrayList<LocalStorageData>();
		for (Iterator<?> it = structuredSelection.iterator(); it.hasNext();) {
			LocalStorageData localStorageData = ((ILocalStorageDataProvider) it.next()).getLocalStorageData();
			if (localStorageData.isFullyDownloaded()) {
				localStoragesToDelete.add(localStorageData);
			}
		}

		if (!localStoragesToDelete.isEmpty()) {
			StringBuffer confirmText = new StringBuffer(100);
			boolean plural = localStoragesToDelete.size() > 1;
			if (!plural) {
				confirmText.append("Are you sure you want to delete the locally downloaded data for the selected storage? ");
			} else {
				confirmText.append("Are you sure you want to  delete the locally downloaded data for the " + localStoragesToDelete.size() + " selected storages? ");
			}

			MessageBox confirmDelete = new MessageBox(HandlerUtil.getActiveShell(event), SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
			confirmDelete.setText("Confirm Delete");
			confirmDelete.setMessage(confirmText.toString());

			if (SWT.OK == confirmDelete.open()) {
				for (LocalStorageData localStorageData : localStoragesToDelete) {
					try {
						InspectIT.getDefault().getInspectITStorageManager().deleteLocalStorageData(localStorageData);
					} catch (Exception e) {
						InspectIT.getDefault().createErrorDialog("There was an exception trying to delete local storage data.", e, -1);
						return null;
					}
				}

			}
		}

		return null;
	}

}
