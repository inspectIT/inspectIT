package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.rcp.editor.preferences.IPreferencePanel;
import info.novatec.inspectit.rcp.editor.root.AbstractRootEditor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceScopes;

/**
 * Handler for the maximize/minimize the active sub-view. At the same time this Handler implements
 * the {@link IElementUpdater} interface so that we can manually update the checked state of the UI
 * elements that are bounded to the {@value #COMMAND_ID} command.
 * 
 * @author Ivan Senic
 * 
 */
public class MaximizeActiveViewHandler extends AbstractHandler implements IHandler, IElementUpdater {

	/**
	 * Command id.
	 */
	public static final String COMMAND_ID = "info.novatec.inspectit.rcp.commands.maximizeActiveView";

	/**
	 * Preference panel id parameter needed for this command.
	 */
	public static final String PREFERENCE_PANEL_ID_PARAMETER = COMMAND_ID + ".preferencePanelId";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if (editorPart instanceof AbstractRootEditor) {
			AbstractRootEditor abstractRootEditor = (AbstractRootEditor) editorPart;
			if (abstractRootEditor.canMaximizeActiveSubView()) {
				abstractRootEditor.maximizeActiveSubView();
			} else if (abstractRootEditor.canMinimizeActiveSubView()) {
				abstractRootEditor.minimizeActiveSubView();
			}
		}

		// after the maximized/minimized is executed we need to refresh the UI elements bounded to
		// the command, so that checked state of that elements is updated
		ICommandService commandService = (ICommandService) HandlerUtil.getActiveWorkbenchWindow(event).getService(ICommandService.class);
		Map<Object, Object> filter = new HashMap<Object, Object>();
		filter.put(IServiceScopes.WINDOW_SCOPE, HandlerUtil.getActiveWorkbenchWindow(event));
		commandService.refreshElements(event.getCommand().getId(), filter);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		// we'll only update the element that is bounded to the preference panel in the active
		// sub-view
		IWorkbenchWindow workbenchWindow = (IWorkbenchWindow) parameters.get("org.eclipse.ui.IWorkbenchWindow");
		String preferencePanelId = (String) parameters.get(PREFERENCE_PANEL_ID_PARAMETER);
		if (null != workbenchWindow && null != preferencePanelId) {
			IEditorPart editorPart = workbenchWindow.getActivePage().getActiveEditor();
			if (editorPart instanceof AbstractRootEditor) {
				AbstractRootEditor abstractRootEditor = (AbstractRootEditor) editorPart;
				IPreferencePanel preferencePanel = abstractRootEditor.getPreferencePanel();
				if (preferencePanelId.equals(preferencePanel.getId())) {
					element.setChecked(!abstractRootEditor.canMaximizeActiveSubView());
				}
			}
		}
	}
}
