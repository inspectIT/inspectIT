package rocks.inspectit.ui.rcp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.ui.rcp.view.IRefreshableView;

/**
 * Handler for refreshing the {@link IRefreshableView}.
 *
 * @author Ivan Senic
 *
 */
public class RefreshViewHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart workbenchPart = HandlerUtil.getActivePart(event);
		if (workbenchPart instanceof IRefreshableView) {
			((IRefreshableView) workbenchPart).refresh();
		}
		return null;
	}

}
