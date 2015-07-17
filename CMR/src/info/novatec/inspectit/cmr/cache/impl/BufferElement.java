package info.novatec.inspectit.cmr.cache.impl;

import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;

/**
 * Simple implementation of the {@link IBufferElement} interface.
 * 
 * @author Ivan Senic
 * 
 * @param <E>
 */
public class BufferElement<E extends DefaultData> implements IBufferElement<E> {

	/**
	 * Element that is next element in the buffer from the perspective of this buffer element.
	 */
	private IBufferElement<E> nextElement;

	/**
	 * Holding object.
	 */
	private E object;

	/**
	 * Size of the whole buffer element.
	 */
	private long bufferElementSize;

	/**
	 * Buffer element state.
	 */
	private BufferElementState bufferElementState;

	/**
	 * Default constructor.
	 * 
	 * @param object
	 *            Object to hold.
	 */
	public BufferElement(E object) {
		super();
		this.object = object;
		this.bufferElementState = BufferElementState.INSERTED;
	}

	/**
	 * {@inheritDoc}
	 */
	public E getObject() {
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getBufferElementSize() {
		return bufferElementSize;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBufferElementSize(long size) {
		this.bufferElementSize = size;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBufferElement<E> getNextElement() {
		return nextElement;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNextElement(IBufferElement<E> element) {
		this.nextElement = element;
	}

	/**
	 * {@inheritDoc}
	 */
	public void calculateAndSetBufferElementSize(IObjectSizes objectSizes) {
		long size = objectSizes.getSizeOfObjectHeader() + objectSizes.getPrimitiveTypesSize(2, 0, 0, 0, 1, 0);
		if (null != object) {
			size += object.getObjectSize(objectSizes);
		}
		size += size * objectSizes.getObjectSecurityExpansionRate();
		bufferElementSize = size;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isAnalyzed() {
		return bufferElementState.compareTo(BufferElementState.ANALYZED) >= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEvicted() {
		return bufferElementState.compareTo(BufferElementState.EVICTED) >= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isIndexed() {
		return bufferElementState.compareTo(BufferElementState.INDEXED) >= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public BufferElementState getBufferElementState() {
		return bufferElementState;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBufferElementState(BufferElementState bufferElementState) {
		this.bufferElementState = bufferElementState;
	}

}