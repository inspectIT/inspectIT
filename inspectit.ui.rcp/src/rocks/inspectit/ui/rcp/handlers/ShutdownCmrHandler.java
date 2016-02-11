package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.CmrRepositoryManager.UpdateRepositoryJob;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

/**
 * Handler that performs shutdown and restart of the CMR.
 * 
 * @author Ivan Senic
 * 
 */
public class ShutdownCmrHandler extends AbstractHandler implements IHandler {

	/**
	 * Parameter that defines if restart should be executed after along shutdown of CMR.
	 */
	public static final String SHOULD_RESTART_PARAMETER = "info.novatec.inspectit.rcp.commands.shutdown.shouldRestart";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String param = event.getParameter(SHOULD_RESTART_PARAMETER);
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (StringUtils.isNotEmpty(param) && selection instanceof StructuredSelection) {
			final boolean shouldRestart = Boolean.parseBoolean(param);
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				final CmrRepositoryDefinition cmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedObject).getCmrRepositoryDefinition();
				String cmrName = "'" + cmrRepositoryDefinition.getName() + "' (" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + ")";
				boolean confirm;
				if (shouldRestart) {
					confirm = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Restart CMR", "Are you sure you want to restart the CMR " + cmrName + "?");
				} else {
					confirm = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Shutdown CMR", "Are you sure you want to shutdown the CMR " + cmrName + "?");
				}
				if (confirm) {
					IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
					try {
						progressService.busyCursorWhile(new IRunnableWithProgress() {
							@Override
							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
								if (shouldRestart) {
									monitor.beginTask("Restarting the CMR", IProgressMonitor.UNKNOWN);
									cmrRepositoryDefinition.getCmrManagementService().restart();
								} else {
									monitor.beginTask("Shutting down the CMR", IProgressMonitor.UNKNOWN);
									cmrRepositoryDefinition.getCmrManagementService().shutdown();
								}

								cmrRepositoryDefinition.changeOnlineStatus(OnlineStatus.CHECKING);
								cmrRepositoryDefinition.changeOnlineStatus(OnlineStatus.OFFLINE);

								// we first sleep so that CMR can shutdown
								// this will ensure that this method does not return to fast and CMR
								// is still online
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									Thread.interrupted();
								}

								// then if we are restarting wait until we are up
								if (shouldRestart) {
									monitor.beginTask("Waiting for the CMR to be online again", IProgressMonitor.UNKNOWN);
									while (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.ONLINE) {
										// first let the CMR restart, we can not invoke the service
										// right away cause it can end up in endless service request
										try {
											Thread.sleep(2000);
										} catch (InterruptedException e) {
											Thread.interrupted();
										}

										// we force status update and wait until job has finished
										UpdateRepositoryJob updateRepositoryJob = InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
										updateRepositoryJob.join();

										if (monitor.isCanceled()) {
											return;
										}
									}
								}

								monitor.done();
							}
						});
					} catch (InvocationTargetException | InterruptedException e) {
						throw new ExecutionException("Exception occurred during execution of shutdown/restart handler", e);
					}
				}
			}
		}
		return null;
	}
}
