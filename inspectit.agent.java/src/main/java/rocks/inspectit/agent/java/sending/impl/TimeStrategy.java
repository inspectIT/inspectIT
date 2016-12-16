package rocks.inspectit.agent.java.sending.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.IThreadTransformHelper;
import rocks.inspectit.agent.java.sending.AbstractSendingStrategy;

/**
 * Implements a strategy to wait a specific (user-defined) time and then executes the sending of the
 * data.
 *
 * @author Patrice Bouillet
 *
 */
public class TimeStrategy extends AbstractSendingStrategy {

	/**
	 * The default wait time.
	 */
	public static final long DEFAULT_WAIT_TIME = 5000L;

	/**
	 * The wait time.
	 */
	private long time = DEFAULT_WAIT_TIME;

	/**
	 * The thread which waits the specified time and starts the sending process.
	 */
	private volatile Trigger trigger;

	/**
	 * If we are allowed to send something right now.
	 */
	private boolean allowSending = true;

	/**
	 * {@link IThreadTransformHelper} to use to disable transformations done in the threads started
	 * by core service.
	 */
	@Autowired
	IThreadTransformHelper threadTransformHelper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void startStrategy() {
		trigger = new Trigger();
		trigger.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		// Interrupt the thread to stop it
		Thread temp = trigger;
		trigger = null; // NOPMD
		synchronized (temp) {
			temp.interrupt();
		}
	}

	/**
	 * The Trigger class is basically a {@link Thread} which starts the sending process once the
	 * specified time is passed by.
	 *
	 * @author Patrice Bouillet
	 *
	 */
	private class Trigger extends Thread {

		/**
		 * Creates a new <code>Trigger</code> as daemon thread.
		 */
		public Trigger() {
			setName("inspectit-timer-strategy-trigger-thread");
			setDaemon(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			// never do any transformation with this thread
			threadTransformHelper.setThreadTransformDisabled(true);

			Thread thisThread = Thread.currentThread();
			while (trigger == thisThread) { // NOPMD
				try {
					synchronized (this) {
						wait(time);
					}

					if (allowSending) {
						sendNow();
					}
				} catch (InterruptedException e) { // NOCHK
					// nothing to do
				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(Map<String, String> settings) {
		this.time = Long.parseLong(settings.get("time"));
	}

}
