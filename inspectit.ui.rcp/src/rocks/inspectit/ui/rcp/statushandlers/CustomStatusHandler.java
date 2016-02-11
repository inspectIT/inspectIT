package info.novatec.inspectit.rcp.statushandlers;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.dialog.BusinessExceptionDialog;
import info.novatec.inspectit.rcp.dialog.ThrowableDialog;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.WorkbenchErrorHandler;

/**
 * Custom status manager for displaying statuses and exceptions correctly.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("restriction")
public class CustomStatusHandler extends WorkbenchErrorHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(final StatusAdapter statusAdapter, int style) {
		// if style is only log and we have an error
		// then we want to show it as well to the user
		if (StatusManager.LOG == style && statusAdapter.getStatus().getSeverity() == IStatus.ERROR) {
			style |= StatusManager.SHOW;
		}

		// here we only show new type if it's error or warning with exception in
		if (statusAdapter.getStatus().getException() != null && (statusAdapter.getStatus().getSeverity() == IStatus.WARNING || statusAdapter.getStatus().getSeverity() == IStatus.ERROR)) {

			if (((style & StatusManager.SHOW) == StatusManager.SHOW) || ((style & StatusManager.BLOCK) == StatusManager.BLOCK)) {

				if (Display.getCurrent() != null) {
					showErrorDialog(statusAdapter);
				} else {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							showErrorDialog(statusAdapter);
						}
					});

				}
			}

			if ((style & StatusManager.LOG) == StatusManager.LOG) {
				StatusManager.getManager().addLoggedStatus(statusAdapter.getStatus());

				WorkbenchPlugin.getDefault().getLog().log(statusAdapter.getStatus());
			}

		} else {
			super.handle(statusAdapter, style);
		}
	}

	/**
	 * Shows new style of error dialogs.
	 * 
	 * @param statusAdapter
	 *            {@link StatusAdapter}
	 */
	protected void showErrorDialog(StatusAdapter statusAdapter) {
		if (!PlatformUI.isWorkbenchRunning()) {
			// we are shutting down, so just log
			WorkbenchPlugin.log(statusAdapter.getStatus());
			return;
		}

		Dialog dialog = null;
		if (statusAdapter.getStatus().getException() instanceof BusinessException) {
			dialog = new BusinessExceptionDialog(null, (BusinessException) statusAdapter.getStatus().getException());
		} else {
			String message = statusAdapter.getStatus().getMessage();
			if (StringUtils.isEmpty(message)) {
				message = statusAdapter.getStatus().getException().getMessage();
			}
			dialog = new ThrowableDialog(null, message, statusAdapter.getStatus().getException());
		}
		dialog.open();
	}
}
