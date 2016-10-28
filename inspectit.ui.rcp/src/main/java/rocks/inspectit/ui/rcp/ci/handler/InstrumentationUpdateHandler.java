package rocks.inspectit.ui.rcp.ci.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.InstrumentationStatus;
import rocks.inspectit.ui.rcp.ci.dialog.InstrumentationUpdateDialog;
import rocks.inspectit.ui.rcp.ci.dialog.InstrumentationUpdateDialog.OnSaveBehavior;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryAndAgentProvider;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.util.SafeExecutor;

/**
 * Handles the opening of the {@link InstrumentationUpdateDialog} and its result.
 *
 * @author Marius Oehler
 *
 */
public class InstrumentationUpdateHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// try to get the CMR where recording should start.
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		PlatformIdent platformIdent = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				cmrRepositoryDefinition = ((ICmrRepositoryProvider) selectedObject).getCmrRepositoryDefinition();
			} else if (selectedObject instanceof ICmrRepositoryAndAgentProvider) {
				ICmrRepositoryAndAgentProvider provider = ((ICmrRepositoryAndAgentProvider) selectedObject);
				cmrRepositoryDefinition = provider.getCmrRepositoryDefinition();
				platformIdent = provider.getPlatformIdent();
			}
		}

		execute(cmrRepositoryDefinition, platformIdent, true, null);

		return null;
	}

	/**
	 * Opens the {@link InstrumentationUpdateDialog} if its {@link OnSaveBehavior} is
	 * {@link OnSaveBehavior#SHOW_DIALOG}.
	 *
	 * @param cmrRepositoryDefinition
	 *            the {@link CmrRepositoryDefinition} to use
	 * @param closeButtonLabel
	 *            the text on the closing button
	 */
	public static void execute(CmrRepositoryDefinition cmrRepositoryDefinition, String closeButtonLabel) {
		execute(cmrRepositoryDefinition, null, false, closeButtonLabel);
	}

	/**
	 * Opens the {@link InstrumentationUpdateDialog}.
	 *
	 * @param repositoryDefinition
	 *            the {@link CmrRepositoryDefinition} to use
	 * @param platformIdent
	 *            the agent which has been selected
	 * @param forceDialog
	 *            whether the set {@link OnSaveBehavior} should be ignored in order to force the
	 *            opening
	 * @param closeButtonLabel
	 *            the text on the closing button
	 */
	private static void execute(final CmrRepositoryDefinition repositoryDefinition, final PlatformIdent platformIdent, final boolean forceDialog, final String closeButtonLabel) {
		SafeExecutor.syncExec(new Runnable() {
			@Override
			public void run() {
				OnSaveBehavior saveBehavior = PreferencesUtils.getObject(PreferencesConstants.INSTRUMENTATION_UPDATED_AUTO_ACTION);
				if (forceDialog || (saveBehavior != OnSaveBehavior.DO_NOTHING)) {
					Map<PlatformIdent, AgentStatusData> agentMap = getPendingAgents(repositoryDefinition);

					Collection<PlatformIdent> updateAgents = new ArrayList<>();

					if (forceDialog || (saveBehavior == OnSaveBehavior.SHOW_DIALOG)) {
						if (forceDialog || ((agentMap != null) && !agentMap.isEmpty())) {
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							if (window == null) {
								if (PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0) {
									window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
								} else {
									return;
								}
							}
							Shell shell = window.getShell();
							InstrumentationUpdateDialog updateDialog = new InstrumentationUpdateDialog(shell, agentMap, platformIdent, closeButtonLabel);
							int result = updateDialog.open();
							if (result == Dialog.OK) {
								updateAgents.addAll(updateDialog.getUpdateAgents());
							}
						}
					} else if (saveBehavior == OnSaveBehavior.UPDATE_ALL_AGENTS) {
						updateAgents.addAll(agentMap.keySet());
					}

					if (CollectionUtils.isNotEmpty(updateAgents)) {
						List<Long> updateIds = new ArrayList<>();
						for (PlatformIdent platformIdent : updateAgents) {
							updateIds.add(platformIdent.getId());
						}

						repositoryDefinition.getAgentInstrumentationService().updateInstrumentation(updateIds);
					}
				}
			}
		});
	}

	/**
	 * Fetches the existing agents from the CMR and extracts the ones which instrumentation status
	 * is pending.
	 *
	 * @param repositoryDefinition
	 *            the {@link CmrRepositoryDefinition} to use
	 * @return {@link Map} of agents having pending an instrumentation
	 */
	private static Map<PlatformIdent, AgentStatusData> getPendingAgents(CmrRepositoryDefinition repositoryDefinition) {
		Map<PlatformIdent, AgentStatusData> agentsOverview = repositoryDefinition.getGlobalDataAccessService().getAgentsOverview();

		Map<PlatformIdent, AgentStatusData> resultMap = new HashMap<>();
		for (Entry<PlatformIdent, AgentStatusData> entry : agentsOverview.entrySet()) {
			if (entry.getValue().getInstrumentationStatus() == InstrumentationStatus.PENDING) {
				resultMap.put(entry.getKey(), entry.getValue());
			}
		}

		return resultMap;
	}
}
