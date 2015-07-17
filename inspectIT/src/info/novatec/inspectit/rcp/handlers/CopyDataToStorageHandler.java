package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.wizard.CopyDataToStorageWizard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for copying data to storage.
 * 
 * @author Ivan Senic
 * 
 */
public class CopyDataToStorageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);

		Set<DefaultData> copyDataSet = new HashSet<DefaultData>(selection.size());
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object nextObject = it.next();

			if (nextObject instanceof InvocationSequenceData) {
				InvocationSequenceData invocationSequenceData = (InvocationSequenceData) nextObject;
				while (null != invocationSequenceData.getParentSequence()) {
					invocationSequenceData = invocationSequenceData.getParentSequence();
				}
				copyDataSet.add(invocationSequenceData);
			} else if (nextObject instanceof DefaultData) {
				copyDataSet.add((DefaultData) nextObject);
			}
		}

		if (!copyDataSet.isEmpty() && repositoryDefinition instanceof CmrRepositoryDefinition) {
			CmrRepositoryDefinition cmrRepositoryDefinition = (CmrRepositoryDefinition) repositoryDefinition;

			// check if the writing state is OK
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
				// ignore because if we can not get the info. we will still respond to user
				// action
			}

			CopyDataToStorageWizard wizard = new CopyDataToStorageWizard(cmrRepositoryDefinition, copyDataSet);
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
			dialog.open();
		}

		return null;
	}

}
