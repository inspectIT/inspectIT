package rocks.inspectit.ui.rcp.handlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.RemoteInvocationInputDefinitionExtra;
import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Handler for navigating remote invocation sequences.
 *
 * @author Thomas Kluge
 *
 */
public class NavigateToRemoteInvocationHandler extends AbstractTemplateHandler {

	/**
	 * If the current selection is a remote call data object or an invocation sequence data object
	 * containing a remote call object, the navigation handler starts a navigation to target of the
	 * remote call. {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();

		Object selectedObject = selection.getFirstElement();
		long platformIdent = getPlatformIdent(selectedObject);
		List<RemoteCallData> remoteDataList = new ArrayList<RemoteCallData>();

		if (selectedObject instanceof RemoteCallData) {
			remoteDataList.add((RemoteCallData) selectedObject);
		} else if (selectedObject instanceof InvocationSequenceData) {
			InvocationSequenceData invocationSequenceData = repositoryDefinition.getInvocationDataAccessService().getInvocationSequenceDetail((InvocationSequenceData) selectedObject);
			LinkedList<InvocationSequenceData> workingList = new LinkedList<>();
			workingList.add(invocationSequenceData);

			while (!workingList.isEmpty()) {
				InvocationSequenceData invocationSequence = workingList.removeFirst();

				if (invocationSequence.getRemoteCallData() != null) {
					remoteDataList.add(invocationSequence.getRemoteCallData());
				}

				if (invocationSequence.getNestedSequences() != null) {
					workingList.addAll(invocationSequence.getNestedSequences());
				}
			}
		}

		if (!remoteDataList.isEmpty()) {
			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(repositoryDefinition);
			// TODO
			// inputDefinition.setId(SensorTypeEnum.NAVIGATION_REMOTE_INVOCATION);

			EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
			// editorPropertiesData.setSensorImage(SensorTypeEnum.NAVIGATION_REMOTE_INVOCATION.getImage());
			// editorPropertiesData.setSensorName(SensorTypeEnum.NAVIGATION_REMOTE_INVOCATION.getDisplayName());
			editorPropertiesData.setViewName("that contain " + "Remote Calls");
			editorPropertiesData.setPartNameFlag(PartType.SENSOR);
			inputDefinition.setEditorPropertiesData(editorPropertiesData);

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(platformIdent);
			inputDefinition.setIdDefinition(idDefinition);

			RemoteInvocationInputDefinitionExtra navigationSteppingExtra = new RemoteInvocationInputDefinitionExtra();
			navigationSteppingExtra.setRemoteCallDataList(remoteDataList);
			inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.REMOTE_INVOCATION_EXTRAS_MARKER, navigationSteppingExtra);

			// open the view via command
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

			Command command = commandService.getCommand(OpenViewHandler.COMMAND);
			ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
			IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
			context.addVariable(OpenViewHandler.INPUT, inputDefinition);

			try {
				command.executeWithChecks(executionEvent);
			} catch (Exception e) {
				InspectIT.getDefault().createErrorDialog(e.getMessage(), e, -1);
			}
		}

		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the platform id for the object.
	 *
	 * @param firstElement
	 *            Object.
	 * @return If object is instance of {@link DefaultData} method returns its platform id,
	 *         otherwise 0.
	 */
	private long getPlatformIdent(Object firstElement) {
		if (firstElement instanceof DefaultData) {
			return ((DefaultData) firstElement).getPlatformIdent();
		}
		return 0;
	}

}