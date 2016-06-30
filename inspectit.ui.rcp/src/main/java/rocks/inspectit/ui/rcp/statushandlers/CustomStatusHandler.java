package rocks.inspectit.ui.rcp.statushandlers;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.statushandlers.WorkbenchErrorHandler;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.ui.rcp.dialog.BusinessExceptionDialog;
import rocks.inspectit.ui.rcp.dialog.ThrowableDialog;

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
		if ((StatusManager.LOG == style) && (statusAdapter.getStatus().getSeverity() == IStatus.ERROR)) {
			style |= StatusManager.SHOW;
		}

		// here we only show new type if it's error or warning with exception in
		if ((statusAdapter.getStatus().getException() != null) && ((statusAdapter.getStatus().getSeverity() == IStatus.WARNING) || (statusAdapter.getStatus().getSeverity() == IStatus.ERROR))) {

			if (((style & StatusManager.SHOW) == StatusManager.SHOW) || ((style & StatusManager.BLOCK) == StatusManager.BLOCK)) {

				if (Display.getCurrent() != null) {
					showErrorDialog(statusAdapter);
				} else {
					Display.getDefault().syncExec(new Runnable() {
						@Override
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
		if (ignore(statusAdapter.getStatus())) {
			return;
		}

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

	/**
	 * If status should be ignored for showing the the error dialog.
	 * <P>
	 * Currently we ignore statuses with following exceptions:
	 * <ul>
	 * <li>NullPointerException on MacOSx in the
	 * {@link org.eclipse.swt.widgets.Control#internal_new_GC(org.eclipse.swt.graphics.GCData)}
	 * method as it's known SWT bug.
	 * </ul>
	 *
	 * @param status
	 *            status to check
	 * @return true if this status should be ignored
	 */
	private boolean ignore(IStatus status) {
		Throwable exception = status.getException();
		if ((exception instanceof NullPointerException) && SystemUtils.IS_OS_MAC_OSX) {
			StackTraceElement[] stackTrace = exception.getStackTrace();
			if (ArrayUtils.isNotEmpty(stackTrace)) {
				StackTraceElement topElement = stackTrace[0];
				if ("org.eclipse.swt.widgets.Control".equals(topElement.getClassName()) && "internal_new_GC".equals(topElement.getMethodName())) {
					return true;
				}
			}
		}
		return false;
	}
}
