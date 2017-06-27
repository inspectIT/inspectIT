package rocks.inspectit.ui.rcp.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.cs.communication.data.cmr.Alert;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.dialog.AlertSelectionDialog;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.AlertInputDefinitionExtra;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.root.FormRootEditor;
import rocks.inspectit.ui.rcp.editor.root.RootEditorInput;
import rocks.inspectit.ui.rcp.formatter.ImageFormatter;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

/**
 * Handler to open alert view.
 *
 * @author Alexander Wert
 *
 */
public class OpenAlertHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		if ((selection != null) && !selection.isEmpty() && (selection instanceof StructuredSelection)) {
			Iterator<?> iterator = ((StructuredSelection) selection).iterator();
			while (iterator.hasNext()) {
				Object object = iterator.next();
				if (object instanceof CmrRepositoryDefinition) {
					cmrRepositoryDefinition = (CmrRepositoryDefinition) object;
					break;
				}
			}
		}

		List<CmrRepositoryDefinition> onlineCMRs = new ArrayList<>();
		for (CmrRepositoryDefinition cmr : InspectIT.getDefault().getCmrRepositoryManager().getCmrRepositoryDefinitions()) {
			if (cmr.getOnlineStatus().equals(OnlineStatus.ONLINE)) {
				onlineCMRs.add(cmr);
			}
		}

		if (onlineCMRs.isEmpty()) {
			throw new ExecutionException("Cannot open alert view for alert: No CMR is online!");
		}

		AlertSelectionDialog dialog = new AlertSelectionDialog(HandlerUtil.getActiveShell(event), cmrRepositoryDefinition, onlineCMRs);
		if (Dialog.OK == dialog.open()) {
			InputDefinition inputDefinition = createInputDefinition(dialog.getCmrRepositoryDefinition(), dialog.getAlert());

			// Get the view
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			// open the view if the input definition is set
			if (null != inputDefinition) {
				RootEditorInput input = new RootEditorInput(inputDefinition);
				try {
					page.openEditor(input, FormRootEditor.ID);
				} catch (PartInitException e) {
					throw new ExecutionException("Exception occurred trying to open the editor.", e);
				}
			}
		}

		return null;
	}

	/**
	 * Creates {@link InputDefinition} for Alert-Invocations view.
	 *
	 * @param cmrRepository
	 *            CMR Repository definition.
	 * @param alert
	 *            The alert.
	 * @return The {@link InputDefinition} instance.
	 */
	private InputDefinition createInputDefinition(CmrRepositoryDefinition cmrRepository, Alert alert) {
		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(cmrRepository);
		AlertInputDefinitionExtra alertInputExtra = new AlertInputDefinitionExtra();
		alertInputExtra.setAlert(alert);
		inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.ALERT_EXTRAS_MARKER, alertInputExtra);
		inputDefinition.setId(SensorTypeEnum.ALERT_INVOCATION);
		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.ALERT_INVOCATION.getImage());
		editorPropertiesData.setSensorName("Alert Invocation Sequences");
		editorPropertiesData.setViewImage(ImageFormatter.getAlertImage(alert));
		editorPropertiesData.setViewName(TextFormatter.getAlertDescription(alert));
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(0);

		inputDefinition.setIdDefinition(idDefinition);
		return inputDefinition;
	}

}
