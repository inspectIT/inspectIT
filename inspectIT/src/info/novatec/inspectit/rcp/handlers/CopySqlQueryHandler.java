package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.data.SqlStatementData;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
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
		if (firstElement instanceof SqlStatementData) {
			SqlStatementData sqlStatementData = (SqlStatementData) firstElement;
			TextTransfer textTransfer = TextTransfer.getInstance();
			Clipboard cb = new Clipboard(HandlerUtil.getActiveShell(event).getDisplay());
			cb.setContents(new Object[] { sqlStatementData.getSqlWithParameterValues() }, new Transfer[] { textTransfer });
		}
		return null;
	}

}
