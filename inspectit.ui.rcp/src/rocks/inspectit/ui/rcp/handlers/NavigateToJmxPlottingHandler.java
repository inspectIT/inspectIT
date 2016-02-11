package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Handler for navigation to the plotting of JMX data.
 * 
 * @author Marius Oehler
 *
 */
public class NavigateToJmxPlottingHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();

		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		JmxSensorValueData jmxValueData = (JmxSensorValueData) selection.getFirstElement();

		if (jmxValueData != null) {
			JmxDefinitionDataIdent jmxIdent = repositoryDefinition.getCachedDataService().getJmxDefinitionDataIdentForId(jmxValueData.getJmxSensorDefinitionDataIdentId());

			InputDefinition inputDefinition = new InputDefinition();
			inputDefinition.setId(SensorTypeEnum.CHARTING_JMX_SENSOR_DATA);
			inputDefinition.setRepositoryDefinition(repositoryDefinition);

			EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
			editorPropertiesData.setSensorImage(SensorTypeEnum.CHARTING_JMX_SENSOR_DATA.getImage());
			editorPropertiesData.setSensorName(String.format("Chart - %s", jmxIdent.getmBeanAttributeName()));
			editorPropertiesData.setPartNameFlag(PartType.SENSOR);
			editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_BEAN));
			editorPropertiesData.setViewName(TextFormatter.getJmxDefinitionString(jmxIdent));
			inputDefinition.setEditorPropertiesData(editorPropertiesData);

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(jmxIdent.getPlatformIdent().getId());
			idDefinition.setSensorTypeId(jmxValueData.getSensorTypeIdent());
			idDefinition.setJmxDefinitionId(jmxIdent.getId());

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
