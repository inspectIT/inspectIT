package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.cache.IBufferElement.BufferElementState;
import info.novatec.inspectit.cmr.util.Converter;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.impl.IndexingException;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Index processor. Performs indexing of the elements, update of indexing tree size and cleaning the
 * indexing tree.
 * 
 * @param <E>
 *            Type of data to process.
 * 
 * @author Ivan Senic
 * 
 */
class IndexBufferElementProcessor<E extends DefaultData> extends AbstractBufferElementProcessor<E> {

	/**
	 * Default constructor.
	 * 
	 * @param atomicBuffer
	 *            {@link AtomicBuffer} to work on.
	 * 
	 * @param lastProcessed
	 *            Reference to the last processed element.
	 * @param lock
	 *            Lock to use during operation.
	 * @param condition
	 *            Condition to wait on when there s nothing to process.
	 */
	public IndexBufferElementProcessor(AtomicBuffer<E> atomicBuffer, AtomicReference<IBufferElement<E>> lastProcessed, Lock lock, Condition condition) {
		super(atomicBuffer, lastProcessed, lock, condition);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * After element processing we check if tree cleaning is needed.
	 */
	@Override
	public void process() throws InterruptedException {
		super.process();

		// if clean flag is set thread should try to perform indexing tree cleaning
		// only thread that successfully executes the compare and set will do the cleaning
		while (true) {
			long dataRemoveInBytesCurrent = atomicBuffer.dataRemovedInBytes.get();
			if (dataRemoveInBytesCurrent > atomicBuffer.flagsSetOnBytes) {
				if (atomicBuffer.dataRemovedInBytes.compareAndSet(dataRemoveInBytesCurrent, 0)) {
					long time = 0;
					if (atomicBuffer.log.isDebugEnabled()) {
						time = System.nanoTime();
					}

					atomicBuffer.indexingTree.cleanWithRunnable(atomicBuffer.indexingTreeCleaningExecutorService);

					if (atomicBuffer.log.isDebugEnabled()) {
						atomicBuffer.log.debug("Indexing tree cleaning duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
					}
					break;
				}
			} else {
				break;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * We wait until element is analyzed to index it.
	 * <p>
	 * After successful indexing we check if update of indexing tree size is needed and if so update
	 * it.
	 * 
	 */
	@Override
	public boolean process(IBufferElement<E> elementToProcess, IBufferElement<E> lastProcessedElement) {
		// we only index when the element has already been analyzed
		if (!elementToProcess.isAnalyzed()) {
			try {
				Thread.sleep(atomicBuffer.bufferProperties.getIndexingWaitTime());
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
			// we go back to the while loop, because we want to check if the nextForIndexing
			// element has changed
			return false;
		}

		// only thread that execute compare and set successfully can perform changes
		if (lastProcessed.compareAndSet(lastProcessedElement, elementToProcess)) {
			try {
				// index element
				atomicBuffer.indexingTree.put(elementToProcess.getObject());
				elementToProcess.setBufferElementState(BufferElementState.INDEXED);

				// increase number of indexed elements, and perform calculation of the
				// indexing tree size if enough elements have been indexed
				atomicBuffer.elementsIndexed.incrementAndGet();

				long dataAddedInBytesCurrent = atomicBuffer.dataAddedInBytes.get();
				if (dataAddedInBytesCurrent > atomicBuffer.flagsSetOnBytes) {
					if (atomicBuffer.dataAddedInBytes.compareAndSet(dataAddedInBytesCurrent, 0)) {
						long time = 0;
						if (atomicBuffer.log.isDebugEnabled()) {
							time = System.nanoTime();
						}
						while (true) {
							// calculation of new size has to be repeated if old size
							// compare and set fails
							long newSize = atomicBuffer.indexingTree.getComponentSize(atomicBuffer.objectSizes);
							newSize += newSize * atomicBuffer.objectSizes.getObjectSecurityExpansionRate();
							long oldSize = atomicBuffer.indexingTreeSize.get();
							if (atomicBuffer.indexingTreeSize.compareAndSet(oldSize, newSize)) {
								atomicBuffer.addToCurrentSize(newSize - oldSize, false);
								if (atomicBuffer.log.isDebugEnabled()) {
									atomicBuffer.log.debug("Indexing tree size update duration: " + Converter.nanoToMilliseconds(System.nanoTime() - time));
									atomicBuffer.log.debug("Indexing tree delta: " + (newSize - oldSize));
									atomicBuffer.log.debug("Indexing tree new size: " + newSize);
								}
								break;
							}
						}
					}
				}
			} catch (IndexingException e) {
				// indexing exception should not happen
				atomicBuffer.log.error(e.getMessage(), e);
			}
			return true;
		}

		return false;
	}

}