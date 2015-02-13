package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.LoggingData;
import info.novatec.inspectit.rcp.util.ClipboardUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

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