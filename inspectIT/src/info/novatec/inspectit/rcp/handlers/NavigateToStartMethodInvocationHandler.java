package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.ModifiersImageFactory;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.core.commands.AbstractHandler;
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

/**
 * Handler for showing only invocations sequences for a specific method ID.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigateToStartMethodInvocationHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();

		Object selectedObject = selection.getFirstElement();
		if (selectedObject instanceof InvocationSequenceData) {
			InvocationSequenceData invocationSequenceData = (InvocationSequenceData) selectedObject;

			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(invocationSequenceData.getMethodIdent());

			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(repositoryDefinition);
			inputDefinition.setId(SensorTypeEnum.INVOCATION_SEQUENCE);

			EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
			editorPropertiesData.setSensorImage(SensorTypeEnum.INVOCATION_SEQUENCE.getImage());
			editorPropertiesData.setSensorName(SensorTypeEnum.INVOCATION_SEQUENCE.getDisplayName());
			editorPropertiesData.setViewImage(ModifiersImageFactory.getImage(methodIdent.getModifiers()));
			editorPropertiesData.setViewName(TextFormatter.getMethodString(methodIdent));
			editorPropertiesData.setPartNameFlag(PartType.SENSOR);
			inputDefinition.setEditorPropertiesData(editorPropertiesData);

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(invocationSequenceData.getPlatformIdent());
			idDefinition.setMethodId(invocationSequenceData.getMethodIdent());
			idDefinition.setSensorTypeId(invocationSequenceData.getSensorTypeIdent());

			inputDefinition.setIdDefinition(idDefinition);

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
		return null;
	}
}
