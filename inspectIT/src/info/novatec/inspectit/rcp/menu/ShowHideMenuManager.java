package info.novatec.inspectit.rcp.menu;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.handlers.ShowHideColumnsHandler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * Menu manager for displaying the show/hide columns group.
 * 
 * @author Ivan Senic
 * 
 */
public class ShowHideMenuManager extends MenuManager implements IMenuListener {

	/**
	 * Viewer to display menu for.
	 */
	private ColumnViewer columnViewer;

	/**
	 * Input controller class.
	 */
	private Class<?> inputControllerClass;

	/**
	 * Default constructor.
	 * 
	 * @param columnViewer
	 *            Viewer to display menu for.
	 * @param inputControllerClass
	 *            Input controller class.
	 */
	public ShowHideMenuManager(ColumnViewer columnViewer, Class<?> inputControllerClass) {
		Assert.isNotNull(columnViewer);
		Assert.isNotNull(inputControllerClass);

		this.columnViewer = columnViewer;
		this.inputControllerClass = inputControllerClass;

		this.addMenuListener(this);
		for (IAction actionItem : getActionItems()) {
			this.add(actionItem);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void menuAboutToShow(IMenuManager manager) {
		this.removeAll();
		for (IAction actionItem : getActionItems()) {
			this.add(actionItem);
		}
	}

	/**
	 * Returns all contribution items.
	 * 
	 * @return Returns all contribution items.
	 */
	private IAction[] getActionItems() {
		List<IAction> items = new ArrayList<IAction>();
		if (columnViewer instanceof TableViewer) {
			TableColumn[] columns = ((TableViewer) columnViewer).getTable().getColumns();
			for (TableColumn column : columns) {
				items.add(new ShowHideColumnAction(column));
			}
		} else if (columnViewer instanceof TreeViewer) {
			TreeColumn[] columns = ((TreeViewer) columnViewer).getTree().getColumns();
			for (TreeColumn column : columns) {
				items.add(new ShowHideColumnAction(column));
			}
		}
		return items.toArray(new IAction[items.size()]);
	}

	/**
	 * Action for showing ot hiding one column.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class ShowHideColumnAction extends Action {

		/**
		 * Column for the action.
		 */
		private Item column;

		/**
		 * Should the column be visible after action execution.
		 */
		private boolean visible;

		/**
		 * Default constructor.
		 * 
		 * @param column
		 *            Column for the action.
		 */
		public ShowHideColumnAction(Item column) {
			Assert.isNotNull(column);
			this.column = column;

			String tooltip;
			ImageDescriptor icon = null;
			int width = 0;
			if (column instanceof TableColumn) {
				width = ((TableColumn) column).getWidth();
			} else if (column instanceof TreeColumn) {
				width = ((TreeColumn) column).getWidth();
			} else {
				RuntimeException exception = new RuntimeException("Unsupported item provided during dynamic columns menu creation. Item class is " + column.getClass().getName() + ".");
				InspectIT.getDefault().createErrorDialog("Error creating dynamic column menu", exception, -1);
				throw exception;
			}

			if (width > 0) {
				visible = false;
				tooltip = "Hide column";
				icon = InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_CHECKMARK);
			} else {
				visible = true;
				tooltip = "Show column";
			}

			this.setText(column.getText());
			this.setImageDescriptor(icon);
			this.setToolTipText(tooltip);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

			IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
			ICommandService commandService = (ICommandService) window.getService(ICommandService.class);

			Command command = commandService.getCommand(ShowHideColumnsHandler.COMMAND_ID);
			ExecutionEvent executionEvent = handlerService.createExecutionEvent(command, new Event());
			IEvaluationContext context = (IEvaluationContext) executionEvent.getApplicationContext();
			context.addVariable(ShowHideColumnsHandler.COLUMN_PARAM, column);
			context.addVariable(ShowHideColumnsHandler.VISIBLE_PARAM, visible);
			context.addVariable(ShowHideColumnsHandler.CONTROLLER_CLASS_PARAM, inputControllerClass);

			try {
				command.executeWithChecks(executionEvent);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
