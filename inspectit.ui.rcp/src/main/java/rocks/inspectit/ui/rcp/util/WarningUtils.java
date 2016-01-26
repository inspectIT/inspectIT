package rocks.inspectit.ui.rcp.util;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;

import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;

/**
 * A default pop-up with some default settings that can be used to notify the user about certain
 * warning.
 *
 * @author Stefan Siegl
 * @author Ivan Senic
 */
public final class WarningUtils {

	/**
	 * Private.
	 */
	private WarningUtils() {
	}

	/**
	 * Creates a default warning pop-up in the current shell using asyncExec displaying the given
	 * title and explanation text. The pop-up comes with a toggle that allows the user to disable
	 * further displaying of the warning.
	 *
	 * @param title
	 *            Dialog title
	 * @param details
	 *            More details on the limitation of this feature.
	 * @param propertyKey
	 *            A key within the default preference key store of inspectIT where the information
	 *            is stored if this warning should not be shown again.
	 */
	public static void inform(final String title, final String details, final String propertyKey) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// read from property store. Returns "false" in case the property is not listed.
				boolean skipThisWarning = PreferencesUtils.getBooleanValue(propertyKey);

				if (skipThisWarning) {
					return;
				}

				// note that the MessageDialogWithToggle could also access the property store
				// directly, but it will not write back the changed value.
				MessageDialogWithToggle toggle = MessageDialogWithToggle.openWarning(Display.getCurrent().getActiveShell(), title, details, "Do not show this warning again",
						false, null, null);
				boolean state = toggle.getToggleState();
				PreferencesUtils.saveBooleanValue(propertyKey, state, false);
			}
		});
	}
}
