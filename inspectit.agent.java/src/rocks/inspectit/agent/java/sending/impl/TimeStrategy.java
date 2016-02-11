package info.novatec.inspectit.agent.sending.impl;

import info.novatec.inspectit.agent.sending.AbstractSendingStrategy;

import java.util.Map;

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
	 * {@inheritDoc}
	 */
	public void startStrategy() {
		trigger = new Trigger();
		trigger.start();
	}

	/**
	 * {@inheritDoc}
	 */
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
		public void run() {
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
	public void init(Map<String, String> settings) {
		this.time = Long.parseLong(settings.get("time"));
	}

}
