package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.provider.IInputDefinitionProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.view.impl.RepositoryManagerView;
import info.novatec.inspectit.util.ObjectUtils;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Handler for clearing the repository buffer.
 * 
 * @author Ivan Senic
 * 
 */
public class ClearRepositoryBufferHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		CmrRepositoryDefinition availableCmr = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				ICmrRepositoryProvider cmrRepositoryProvider = (ICmrRepositoryProvider) selectedObject;
				availableCmr = cmrRepositoryProvider.getCmrRepositoryDefinition();
			}
		}
		if (null == availableCmr) {
			IWorkbenchPart editor = HandlerUtil.getActivePart(event);
			if (editor instanceof IInputDefinitionProvider) {
				IInputDefinitionProvider inputDefinitionProvider = (IInputDefinitionProvider) editor;
				if (inputDefinitionProvider.getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
					availableCmr = (CmrRepositoryDefinition) inputDefinitionProvider.getInputDefinition().getRepositoryDefinition();
				}
			}
		}

		final CmrRepositoryDefinition cmrRepositoryDefinition = availableCmr;
		if (null != cmrRepositoryDefinition && cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
			boolean isSure = MessageDialog.openConfirm(null, "Empty buffer",
					"Are you sure that you want to completely delete all the data in the buffer on repository " + cmrRepositoryDefinition.getName() + " (" + cmrRepositoryDefinition.getIp() + ":"
							+ cmrRepositoryDefinition.getPort() + ")?");
			if (isSure) {
				Job clearBufferJob = new Job("Clear Respoitory Buffer") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						cmrRepositoryDefinition.getCmrManagementService().clearBuffer();
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
								IEditorReference[] editors = window.getActivePage().getEditorReferences();
								for (IEditorReference editor : editors) {
									IEditorPart editorPart = editor.getEditor(false);
									if (editorPart instanceof IRootEditor) {
										IRootEditor rootEditor = (IRootEditor) editorPart;
										if (null != rootEditor.getPreferencePanel()) {
											if (rootEditor.getSubView().getPreferenceIds().contains(PreferenceId.CLEAR_BUFFER)) {
												InputDefinition inputDefinition = rootEditor.getInputDefinition();
												if (ObjectUtils.equals(inputDefinition.getRepositoryDefinition(), cmrRepositoryDefinition)) {
													rootEditor.getSubView().setDataInput(Collections.<DefaultData> emptyList());
												}
											}
										}
									}
								}
								IViewPart viewPart = window.getActivePage().findView(RepositoryManagerView.VIEW_ID);
								if (viewPart instanceof RepositoryManagerView) {
									((RepositoryManagerView) viewPart).refresh();
								}

							}
						});
						return Status.OK_STATUS;
					}
				};
				clearBufferJob.setUser(true);
				clearBufferJob.setProperty(IProgressConstants.ICON_PROPERTY, InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_BUFFER_CLEAR));
				clearBufferJob.schedule();
			}
		}
		return null;
	}

}
