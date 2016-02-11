package info.novatec.inspectit.rcp.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for Clipboard operations.
 * 
 * @author Ivan Senic
 * 
 */
public final class ClipboardUtil {

	/**
	 * Private constructor.
	 */
	private ClipboardUtil() {
	}

	/**
	 * Sets the given text to the Clipboard so it can be used by other application.
	 * 
	 * @param display
	 *            {@link Display} to use.
	 * @param text
	 *            Text to copy to Clipboard.
	 */
	public static void textToClipboard(Display display, String text) {
		Assert.isNotNull(display);

		if (null == text) {
			return;
		}

		TextTransfer textTransfer = TextTransfer.getInstance();
		Clipboard cb = new Clipboard(display);
		cb.setContents(new Object[] { text }, new Transfer[] { textTransfer });

	}
}
