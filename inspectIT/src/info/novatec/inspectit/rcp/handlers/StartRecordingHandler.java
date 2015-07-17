package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.view.impl.RepositoryManagerView;
import info.novatec.inspectit.rcp.view.impl.StorageManagerView;
import info.novatec.inspectit.rcp.wizard.StartRecordingWizard;
import info.novatec.inspectit.storage.recording.RecordingProperties;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Starts recording.
 * 
 * @author Ivan Senic
 * 
 */
public class StartRecordingHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// try to get the CMR where recording should start.
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		Collection<PlatformIdent> autoSelectedAgents = Collections.emptyList();
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				cmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedObject).getCmrRepositoryDefinition();
			} else if (selectedObject instanceof ICmrRepositoryAndAgentProvider) {
				cmrRepositoryDefinition = ((ICmrRepositoryAndAgentProvider) selectedObject).getCmrRepositoryDefinition();
				autoSelectedAgents = Collections.singletonList(((ICmrRepositoryAndAgentProvider) selectedObject).getPlatformIdent());
			}
		}

		// check if the writing state is OK
		if (null != cmrRepositoryDefinition) {
			try {
				CmrStatusData cmrStatusData = cmrRepositoryDefinition.getCmrManagementService().getCmrStatusData();
				if (cmrStatusData.isWarnSpaceLeftActive()) {
					String leftSpace = NumberFormatter.humanReadableByteCount(cmrStatusData.getStorageDataSpaceLeft());
					if (!MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), "Confirm", "For selected CMR there is an active warning about insufficient storage space left. Only "
							+ leftSpace + " are left on the target server, are you sure you want to continue?")) {
						return null;
					}
				}
			} catch (Exception e) { // NOPMD NOCHK
				// ignore because if we can not get the info. we will still respond to user action
			}
		}

		// open wizard
		StartRecordingWizard startRecordingWizard = new StartRecordingWizard(cmrRepositoryDefinition, autoSelectedAgents);
		WizardDialog wizardDialog = new WizardDialog(HandlerUtil.getActiveShell(event), startRecordingWizard);
		wizardDialog.open();

		// if recording has been started refresh the repository and storage manager view
		if (wizardDialog.getReturnCode() == WizardDialog.OK) {
			final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IViewPart repositoryManagerView = activePage.findView(RepositoryManagerView.VIEW_ID);
			if (repositoryManagerView instanceof RepositoryManagerView) {
				((RepositoryManagerView) repositoryManagerView).refresh();
			}
			IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
			if (storageManagerView instanceof StorageManagerView) {
				if (null != cmrRepositoryDefinition) {
					((StorageManagerView) storageManagerView).refresh(cmrRepositoryDefinition);
				} else {
					((StorageManagerView) storageManagerView).refresh();
				}
			}

			// auto-refresh on recording stop if there is recording duration specified
			RecordingProperties recordingProperties = startRecordingWizard.getRecordingProperties();
			if (null != recordingProperties && recordingProperties.getRecordDuration() > 0) {
				Job refreshStorageManagerJob = new Job("Recording Auto-Stop Updates") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						IViewPart storageManagerView = activePage.findView(StorageManagerView.VIEW_ID);
						if (storageManagerView instanceof StorageManagerView) {
							((StorageManagerView) storageManagerView).refresh();
						}
						return Status.OK_STATUS;
					}
				};
				refreshStorageManagerJob.setUser(false);
				refreshStorageManagerJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImage(InspectITImages.IMG_RECORD_STOP));
				// add 5 seconds to be sure all is done
				long delay = 5000 + recordingProperties.getRecordDuration() + recordingProperties.getStartDelay();
				refreshStorageManagerJob.schedule(delay);
			}
		}
		return null;
	}

}
