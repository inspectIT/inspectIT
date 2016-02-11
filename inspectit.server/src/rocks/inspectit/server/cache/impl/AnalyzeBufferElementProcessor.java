package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.cache.IBufferElement.BufferElementState;
import info.novatec.inspectit.communication.DefaultData;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Analyze processor. Performs analyzing of element sizes in the buffer.
 * 
 * @param <E>
 *            Type of data to process.
 * 
 * @author Ivan Senic
 * 
 */
class AnalyzeBufferElementProcessor<E extends DefaultData> extends AbstractBufferElementProcessor<E> {

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
	public AnalyzeBufferElementProcessor(AtomicBuffer<E> atomicBuffer, AtomicReference<IBufferElement<E>> lastProcessed, Lock lock, Condition condition) {
		super(atomicBuffer, lastProcessed, lock, condition);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean process(IBufferElement<E> elementToProcess, IBufferElement<E> lastProcessedElement) {
		// only thread that execute compare and set successfully can perform changes
		if (lastProcessed.compareAndSet(lastProcessedElement, elementToProcess)) {
			// perform analysis
			elementToProcess.calculateAndSetBufferElementSize(atomicBuffer.objectSizes);
			elementToProcess.setBufferElementState(BufferElementState.ANALYZED);
			atomicBuffer.addToCurrentSize(elementToProcess.getBufferElementSize(), true);
			atomicBuffer.elementsAnalyzed.incrementAndGet();
			return true;
		}

		return false;
	}

}