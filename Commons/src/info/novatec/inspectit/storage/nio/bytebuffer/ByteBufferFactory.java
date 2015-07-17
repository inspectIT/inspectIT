package info.novatec.inspectit.storage.nio.bytebuffer;

import java.nio.ByteBuffer;

import org.apache.commons.pool.PoolableObjectFactory;

/**
 * {@link PoolableObjectFactory} for {@link ByteBuffer}s that will be used for IO.
 * 
 * @author Ivan Senic
 * 
 */
public class ByteBufferFactory implements PoolableObjectFactory<ByteBuffer> {

	/**
	 * Capacity of each buffer that will be made.
	 */
	private int bufferCapacity;

	/**
	 * Default constructor.
	 * 
	 * @param bufferCapacity
	 *            Capacity of each buffer that is created from this factory.
	 */
	public ByteBufferFactory(int bufferCapacity) {
		this.bufferCapacity = bufferCapacity;
	}

	/**
	 * {@inheritDoc}
	 */
	public ByteBuffer makeObject() throws Exception {
		return ByteBuffer.allocateDirect(bufferCapacity);
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroyObject(ByteBuffer buffer) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean validateObject(ByteBuffer buffer) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void activateObject(ByteBuffer buffer) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	public void passivateObject(ByteBuffer buffer) throws Exception {
		buffer.clear();
	}

	/**
	 * Gets {@link #bufferCapacity}.
	 * 
	 * @return {@link #bufferCapacity}
	 */
	public int getBufferCapacity() {
		return bufferCapacity;
	}

	/**
	 * Sets {@link #bufferCapacity}.
	 * 
	 * @param bufferCapacity
	 *            New value for {@link #bufferCapacity}
	 */
	public void setBufferCapacity(int bufferCapacity) {
		this.bufferCapacity = bufferCapacity;
	}

}
