package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.TimerDataChartingInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.ModifiersImageFactory;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
 * Handler for navigation from the aggregated timer data to the plotting.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigateToPlotting extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();
		InputDefinition inputDefinition = null;
		List<TimerData> templates = new ArrayList<>();

		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object selectedObject = it.next();
			TimerData timerData = null;
			if (selectedObject instanceof TimerData) {
				timerData = (TimerData) selectedObject;
			} else if (selectedObject instanceof InvocationSequenceData) {
				InvocationSequenceData invoc = (InvocationSequenceData) selectedObject;
				if (invoc.getTimerData() != null) {
					timerData = invoc.getTimerData();
				}
			}

			if (null != timerData) {
				TimerData template = new TimerData(null, timerData.getPlatformIdent(), timerData.getSensorTypeIdent(), timerData.getMethodIdent());
				templates.add(template);
			}
		}

		if (CollectionUtils.isEmpty(templates)) {
			return null;
		}

		inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(repositoryDefinition);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.CHARTING_TIMER.getImage());
		editorPropertiesData.setSensorName("Chart");
		editorPropertiesData.setPartNameFlag(PartType.SENSOR);
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		idDefinition.setPlatformId(templates.get(0).getPlatformIdent());
		inputDefinition.setIdDefinition(idDefinition);

		if (templates.size() == 1) {
			TimerData timerData = templates.get(0);
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(timerData.getMethodIdent());

			editorPropertiesData.setViewImage(ModifiersImageFactory.getImage(methodIdent.getModifiers()));
			editorPropertiesData.setViewName(TextFormatter.getMethodString(methodIdent));

			inputDefinition.setId(SensorTypeEnum.CHARTING_TIMER);
			idDefinition.setPlatformId(timerData.getPlatformIdent());
			idDefinition.setSensorTypeId(timerData.getSensorTypeIdent());
			idDefinition.setMethodId(timerData.getMethodIdent());
		} else {
			editorPropertiesData.setViewName("Multiple Timer data");

			TimerDataChartingInputDefinitionExtra definitionExtra = new TimerDataChartingInputDefinitionExtra();
			definitionExtra.setTemplates(templates);
			inputDefinition.setId(SensorTypeEnum.CHARTING_MULTI_TIMER);
			inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.TIMER_DATA_CHARTING_EXTRAS_MARKER, definitionExtra);
		}

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

		return null;
	}

}
