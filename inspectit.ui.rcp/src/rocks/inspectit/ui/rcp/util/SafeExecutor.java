package info.novatec.inspectit.rcp.util;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 * Safe executor enables calling the {@link Display#asyncExec(Runnable)} and
 * {@link Display#syncExec(Runnable)} with checking in a UI thread if any of the widgets that are
 * going to be updated in the given {@link Runnable} are disposed. The class enables easy avoiding
 * of <i>Widget already disposed</i> exceptions.
 * <p>
 * <b>IMPORTANT:</b> The class code is inspired from
 * <a href="https://gist.github.com/rherrmann/7324823630a089217f46#file-uithreadsynchronizer-L60">
 * Rüdiger Herrmann's UIThreadSynchronizer</a>. Original author is Rüdiger Herrmann. License info
 * can be found
 * <a href="hhttps://gist.github.com/rherrmann/7324823630a089217f46#file-uithreadsynchronizer-L60">
 * here</a>.
 *
 * @author Ivan Senic,
 *
 */
public final class SafeExecutor {

	/**
	 * Private constructor (utility class).
	 */
	private SafeExecutor() {
	}

	/**
	 * Executes the given runnable asynchronously within the {@link Display#asyncExec(Runnable)},
	 * but checks prior to execution that all of the given widgets are not disposed. Disposed checks
	 * are done in UI thread and if any check fails runnable will not be run.
	 * <p>
	 * Display to execute operation on will be retrieved by {@link Display#getDefault()}. Use
	 * {@link #asyncExec(Runnable, Display, Widget...)} if you want to use specific display.
	 *
	 * @param runnable
	 *            Runnable to run.
	 * @param widgets
	 *            Widgets to check for disposal.
	 * @see SafeExecutor#asyncExec(Runnable, Display, Widget...)
	 */
	public static void asyncExec(Runnable runnable, Widget... widgets) {
		asyncExec(runnable, Display.getDefault(), widgets);
	}

	/**
	 * Executes the given runnable asynchronously within the {@link Display#asyncExec(Runnable)},
	 * but checks prior to execution that all of the given widgets are not disposed. Disposed checks
	 * are done in UI thread and if any check fails runnable will not be run.
	 * <p>
	 * If given display is null or already disposed runnable will not be run, the method will simply
	 * return.
	 *
	 * @param runnable
	 *            Runnable to run.
	 * @param display
	 *            Display to use.
	 * @param widgets
	 *            Widgets to check for disposal.
	 */
	public static void asyncExec(Runnable runnable, Display display, Widget... widgets) {
		if (null == display || display.isDisposed()) {
			return;
		}
		display.asyncExec(new GuardedRunnable(runnable, widgets));
	}

	/**
	 * Executes the given runnable synchronously within the {@link Display#syncExec(Runnable)}, but
	 * checks prior to execution that all of the given widgets are not disposed. Disposed checks are
	 * done in UI thread and if any check fails runnable will not be run.
	 * <p>
	 * Display to execute operation on will be retrieved by {@link Display#getDefault()}. Use
	 * {@link #syncExec(Runnable, Display, Widget...)} if you want to use specific display.
	 *
	 * @param runnable
	 *            Runnable to run.
	 * @param widgets
	 *            Widgets to check for disposal.
	 * @see SafeExecutor#syncExec(Runnable, Display, Widget...)
	 */
	public static void syncExec(Runnable runnable, Widget... widgets) {
		syncExec(runnable, Display.getDefault(), widgets);
	}

	/**
	 * Executes the given runnable synchronously within the {@link Display#syncExec(Runnable)}, but
	 * checks prior to execution that all of the given widgets are not disposed. Disposed checks are
	 * done in UI thread and if any check fails runnable will not be run.
	 * <p>
	 * If given display is null or already disposed runnable will not be run, the method will simply
	 * return.
	 *
	 * @param runnable
	 *            Runnable to run.
	 * @param display
	 *            Display to use.
	 * @param widgets
	 *            Widgets to check for disposal.
	 */
	public static void syncExec(Runnable runnable, Display display, Widget... widgets) {
		if (null == display || display.isDisposed()) {
			return;
		}
		display.syncExec(new GuardedRunnable(runnable, widgets));
	}

	/**
	 * Runnable that guards from {@link Widget} disposal prior to running the delegate
	 * {@link Runnable}.
	 *
	 * @author Ivan Senic
	 *
	 */
	private static class GuardedRunnable implements Runnable {

		/**
		 * Delegate {@link Runnable}.
		 */
		private final Runnable runnable;

		/**
		 * {@link Widget}s to check for disposal.
		 */
		private final Widget[] widgets;

		/**
		 * Default constructor.
		 *
		 * @param runnable
		 *            Delegate {@link Runnable}.
		 * @param widgets
		 *            {@link Widget}s to check for disposal.
		 */
		GuardedRunnable(Runnable runnable, Widget... widgets) {
			this.runnable = runnable;
			this.widgets = widgets;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			if (ArrayUtils.isNotEmpty(widgets)) {
				for (Widget widget : widgets) {
					if (widget == null) {
						continue;
					} else if (widget.isDisposed()) {
						return;
					}
				}
			}
			runnable.run();
		}

	}
}
