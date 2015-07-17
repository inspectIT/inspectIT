package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBuffer;
import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.cache.IBufferElement.BufferElementState;
import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.property.spring.PropertyUpdate;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.spring.logger.Log;

import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Buffer uses atomic variables and references to handle the synchronization. Thus, non of its
 * methods is synchronized, nor synchronized block were used. However, the whole buffer is thread
 * safe.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Parameterized type of elements buffer can hold.
 */
@Component
public class AtomicBuffer<E extends DefaultData> implements IBuffer<E> {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * Buffer properties.
	 */
	@Autowired
	BufferProperties bufferProperties;

	/**
	 * Correct interface for calculating object sizes.
	 */
	@Autowired
	IObjectSizes objectSizes;

	/**
	 * Indexing tree where the elements will be indexed.
	 */
	@Autowired
	IBufferTreeComponent<E> indexingTree;

	/**
	 * Atomic reference to the first object.
	 */
	private AtomicReference<IBufferElement<E>> first;

	/**
	 * Max size of the buffer in atomic long.
	 */
	private AtomicLong maxSize;

	/**
	 * Eviction occupancy percentage. The value triggers the eviction when the occupancy of the
	 * buffer is greater than it. Although it is a float value, atomic integer is used via
	 * {@link Float#intBitsToFloat(int)} and {@link Float#floatToIntBits(float)} methods.
	 */
	private AtomicInteger evictionOccupancyPercentage;

	/**
	 * Current size of the buffer in atomic long.
	 */
	private AtomicLong currentSize = new AtomicLong();

	/**
	 * Number of elements added to the buffer.
	 */
	private AtomicLong elementsAdded = new AtomicLong();

	/**
	 * Atomic reference to the last object.
	 */
	AtomicReference<IBufferElement<E>> last;

	/**
	 * Number of elements evicted from the buffer.
	 */
	private AtomicLong elementsEvicted = new AtomicLong();

	/**
	 * Number of elements that where indexed into indexing tree.
	 */
	AtomicLong elementsIndexed = new AtomicLong();

	/**
	 * Number of elements that where analyzed.
	 */
	AtomicLong elementsAnalyzed = new AtomicLong();

	/**
	 * Atomic reference to the object that was analyzed last.
	 */
	AtomicReference<IBufferElement<E>> lastAnalyzed;

	/**
	 * Eviction lock.
	 */
	private ReentrantLock evictLock = new ReentrantLock();

	/**
	 * Condition that states that there is nothing to evict currently.
	 */
	private Condition nothingToEvict = evictLock.newCondition();

	/**
	 * Analyze lock.
	 */
	private ReentrantLock analyzeLock = new ReentrantLock();

	/**
	 * Condition that states that there is nothing to analyze currently.
	 */
	private Condition nothingToAnalyze = analyzeLock.newCondition();

	/**
	 * Indexing lock.
	 */
	private ReentrantLock indexingLock = new ReentrantLock();

	/**
	 * Condition that states that there is nothing to index currently.
	 */
	private Condition nothingToIndex = indexingLock.newCondition();

	/**
	 * Atomic reference to the object that was indexed last.
	 */
	private AtomicReference<IBufferElement<E>> lastIndexed;

	/**
	 * Size of the indexing tree.
	 */
	AtomicLong indexingTreeSize = new AtomicLong();

	/**
	 * Executor service for cleaning the indexing tree.
	 */
	ExecutorService indexingTreeCleaningExecutorService;

	/**
	 * Data added to the buffer in bytes.
	 */
	AtomicLong dataAddedInBytes = new AtomicLong();

	/**
	 * Data removed from the buffer in bytes.
	 */
	AtomicLong dataRemovedInBytes = new AtomicLong();

	/**
	 * Marker for empty buffer element.
	 */
	EmptyBufferElement emptyBufferElement = new EmptyBufferElement();

	/**
	 * Amount on bytes flags for tree clean and size update will be set.
	 */
	volatile long flagsSetOnBytes;

