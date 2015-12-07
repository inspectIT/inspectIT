package info.novatec.inspectit.rcp.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Action to add a menu to the preference view.
 *
 * @author Patrice Bouillet, Alexander Wert
 *
 */
public final class MenuAction extends Action implements IMenuCreator {

	/**
	 * The menu manager.
	 */
	private final MenuManager menuManager;

	/**
	 * The task to execute when action is selected.
	 */
	private RunTask runTask;

	/**
	 * Constructor. Creates a new menu.
	 */
	public MenuAction() {
		this(null, "", null);
	}

	/**
	 * Constructor. Creates a new menu.
	 *
	 * @param text
	 *            The menu text.
	 * @param image
	 *            The menu image.
	 */
	public MenuAction(String text, ImageDescriptor image) {
		this(null, text, image);
	}

	/**
	 * Constructor. Creates a new menu.
	 *
	 * @param runnable
	 *            The task to execute when action is selected. If null, no task will be performed.
	 */
	public MenuAction(RunTask runnable) {
		this(runnable, "", null);
	}

	/**
	 * Constructor. Creates a new menu.
	 *
	 * @param runnable
	 *            The task to execute when action is selected. If null, no task will be performed.
	 * @param text
	 *            The menu text.
	 * @param image
	 *            The menu image.
	 */
	public MenuAction(RunTask runnable, String text, ImageDescriptor image) {
		super("", Action.AS_DROP_DOWN_MENU);
		menuManager = new MenuManager(text, image, text);
		setText(text);
		if (null != image) {
			setImageDescriptor(image);
		}

		setRunTask(runnable);
		setMenuCreator(this);
	}

	/**
	 * Adds a contribution item to this manager, like a sub-menu ...
	 *
	 * @param contributionItem
	 *            THe contribution item to add.
	 */
	public void addContributionItem(IContributionItem contributionItem) {
		menuManager.add(contributionItem);
	}

	/**
	 * Adds an action to this manager.
	 *
	 * @param action
	 *            The action to add.
	 */
	public void addAction(IAction action) {
		menuManager.add(action);
	}

	/**
	 * @see MenuManager#getSize()
	 * @return the number of contributions in this manager.
	 */
	public int getSize() {
		return menuManager.getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Menu getMenu(Control parent) {
		return menuManager.createContextMenu(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Menu getMenu(Menu parent) {
		Menu dropDownMenu = new Menu(parent.getParent(), SWT.DROP_DOWN);
		int i = 0;
		for (IContributionItem ci : menuManager.getItems()) {
			ci.fill(dropDownMenu, i);
			i++;
		}
		return dropDownMenu;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		menuManager.dispose();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runWithEvent(Event event) {
		RunTask runTask = getRunTask();
		if (null != runTask) {
			runTask.setEvent(event);
			if (runTask instanceof ToolbarDropDownTask) {
				((ToolbarDropDownTask) runTask).setMenuAction(this);
			}
			Display.getDefault().syncExec(runTask);
		}
	}

	/**
	 * Gets {@link #runTask}.
	 *
	 * @return {@link #runTask}
	 */
	public RunTask getRunTask() {
		return runTask;
	}

	/**
	 * Sets {@link #runTask}.
	 *
	 * @param runTask
	 *            New value for {@link #runTask}
	 */
	public void setRunTask(RunTask runTask) {
		this.runTask = runTask;
	}

	/**
	 * This is special {@link Runnable} that is aware of an {@link Event} that has triggered the
	 * execution of the {@link Runnable}.
	 *
	 * @author Alexander Wert
	 *
	 */
	public abstract static class RunTask implements Runnable {

		/**
		 * Event that trigger the execution of this task.
		 */
		private Event event;

		/**
		 * Gets {@link #event}.
		 *
		 * @return {@link #event}
		 */
		public Event getEvent() {
			return event;
		}

		/**
		 * Sets {@link #event}.
		 *
		 * @param event
		 *            New value for {@link #event}
		 */
		public void setEvent(Event event) {
			this.event = event;
		}
	}

	/**
	 * This is special {@link RunTask} that executes the drop down of the menu when the
	 * {@link MenuAction} is selected in a {@link ToolBar}.
	 *
	 * @author Alexander Wert
	 *
	 */
	public static class ToolbarDropDownTask extends RunTask {

		/**
		 * {@link ToolBar} containing the corresponding {@link MenuAction} as an {@link ToolItem}.
		 */
		private final ToolBar toolbar;

		/**
		 * {@link MenuAction} to execute this task for.
		 */
		private MenuAction menuAction;

		/**
		 * Constructor.
		 *
		 * @param toolbar
		 *            {@link ToolBar} containing the corresponding {@link MenuAction} as an
		 *            {@link ToolItem}.
		 */
		public ToolbarDropDownTask(ToolBar toolbar) {
			this.toolbar = toolbar;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			Menu menu = getMenuAction().getMenu(toolbar);
			if (menu != null && getEvent().widget instanceof ToolItem) {
				Rectangle rect = ((ToolItem) getEvent().widget).getBounds();
				Point point = toolbar.toDisplay(rect.x, rect.y + toolbar.getBounds().height);
				menu.setLocation(point.x, point.y);
				menu.setVisible(true);
			}

		}

		/**
		 * Gets {@link #menuAction}.
		 *
		 * @return {@link #menuAction}
		 */
		public MenuAction getMenuAction() {
			return menuAction;
		}

		/**
		 * Sets {@link #menuAction}.
		 *
		 * @param menuAction
		 *            New value for {@link #menuAction}
		 */
		public void setMenuAction(MenuAction menuAction) {
			this.menuAction = menuAction;
		}
	}
}