package rocks.inspectit.ui.rcp.handlers;

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

import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

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
