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

import rocks.inspectit.shared.all.tracing.data.ISpanIdentAware;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.InputDefinitionExtrasMarkerFactory;
import rocks.inspectit.ui.rcp.editor.inputdefinition.extra.TraceInputDefinitionExtra;
import rocks.inspectit.ui.rcp.editor.root.AbstractRootEditor;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;

/**
 * Handler for opening the trace details view.
 *
 * @author Ivan Senic
 *
 */
public class OpenTraceDetailsHandler extends AbstractHandler {

	/**
	 * Command id.
	 */
	public static final String COMMAND = "rocks.inspectit.ui.rcp.commands.traceDetails";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelectionChecked(event);
		AbstractRootEditor rootEditor = (AbstractRootEditor) HandlerUtil.getActiveEditor(event);
		RepositoryDefinition repositoryDefinition = rootEditor.getInputDefinition().getRepositoryDefinition();

		Object selected = selection.getFirstElement();
		if (selected instanceof ISpanIdentAware) {
			SpanIdent spanIdent = ((ISpanIdentAware) selected).getSpanIdent();
			if (null != spanIdent) {
				InputDefinition inputDefinition = new InputDefinition();
				inputDefinition.setRepositoryDefinition(repositoryDefinition);
				inputDefinition.setId(SensorTypeEnum.TRACING_DETAILS);

				EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
				editorPropertiesData.setSensorName(SensorTypeEnum.TRACING.getDisplayName());
				editorPropertiesData.setSensorImage(SensorTypeEnum.TRACING.getImage());
				editorPropertiesData.setViewName("Details (" + Long.toHexString(spanIdent.getTraceId()) + ")");
				editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_PROPERTIES));
				editorPropertiesData.setPartNameFlag(PartType.VIEW);
				inputDefinition.setEditorPropertiesData(editorPropertiesData);

				inputDefinition.setIdDefinition(new IdDefinition());
				TraceInputDefinitionExtra traceInputDefinitionExtra = new TraceInputDefinitionExtra();
				traceInputDefinitionExtra.setTraceId(spanIdent.getTraceId());
				inputDefinition.addInputDefinitonExtra(InputDefinitionExtrasMarkerFactory.TRACE_EXTRAS_MARKER, traceInputDefinitionExtra);

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
					throw new ExecutionException("Error opening the trace details view.", e);
				}
			}
		}

		return null;
	}

}
