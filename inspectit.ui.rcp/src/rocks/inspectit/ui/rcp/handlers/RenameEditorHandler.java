package rocks.inspectit.ui.rcp.handlers;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.editor.root.FormRootEditor;

/**
 * Handler for changing the name of the editor.
 * 
 * @author Ivan Senic
 * 
 */
public class RenameEditorHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FormRootEditor editor = (FormRootEditor) HandlerUtil.getActiveEditor(event);
		Shell shell = HandlerUtil.getActiveShell(event);
		IInputValidator inputValidator = new IInputValidator() {
			@Override
			public String isValid(String newText) {
				if (StringUtils.isEmpty(newText)) {
					return "Name of the view is required";
				}
				return null;
			}
		};
		InputDialog inputDialog = new InputDialog(shell, "Rename View", "Please enter new name for the active view", editor.getPartName(), inputValidator);
		inputDialog.open();
		if (inputDialog.getReturnCode() == Dialog.OK) {
			String name = inputDialog.getValue();
			editor.updateEditorName(name);
		}

		return null;
	}

}
