package info.novatec.inspectit.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The show pull down menu handler.
 * <p>
 * Part of the code was taken from the https://www.eclipse.org/forums/index.php/t/488692/.
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
