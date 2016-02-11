package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.LoggingData;
import rocks.inspectit.ui.rcp.util.ClipboardUtil;

/**
 * Handler that copies the logging message to the clipboard.
 * 
 * @author Stefan Siegl
 */
public class CopyLogMessageHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object firstElement = ((StructuredSelection) HandlerUtil.getCurrentSelection(event)).getFirstElement();
		if (firstElement instanceof LoggingData) {
			LoggingData loggingData = (LoggingData) firstElement;
			ClipboardUtil.textToClipboard(HandlerUtil.getActiveShell(event).getDisplay(), loggingData.getMessage());
		} else if (firstElement instanceof InvocationSequenceData) {
			LoggingData loggingData = ((InvocationSequenceData) firstElement).getLoggingData();
			ClipboardUtil.textToClipboard(HandlerUtil.getActiveShell(event).getDisplay(), loggingData.getMessage());
		}
		return null;
	}

}