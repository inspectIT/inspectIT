package rocks.inspectit.server.cache.impl;

import rocks.inspectit.server.cache.IBufferElement;
import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.DefaultData;

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
	@Override
	public E getObject() {
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getBufferElementSize() {
		return bufferElementSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBufferElementSize(long size) {
		this.bufferElementSize = size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IBufferElement<E> getNextElement() {
		return nextElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNextElement(IBufferElement<E> element) {
		this.nextElement = element;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
	public boolean isAnalyzed() {
		return bufferElementState.compareTo(BufferElementState.ANALYZED) >= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEvicted() {
		return bufferElementState.compareTo(BufferElementState.EVICTED) >= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isIndexed() {
		return bufferElementState.compareTo(BufferElementState.INDEXED) >= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferElementState getBufferElementState() {
		return bufferElementState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBufferElementState(BufferElementState bufferElementState) {
		this.bufferElementState = bufferElementState;
	}

}