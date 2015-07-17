package info.novatec.inspectit.cmr.cache;

/**
 * Interface for Buffer functionality.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 *            Type of objects in buffer.
 */
public interface IBuffer<E> {

	/**
	 * Puts one {@link IBufferElement} in the buffer.
	 * 
	 * @param element
	 *            Element to be put into the buffer.
	 */
	void put(IBufferElement<E> element);

	/**
	 * Performs the eviction from the buffer. The element or elements that needs to be evicted
	 * depends on buffer implementation.
	 * 
	 * @throws InterruptedException
	 *             {@link InterruptedException}
	 */
	void evict() throws InterruptedException;

	/**
	 * Performs the size analysis of one {@link IBufferElement} in the buffer, that is next in the
	 * line for analysis. The size of the object is added to the current size of the buffer.
	 * 
	 * @throws InterruptedException
	 *             {@link InterruptedException}
	 */
	void analyzeNext() throws InterruptedException;

	/**
	 * Performs the indexing of one {@link IBufferElement} in the buffer, that is next in the line
	 * for indexing.
	 * 
	 * @throws InterruptedException
	 *             {@link InterruptedException}
	 */
	void indexNext() throws InterruptedException;

	/**
	 * Empties buffer.
	 */
	void clearAll();

	/**
	 * Returns max size of the buffer.
	 * 
	 * @return Buffer maximum size in bytes.
	 */
	long getMaxSize();

	/**
	 * Sets max size of the buffer.
	 * 
	 * @param maxSize
	 *            Maximum size for buffer in bytes.
	 */
	void setMaxSize(long maxSize);

	/**
	 * Returns current size of the buffer.
	 * 
	 * @return Current buffer size in bytes.
	 */
	long getCurrentSize();

	/**
	 * Returns the eviction occupancy percentage, which defines the occupancy percentage of the
	 * buffer when eviction of the elements should start.
	 * 
	 * @return Eviction occupancy percentage presented as a float ranging from 0 to 1.
	 */
	float getEvictionOccupancyPercentage();

	/**
	 * Sets the eviction occupancy percentage, which defines the occupancy percentage of the buffer
	 * when eviction of the elements should start.
	 * 
	 * @param evictionOccupancyPercentage
	 *            Eviction occupancy percentage presented as a float ranging from 0 to 1.
	 */
	void setEvictionOccupancyPercentage(float evictionOccupancyPercentage);

	/**
	 * Returns current occupancy percentage of the buffer.
	 * 
	 * @return Current buffer occupancy percentage presented as a float ranging from 0 to 1.
	 */
	float getOccupancyPercentage();

	/**
	 * 
	 * @return Returns the oldest element in the buffer.
	 */
	E getOldestElement();

	/**
	 * 
	 * @return Returns the newest element in the buffer.
	 */
	E getNewestElement();
}
