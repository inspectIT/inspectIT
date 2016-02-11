package info.novatec.inspectit.rcp.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Action to add a menu to the preference view.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class MenuAction extends Action implements IMenuCreator {

	/**
	 * The menu manager.
	 */
	private final MenuManager menuManager;

	/**
	 * Creates a new menu.
	 */
	public MenuAction() {
		super("", Action.AS_DROP_DOWN_MENU);
		menuManager = new MenuManager();
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
	 * @see MenuManager#getSize()
	 * @return the number of contributions in this manager.
	 */
	public int getSize() {
		return menuManager.getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public Menu getMenu(Control parent) {
		return menuManager.createContextMenu(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	public Menu getMenu(Menu parent) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		menuManager.dispose();
	}

}