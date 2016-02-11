package info.novatec.inspectit.rcp.util;

import info.novatec.inspectit.rcp.preferences.PreferencesUtils;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;

/**
 * A default popup with some default settings that can be used to notify the user that a certain
 * feature is currently not fully functional.
 * 
 * In order to allow for quicker and smaller integration steps we allow features to be integrated
 * for pre-releases that are not fully functional yet. These features should be labelled with this
 * popup to make the end user aware of that.
 * 
 * @author Stefan Siegl
 */
public final class UnfinishedWarningUtils {

	/**
	 * Private.
	 */
	private UnfinishedWarningUtils() {
	}

	/**
	 * Creates a default warning popup in the current shell using asyncExec displaying the given
	 * explanation text. The popup comes with a toogle that allows the user to disable further
	 * displayal of the warning.
	 * 
	 * @param details
	 *            More details on the limitation of this feature.
	 * @param propertyKey
	 *            A key within the default preference keystore of inspectIT where the information is
	 *            stored if this warning should not be shown again.
	 */
	public static void inform(final String details, final String propertyKey) {
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
				MessageDialogWithToggle toggle = MessageDialogWithToggle.openWarning(Display.getCurrent().getActiveShell(), "Warning: Feature not fully functional", details, "do not show this warning again",
						false, null, null);
				boolean state = toggle.getToggleState();
				PreferencesUtils.saveBooleanValue(propertyKey, state, false);

			}
		});
	}
}