	/**
	 * This is the read lock that has to be acquired when the size of the buffer or indexing tree is
	 * updated.
	 */
	Lock clearReadLock;

	/**
	 * This is the write lock and has to be acquired when buffer is cleared. This means that during
	 * the clear of the buffer all operations will be suspended.
	 */
	private Lock clearWriteLock;

	/**
	 * {@link AnalyzeBufferElementProcessor} that will analyze the size of the elements.
	 */
	private AnalyzeBufferElementProcessor<E> analyzeProcessor;

	/**
	 * {@link IndexBufferElementProcessor} that will index the elements.
	 */
	private IndexBufferElementProcessor<E> indexProcessor;

	/**
	 * Default constructor.
	 */
	public AtomicBuffer() {
		ReadWriteLock readWriteCleanLock = new ReentrantReadWriteLock();
		clearReadLock = readWriteCleanLock.readLock();
		clearWriteLock = readWriteCleanLock.writeLock();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method also set the ID of the object that buffer element is holding, thus overwriting
	 * any earlier set ID.
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void put(IBufferElement<E> element) {
		boolean informAnalyzing = false;
		boolean informIndexing = false;

		// the element that is now first has to have a empty buffer element as next one
		element.setNextElement(emptyBufferElement);

		while (true) {
			// retrieving currently first element
			IBufferElement<E> currentlyFirst = first.get();

			// only thread that successfully execute compare and set will be able to perform changes
			if (first.compareAndSet(currentlyFirst, element)) {

				// increment number of added elements
				elementsAdded.incrementAndGet();

				// if currently first is not pointing to marker, it means that we already have
				// elements in the buffer, so connect elements
				if (!emptyBufferElement.equals(currentlyFirst)) {
					currentlyFirst.setNextElement(element);
					// see if last index or analyzed points to the last added element
					// if so, inform
					informAnalyzing = currentlyFirst == lastAnalyzed.get();
					informIndexing = currentlyFirst == lastIndexed.get();
				} else {
					// otherwise this is the first element in the buffer, so set last
					// and inform both indexing and analyzing
					last.set(element);
					informAnalyzing = true;
					informIndexing = true;
				}

				// break from while
				break;
			}
		}

		if (informAnalyzing) {
			analyzeLock.lock();
			try {
				nothingToAnalyze.signal();
			} finally {
				analyzeLock.unlock();
			}
		}

		if (informIndexing) {
			indexingLock.lock();
			try {
				nothingToIndex.signal();
			} finally {
				indexingLock.unlock();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The executing thread will wait until the current occupancy percentage of the buffer is
	 * smaller than eviction occupancy percentage. This method also sets the cleaning flag after
	 * every {@value #elementsCountForMaintenance}th element evicted.
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void evict() throws InterruptedException {
		// wait until there is need for eviction
		while (!shouldEvict()) {
			evictLock.lock();
			try {
				// check again for avoiding deadlocks
				if (!shouldEvict()) {
					nothingToEvict.await();
				}
			} finally {
				evictLock.unlock();
			}
		}

		while (true) {
			clearReadLock.lock();
			try {
				// get the currently last element
				IBufferElement<E> currentLastElement = last.get();

				// check if we really have concrete elements because clear buffer can happen
				// anywhere
				if (emptyBufferElement.equals(currentLastElement)) {
					break;
				}

				// set up the values for evicting the fragment of elements
				IBufferElement<E> newLastElement = currentLastElement;
				long evictionFragmentMaxSize = (long) (this.getMaxSize() * bufferProperties.getEvictionFragmentSizePercentage());
				long fragmentSize = 0;
				int elementsInFragment = 0;

				// iterate until size of the eviction fragment is reached
				while (fragmentSize < evictionFragmentMaxSize) {
					fragmentSize += newLastElement.getBufferElementSize();
					newLastElement.setBufferElementState(BufferElementState.EVICTED);
					elementsInFragment++;
					newLastElement = newLastElement.getNextElement();

					// break if we reach the end of queue
					if (emptyBufferElement.equals(newLastElement)) {
						break;
					}
				}

				// change the last element to the right one
				// only thread that execute compare and set successfully can perform changes
				if (last.compareAndSet(currentLastElement, newLastElement)) {
					// subtract the fragment size
					substractFromCurrentSize(fragmentSize);

					// add evicted elements to the total count
					elementsEvicted.addAndGet(elementsInFragment);

					// if the last is now pointing to the empty buffer element, it means that we
					// have
					// evicted all elements, so first should also point to empty buffer element
					// this can only happen in theory
					if (emptyBufferElement == last.get()) {
						first.set(emptyBufferElement);
					}

					// break from while
					break;
				}
			} finally {
				clearReadLock.unlock();
			}
		}

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void analyzeNext() throws InterruptedException {
		analyzeProcessor.process();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method also performs the cleaning of the indexing tree if the cleaning flag is on.
	 * <p>
	 * This method is designed for multiply thread access.
	 */
	public void indexNext() throws InterruptedException {
		indexProcessor.process();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getMaxSize() {
		return maxSize.get();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 * <p>
	 * Using this method does not provide any check for the supplied new maximum size. Thus, it is
	 * responsibility of the user to assure that the given value is correct.
	 */
	public void setMaxSize(long maxSize) {
		this.maxSize.set(maxSize);
		notifyEvictionIfNeeded();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getCurrentSize() {
		return currentSize.get();
	}

	/**
	 * Sets the current size of the buffer.
	 * 
	 * @param currentSize
	 *            Size in bytes.
	 */
	public void setCurrentSize(long currentSize) {
		this.currentSize.set(currentSize);
		notifyEvictionIfNeeded();
	}

	/**
	 * Adds size value to the current size.
	 * <p>
	 * This method is thread safe.
	 * 
	 * @param size
	 *            Size in bytes.
	 * @param areObjects
	 *            Defines if the size that is added to the current size relates to the objects in
	 *            buffer. True means size is related to objects in buffer, false means that the size
	 *            relates to the indexing tree.
	 */
	void addToCurrentSize(long size, boolean areObjects) {
		currentSize.addAndGet(size);
		notifyEvictionIfNeeded();
		if (areObjects) {
			dataAddedInBytes.addAndGet(size);
		}
	}

	/**
	 * Subtracts size value from the current size.
	 * <p>
	 * This method is thread safe.
	 * 
	 * @param size
	 *            Size in bytes.
	 */
	private void substractFromCurrentSize(long size) {
		currentSize.addAndGet(-(size));
		dataRemovedInBytes.addAndGet(size);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getEvictionOccupancyPercentage() {
		return Float.intBitsToFloat(evictionOccupancyPercentage.get());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 */
	public void setEvictionOccupancyPercentage(float evictionOccupancyPercentage) {
		this.evictionOccupancyPercentage.set(Float.floatToIntBits(evictionOccupancyPercentage));
		notifyEvictionIfNeeded();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 */
	public float getOccupancyPercentage() {
		return ((float) currentSize.get()) / maxSize.get();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method is thread safe.
	 */
	public boolean shouldEvict() {
		return getOccupancyPercentage() > Float.intBitsToFloat(evictionOccupancyPercentage.get());
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearAll() {
		clearWriteLock.lock();
		try {
			last.set(emptyBufferElement);
			lastAnalyzed.set(emptyBufferElement);
			lastIndexed.set(emptyBufferElement);
			setCurrentSize(0);
			elementsAdded.set(0);
			elementsAnalyzed.set(0);
			elementsIndexed.set(0);
			elementsEvicted.set(0);
			indexingTree.clearAll();
			indexingTreeSize.set(0);
			dataAddedInBytes.set(0);
			dataRemovedInBytes.set(0);
			// reference to first has to be reset at the end
			first.set(emptyBufferElement);
		} finally {
			clearWriteLock.unlock();
		}
	}

	/**
	 * Returns the number of inserted elements since the buffer has been created.
	 * 
	 * @return Number of inserted elements.
	 */
	public long getInsertedElemenets() {
		return elementsAdded.get();
	}

	/**
	 * Returns the number of evicted elements since the buffer has been created.
	 * 
	 * @return Number of evicted elements.
	 */
	public long getEvictedElemenets() {
		return elementsEvicted.get();
	}

	/**
	 * Returns the number of indexed elements since the buffer has been created.
	 * 
	 * @return Number of indexed elements.
	 */
	public long getIndexedElements() {
		return elementsIndexed.get();
	}

	/**
	 * Returns the number of analyzed elements since the buffer has been created.
	 * 
	 * @return Number of analyzed elements.
	 */
	public long getAnalyzedElements() {
		return elementsAnalyzed.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public E getOldestElement() {
		IBufferElement<E> bufferElement = last.get();
		if (null != bufferElement) {
			return bufferElement.getObject();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public E getNewestElement() {
		IBufferElement<E> bufferElement = first.get();
		if (null != bufferElement) {
			return bufferElement.getObject();
		}
		return null;
	}

	/**
	 * Is executed after dependency injection is done to perform any initialization.
	 * 
	 * @throws Exception
	 *             if an error occurs during {@link PostConstruct}
	 */
	@PostConstruct
	public void postConstruct() throws Exception {
		this.maxSize = new AtomicLong(bufferProperties.getInitialBufferSize());
		this.evictionOccupancyPercentage = new AtomicInteger(Float.floatToIntBits(bufferProperties.getEvictionOccupancyPercentage()));
		this.objectSizes.setObjectSecurityExpansionRate(bufferProperties.getObjectSecurityExpansionRate(maxSize.get()));
		this.first = new AtomicReference<IBufferElement<E>>(emptyBufferElement);
		this.last = new AtomicReference<IBufferElement<E>>(emptyBufferElement);
		this.lastAnalyzed = new AtomicReference<IBufferElement<E>>(emptyBufferElement);
		this.lastIndexed = new AtomicReference<IBufferElement<E>>(emptyBufferElement);
		this.indexingTreeCleaningExecutorService = Executors.newFixedThreadPool(bufferProperties.getIndexingTreeCleaningThreads());
		this.flagsSetOnBytes = bufferProperties.getFlagsSetOnBytes(this.maxSize.get());

		// initialize processors
		this.analyzeProcessor = new AnalyzeBufferElementProcessor<E>(this, lastAnalyzed, analyzeLock, nothingToAnalyze);
		this.indexProcessor = new IndexBufferElementProcessor<E>(this, lastIndexed, indexingLock, nothingToIndex);

		if (log.isInfoEnabled()) {
			log.info("|-Using buffer with maximum size " + NumberFormat.getInstance().format(maxSize) + " bytes...");
			log.info("|-Indexing tree maintenance on " + NumberFormat.getInstance().format(flagsSetOnBytes) + " bytes added/removed...");
			log.info("|-Using object expansion rate of " + NumberFormat.getInstance().format(objectSizes.getObjectSecurityExpansionRate() * 100) + "%");
		}
	}

	/**
	 * Updates value of the {@link #evictionOccupancyPercentage}.
	 */
	@PropertyUpdate(properties = { "buffer.evictionOccupancyPercentage", "buffer.bytesMaintenancePercentage", })
	protected void updateEvictionOccupancyPercentage() {
		this.evictionOccupancyPercentage.set(Float.floatToIntBits(bufferProperties.getEvictionOccupancyPercentage()));
	}

	/**
	 * Updates value of the {@link #evictionOccupancyPercentage}.
	 */
	@PropertyUpdate(properties = { "buffer.bytesMaintenancePercentage", })
	protected void updateBytesMaintenancePercentage() {
		this.flagsSetOnBytes = bufferProperties.getFlagsSetOnBytes(this.maxSize.get());
	}

	/**
	 * Updates the buffer size and to it related properties.
	 */
	@PropertyUpdate(properties = { "buffer.minOldSpaceOccupancy", "buffer.maxOldSpaceOccupancy", "buffer.minOldSpaceOccupancyActiveTillOldGenSize", "buffer.maxOldSpaceOccupancyActiveFromOldGenSize" })
	protected void updateBufferSizeAndRelated() {
		this.maxSize.set(bufferProperties.getInitialBufferSize());
		this.objectSizes.setObjectSecurityExpansionRate(bufferProperties.getObjectSecurityExpansionRate(maxSize.get()));
		this.flagsSetOnBytes = bufferProperties.getFlagsSetOnBytes(this.maxSize.get());
	}

	/**
	 * Updates the object security expansion rate.
	 */
	@PropertyUpdate(properties = { "buffer.minObjectExpansionRate", "buffer.maxObjectExpansionRate", "buffer.maxObjectExpansionRateActiveTillBufferSize",
			"buffer.minObjectExpansionRateActiveFromBufferSize", "buffer.maxObjectExpansionRateActiveFromOccupancy", "buffer.minObjectExpansionRateActiveTillOccupancy" })
	protected void updateObjectSecurityExpansionRate() {
		this.objectSizes.setObjectSecurityExpansionRate(bufferProperties.getObjectSecurityExpansionRate(maxSize.get()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer(256);
		msg.append("The buffer occupancy status: ");
		msg.append(NumberFormat.getInstance().format(currentSize.get()));
		msg.append(" bytes occupied from total ");
		msg.append(NumberFormat.getInstance().format(maxSize.get()));
		msg.append(" bytes available (");
		msg.append(NumberFormat.getInstance().format(getOccupancyPercentage() * 100));
		msg.append("%).\nElements processed in the buffer since last clear buffer:\n-Elements added: ");
		msg.append(NumberFormat.getInstance().format(elementsAdded.get()));

		msg.append("\n-Elements analyzed: ");
		msg.append(NumberFormat.getInstance().format(elementsAnalyzed.get()));

		msg.append("\n-Elements indexed: ");
		msg.append(NumberFormat.getInstance().format(elementsIndexed.get()));

		msg.append("\n-Elements evicted: ");
		msg.append(NumberFormat.getInstance().format(elementsEvicted.get()));
		msg.append('\n');
		return msg.toString();
	}

	/**
	 * Checks if the eviction should start, and if it does notifies the right thread.
	 */
	private void notifyEvictionIfNeeded() {
		if (shouldEvict()) {
			evictLock.lock();
			try {
				nothingToEvict.signal();
			} finally {
				evictLock.unlock();
			}
		}
	}

	/**
	 * Class that serves as a marker for empty buffer element.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class EmptyBufferElement implements IBufferElement<E> {

		@Override
		public E getObject() {
			return null;
		}

		@Override
		public long getBufferElementSize() {
			return 0;
		}

		@Override
		public void setBufferElementSize(long size) {
		}

		@Override
		public void calculateAndSetBufferElementSize(IObjectSizes objectSizes) {
		}

		@Override
		public IBufferElement<E> getNextElement() {
			return null;
		}

		@Override
		public void setNextElement(IBufferElement<E> element) {
		}

		@Override
		public boolean isAnalyzed() {
			return false;
		}

		@Override
		public boolean isEvicted() {
			return false;
		}

		@Override
		public boolean isIndexed() {
			return false;
		}

		@Override
		public info.novatec.inspectit.cmr.cache.IBufferElement.BufferElementState getBufferElementState() {
			return null;
		}

		@Override
		public void setBufferElementState(info.novatec.inspectit.cmr.cache.IBufferElement.BufferElementState bufferElementState) {
		}

	}
}
