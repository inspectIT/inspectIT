package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.dialog.EditNameDescriptionDialog;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Edit storage name and description handler.
 * 
 * @author Ivan Senic
 * 
 */
public class EditStorageDataHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStorageDataProvider storageDataProvider = null;
		Object selectedElement = ((StructuredSelection) HandlerUtil.getCurrentSelection(event)).getFirstElement();
		if (selectedElement instanceof IStorageDataProvider) {
			storageDataProvider = (IStorageDataProvider) selectedElement;
		} else {
			return null;
		}

		StorageData storageData = storageDataProvider.getStorageData();
		EditNameDescriptionDialog editStorageDataDialog = new EditNameDescriptionDialog(HandlerUtil.getActiveShell(event), storageData.getName(), storageData.getDescription());
		editStorageDataDialog.open();
		if (editStorageDataDialog.getReturnCode() == EditNameDescriptionDialog.OK) {
			CmrRepositoryDefinition cmrRepositoryDefinition = storageDataProvider.getCmrRepositoryDefinition();
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				try {
					storageData.setName(editStorageDataDialog.getName());
					storageData.setDescription(editStorageDataDialog.getDescription());
					cmrRepositoryDefinition.getStorageService().updateStorageData(storageData);
					try {
						InspectIT.getDefault().getInspectITStorageManager().storageRemotelyUpdated(storageData);
					} catch (SerializationException | IOException e) {
						throw new ExecutionException("Storage data update failed.", e);
					}
				} catch (BusinessException e) {
					throw new ExecutionException("Storage data update failed.", e);
				}
			} else {
				InspectIT.getDefault().createInfoDialog("Storage data can not be updated, because the underlying repository is currently offline.", -1);
			}
		}
		return null;
	}

}
