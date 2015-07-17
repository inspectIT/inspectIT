package info.novatec.inspectit.rcp.statushandlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.WorkbenchErrorHandler;

/**
 * Custom status manager for displaying statuses and exceptions correctly.
 * 
 * @author Ivan Senic
 * 
 */
public class CustomStatusHandler extends WorkbenchErrorHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(StatusAdapter statusAdapter, int style) {
		// if style is only log and we have an error
		// then we want to show it as well to the user
		if (StatusManager.LOG == style && statusAdapter.getStatus().getSeverity() == IStatus.ERROR) {
			style |= StatusManager.SHOW;
		}

		super.handle(statusAdapter, style);
	}
}
