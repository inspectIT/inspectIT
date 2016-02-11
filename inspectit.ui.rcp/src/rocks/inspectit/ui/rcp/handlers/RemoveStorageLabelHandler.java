package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for removing a list of labels from storage.
 * 
 * @author Ivan Senic
 * 
 */
public class RemoveStorageLabelHandler extends AbstractHandler implements IHandler {

	/**
	 * Command ID.
	 */
	public static final String COMMAND = "info.novatec.inspectit.rcp.commands.removeStorageLabel";

	/**
	 * Input ID.
	 */
	public static final String INPUT = COMMAND + ".input";

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the input list out of the context
		IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
		List<AbstractStorageLabel<?>> inputList = (List<AbstractStorageLabel<?>>) context.getVariable(INPUT);

		IStorageDataProvider storageProvider = null;

		// try to get it from selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			if (((StructuredSelection) selection).getFirstElement() instanceof IStorageDataProvider) {
				storageProvider = (IStorageDataProvider) ((StructuredSelection) selection).getFirstElement();
			}
		}

		if (null != storageProvider && null != inputList) {
			CmrRepositoryDefinition cmrRepositoryDefinition = storageProvider.getCmrRepositoryDefinition();
			if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
				try {
					StorageData updatedStorageData = cmrRepositoryDefinition.getStorageService().removeLabelsFromStorage(storageProvider.getStorageData(), inputList);
					try {
						InspectIT.getDefault().getInspectITStorageManager().storageRemotelyUpdated(updatedStorageData);
					} catch (SerializationException | IOException e) {
						throw new ExecutionException("Error occured trying to save local storage data to disk.", e);
					}
					IViewPart viewPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StorageManagerView.VIEW_ID);
					if (viewPart instanceof StorageManagerView) {
						((StorageManagerView) viewPart).refresh(cmrRepositoryDefinition);
					}
				} catch (BusinessException e) {
					throw new ExecutionException("Error occured trying to remove labels from storage.", e);
				}
			} else {
				throw new ExecutionException("Labels could not be removed from storage, because the underlying repository is offline.");
			}
		}

		return null;
	}
}
