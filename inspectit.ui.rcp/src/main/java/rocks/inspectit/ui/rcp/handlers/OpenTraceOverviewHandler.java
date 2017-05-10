package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.InspectITImages;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData;
import rocks.inspectit.ui.rcp.editor.inputdefinition.EditorPropertiesData.PartType;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import rocks.inspectit.ui.rcp.editor.root.FormRootEditor;
import rocks.inspectit.ui.rcp.editor.root.RootEditorInput;
import rocks.inspectit.ui.rcp.model.SensorTypeEnum;
import rocks.inspectit.ui.rcp.provider.ICmrRepositoryProvider;
import rocks.inspectit.ui.rcp.provider.IInputDefinitionProvider;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.StorageRepositoryDefinition;

/**
 * Handler for opening the trace overview view.
 *
 * @author Iavn Senic
 */
public class OpenTraceOverviewHandler extends AbstractHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		RepositoryDefinition availableRepository = null;

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection) {
			Object selectedObject = ((StructuredSelection) selection).getFirstElement();
			if (selectedObject instanceof ICmrRepositoryProvider) {
				ICmrRepositoryProvider cmrRepositoryProvider = (ICmrRepositoryProvider) selectedObject;
				availableRepository = cmrRepositoryProvider.getCmrRepositoryDefinition();
			} else if (selectedObject instanceof StorageRepositoryDefinition) {
				availableRepository = (RepositoryDefinition) selectedObject;
			}
		}
		if (null == availableRepository) {
			IWorkbenchPart editor = HandlerUtil.getActivePart(event);
			if (editor instanceof IInputDefinitionProvider) {
				IInputDefinitionProvider inputDefinitionProvider = (IInputDefinitionProvider) editor;
				availableRepository = inputDefinitionProvider.getInputDefinition().getRepositoryDefinition();
			}
		}

		if (null != availableRepository) {
			InputDefinition inputDefinition = createInputDefinition(availableRepository);

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
	 * Creates input definition for the tracing overview view.
	 *
	 * @param repository
	 *            repository to use
	 * @return input definition
	 */
	private InputDefinition createInputDefinition(RepositoryDefinition repository) {
		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setRepositoryDefinition(repository);
		inputDefinition.setId(SensorTypeEnum.TRACING);

		EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
		editorPropertiesData.setSensorImage(SensorTypeEnum.TRACING.getImage());
		editorPropertiesData.setSensorName("Tracing");
		editorPropertiesData.setViewImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SHOW_ALL));
		editorPropertiesData.setViewName("Show All");
		editorPropertiesData.setPartNameFlag(PartType.VIEW);
		inputDefinition.setEditorPropertiesData(editorPropertiesData);

		IdDefinition idDefinition = new IdDefinition();
		inputDefinition.setIdDefinition(idDefinition);
		return inputDefinition;
	}

}
