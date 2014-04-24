package info.novatec.inspectit.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The show pull down menu handler. This is a fix for the toolbar items that have the drop down menu
 * (for us usually a + icons for adding new Storage, Cmr, Profile, etc..). Before clicking on the
 * icon it self was not showing the drop down menu, but only if you select the arrow on the right.
 * Now this is fixed and clicking on the icon itself with show the menu.
 * <p>
 * <b>IMPORTANT:</b> The class code is copied/taken from <a
 * href="https://www.eclipse.org/forums/index.php/t/488692/">Eclipse forums</a>. Original author is
 * Mario Marinato. License info can be found <a
 * href="https://eclipse.org/legal/termsofuse.php">here</a>.
 * 
 * @author Ivan Senic
 * @author Mario Marinato
 * 
 */
public class ShowPullDownMenuHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Class check.
		if (!(event.getTrigger() instanceof Event)) {
			return null;
		}

		Event eventWidget = (Event) event.getTrigger();

		// Makes sure event came from a ToolItem.
		if (!(eventWidget.widget instanceof ToolItem)) {
			return null;
		}

		ToolItem toolItem = (ToolItem) eventWidget.widget;

		// Creates fake selection event.
		Event newEvent = new Event();
		newEvent.button = 1;
		newEvent.widget = toolItem;
		newEvent.detail = SWT.ARROW;
		newEvent.x = toolItem.getBounds().x;
		newEvent.y = toolItem.getBounds().y + toolItem.getBounds().height;

		// Dispatches the event.
		toolItem.notifyListeners(SWT.Selection, newEvent);

		return null;
	}

}
