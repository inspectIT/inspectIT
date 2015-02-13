package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.rcp.util.ClipboardUtil;

import static org.hamcrest.Matchers.instanceOf;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler that copies the SQL Query string to the clipboard.
 * 
 * @author Ivan Senic
 * 
 */
public class CopySqlQueryHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object firstElement = ((StructuredSelection) HandlerUtil.getCurrentSelection(event)).getFirstElement();
		if (firstElement instanceof InvocationSequenceData) {
			InvocationSequenceData data = (InvocationSequenceData) firstElement;
			SqlStatementData sqlStatementData = data.getSqlStatementData();
			if (null == sqlStatementData) {
				return null;
			}
			ClipboardUtil.textToClipboard(HandlerUtil.getActiveShell(event).getDisplay(), data.getSqlStatementData().getSqlWithParameterValues());
		}
		if (firstElement instanceof SqlStatementData) {
			SqlStatementData sqlStatementData = (SqlStatementData) firstElement;
			ClipboardUtil.textToClipboard(HandlerUtil.getActiveShell(event).getDisplay(), sqlStatementData.getSqlWithParameterValues());
		}
		return null;
	}

}
