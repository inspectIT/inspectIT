package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.communication.DefaultData;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Abstract class for indexing and analyzing processing.
 * 
 * @param <E>
 *            Type of data to process.
 * 
 * @author Ivan Senic
 * 
 */
abstract class AbstractBufferElementProcessor<E extends DefaultData> {

	/**
	 * {@link AtomicBuffer} to work on.
	 */
	protected final AtomicBuffer<E> atomicBuffer;

	/**
	 * Reference to the last processed element.
	 */
	protected AtomicReference<IBufferElement<E>> lastProcessed;

	/**
	 * Lock to use during operation.
	 */
	private Lock lock;

	/**
	 * Condition to wait on when there s nothing to process.
	 */
	private Condition condition;

	/**
	 * Default constructor.
	 * 
	 * @param atomicBuffer
	 *            {@link AtomicBuffer} to work on.
	 * @param lastProcessed
	 *            Reference to the last processed element.
	 * @param lock
	 *            Lock to use during operation.
	 * @param condition
	 *            Condition to wait on when there s nothing to process.
	 */
	public AbstractBufferElementProcessor(AtomicBuffer<E> atomicBuffer, AtomicReference<IBufferElement<E>> lastProcessed, Lock lock, Condition condition) {
		this.atomicBuffer = atomicBuffer;
		this.lastProcessed = lastProcessed;
		this.lock = lock;
		this.condition = condition;
	}

	/**
	 * Processes next element to be processed. Note that this method passes the element to the
	 * {@link #process(IBufferElement, IBufferElement)} method so that sub-classes can execute the
	 * real processing. This method handles waiting of element to be available for processing.
	 * 
	 * @throws InterruptedException
	 *             If {@link InterruptedException} occurs.
	 */
	public void process() throws InterruptedException {
		// wait until there are elements to process
		// we wait if:
		// 1) queue is empty -> last points to empty element
		// 2) all are analyzed -> last to process is not empty element, but points to the empty
		// one
		while (true) {
			IBufferElement<E> lastProcessedElement = lastProcessed.get();
			if (this.atomicBuffer.emptyBufferElement == this.atomicBuffer.last.get()
					|| (this.atomicBuffer.emptyBufferElement != lastProcessedElement && this.atomicBuffer.emptyBufferElement == lastProcessedElement.getNextElement())) { // NOPMD
				lock.lock();
				try {
					// check again with lock
					lastProcessedElement = this.atomicBuffer.lastAnalyzed.get();
					if (this.atomicBuffer.emptyBufferElement == this.atomicBuffer.last.get()
							|| (this.atomicBuffer.emptyBufferElement != lastProcessedElement && this.atomicBuffer.emptyBufferElement == lastProcessedElement.getNextElement())) { // NOPMD
						condition.await();
					} else {
						break;
					}
				} finally {
					lock.unlock();
				}
			} else {
				break;
			}
		}

		while (true) {
			this.atomicBuffer.clearReadLock.lock();
			try {
				IBufferElement<E> elementToProcess = null;
				IBufferElement<E> lastProcessElement = lastProcessed.get();
				// if last analyzed points to empty then we take the first added element
				if (this.atomicBuffer.emptyBufferElement == lastProcessElement) { // NOPMD
					elementToProcess = this.atomicBuffer.last.get();
				} else {
					elementToProcess = lastProcessElement.getNextElement();
				}

				// if there is nothing to analyze any more break
				if (this.atomicBuffer.emptyBufferElement == elementToProcess) { // NOPMD
					break;
				}

				if (process(elementToProcess, lastProcessElement)) {
					break;
				}
			} finally {
				this.atomicBuffer.clearReadLock.unlock();
			}
		}
	}

	/**
	 * Sub-classes should implement this method with the real processing.
	 * 
	 * @param elementToProcess
	 *            Element to be processed.
	 * @param lastProcessedElement
	 *            Last successfully processed element.
	 * @return This method should return <code>true</code> if element was processed and
	 *         <code>false</code> otherwise.
	 */
	public abstract boolean process(IBufferElement<E> elementToProcess, IBufferElement<E> lastProcessedElement);

}