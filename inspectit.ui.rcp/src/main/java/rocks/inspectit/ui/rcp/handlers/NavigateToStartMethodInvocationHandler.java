package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.model.ModifiersImageFactory;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

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
			} catch (NotDefinedException | NotEnabledException | NotHandledException e) {
				throw new ExecutionException("Error opening the invocation data view.", e);
			}
		}
		return null;
	}
}
