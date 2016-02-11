package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;

/**
 * Abstract class for all threads that work on the buffer. The work each worker performs is defined
 * in abstract method {@link #work()}.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class BufferWorker extends Thread {

	/**
	 * Buffer to work on.
	 */
	private IBuffer<?> buffer;

	/**
	 * Default constructor. Thread is set to be a daemon, to have highest priority and started.
	 * 
	 * @param buffer
	 *            Buffer to work on.
	 * @param threadName
	 *            How to name the thread.
	 */
	public BufferWorker(IBuffer<?> buffer, String threadName) {
		this.buffer = buffer;
		setName(threadName);
		setDaemon(true);
		setPriority(MAX_PRIORITY);
	}

	/**
	 * Defines the work to be done on the buffer.
	 * 
	 * @throws InterruptedException
	 *             {@link InterruptedException}
	 */
	public abstract void work() throws InterruptedException;

	/**
	 * Returns buffer that worker is working on.
	 * 
	 * @return Buffer.
	 */
	protected IBuffer<?> getBuffer() {
		return buffer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		while (true) {
			// if we go again to the run and we are interrupted we will break;
			if (Thread.currentThread().isInterrupted()) {
				break;
			}

			try {
				work();
			} catch (InterruptedException interruptedException) {
				// we should never be interrupted, because we don't use this mechanism.. if it
				// happens, we preserve evidence that the interruption happened for the code higher
				// up, that can figure it out if it wants..
				Thread.currentThread().interrupt();
			}
		}
	}
}
