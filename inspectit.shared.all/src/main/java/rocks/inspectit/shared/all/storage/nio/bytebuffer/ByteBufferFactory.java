package rocks.inspectit.shared.all.storage.nio.bytebuffer;

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
	@Override
	public ByteBuffer makeObject() throws Exception {
		return ByteBuffer.allocateDirect(bufferCapacity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroyObject(ByteBuffer buffer) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean validateObject(ByteBuffer buffer) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activateObject(ByteBuffer buffer) throws Exception {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
