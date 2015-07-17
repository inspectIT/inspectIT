package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.HttpChartingInputDefinitionExtra;
import info.novatec.inspectit.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;
import info.novatec.inspectit.rcp.editor.table.input.TaggedHttpTimerDataInputController;
import info.novatec.inspectit.rcp.model.SensorTypeEnum;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.rcp.util.data.RegExAggregatedHttpTimerData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Handler for displaying the {@link HttpTimerData} in charts.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpDisplayInChartHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();
		InputDefinition inputDefinition = null;

		List<HttpTimerData> templates = new ArrayList<>();
		List<RegExAggregatedHttpTimerData> regExTemplates = new ArrayList<>();
		boolean regExTransformation = false;
		for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			Object selectedObject = iterator.next();
			if (selectedObject instanceof RegExAggregatedHttpTimerData) {
				templates.addAll(((RegExAggregatedHttpTimerData) selectedObject).getAggregatedDataList());
				regExTemplates.add((RegExAggregatedHttpTimerData) selectedObject);
				regExTransformation = true;
			} else if (selectedObject instanceof HttpTimerData) {
				templates.add((HttpTimerData) selectedObject);
			}
		}

		if (CollectionUtils.isNotEmpty(templates)) {
			boolean plotByTagValue = null != rootEditor.getSubView().getSubViewWithInputController(TaggedHttpTimerDataInputController.class);

			inputDefinition = new InputDefinition();
			inputDefinition.setRepositoryDefinition(repositoryDefinition);
			inputDefinition.setId(SensorTypeEnum.CHARTING_HTTP_TIMER_SENSOR);

			EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
			editorPropertiesData.setSensorImage(SensorTypeEnum.CHARTING_HTTP_TIMER_SENSOR.getImage());
			editorPropertiesData.setSensorName("Chart");
			editorPropertiesData.setPartNameFlag(PartType.SENSOR);
			if (templates.size() == 1 && !regExTransformation) {
				HttpTimerData template = templates.get(0);
				if (plotByTagValue) {
					editorPropertiesData.setViewName("Tag: " + template.getInspectItTaggingHeaderValue() + " [" + template.getRequestMethod() + "]");
				} else {
					editorPropertiesData.setViewName("URI: " + template.getUri() + " [" + template.getRequestMethod() + "]");
				}
			} else if (regExTemplates.size() == 1 && regExTransformation) {
				RegExAggregatedHttpTimerData regExTemplate = regExTemplates.get(0);
				editorPropertiesData.setViewName("Transformed URI: " + regExTemplate.getTransformedUri() + " [" + regExTemplate.getRequestMethod() + "]");
			} else {
				editorPropertiesData.setViewName("Multiple HTTP data");
			}

			inputDefinition.setEditorPropertiesData(editorPropertiesData);

			IdDefinition idDefinition = new IdDefinition();
			idDefinition.setPlatformId(templates.get(0).getPlatformIdent());
			idDefinition.setSensorTypeId(templates.get(0).getSensorTypeIdent());
			inputDefinition.setIdDefinition(idDefinition);

			HttpChartingInputDefinitionExtra inputDefinitionExtra = new HttpChartingInputDefinitionExtra();
			inputDefinitionExtra.setTemplates(templates);
			inputDefinitionExtra.setRegExTemplates(regExTemplates);
			inputDefinitionExtra.setPlotByTagValue(plotByTagValue);
			inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.HTTP_CHARTING_EXTRAS_MARKER, inputDefinitionExtra);

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
