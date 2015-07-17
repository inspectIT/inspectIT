package info.novatec.inspectit.storage.nio.stream;

import info.novatec.inspectit.storage.nio.ByteBufferProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.io.ByteBufferOutputStream;

/**
 * This class extends the {@link ByteBufferOutputStream} in the way that it caches the
 * {@link ByteBuffer}s until the complete amount of data is written. Every call to the
 * {@link #flush()} method will change the currently full buffer with an empty one, caching the full
 * buffer to the {@link #byteBuffers} list. This way an object with unknown size can be serialized,
 * not depending on the size of the buffers, simply when one buffer is full, a new one will be
 * borrowed, till the object is serialized.
 * 
 * @author Ivan Senic
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class ExtendedByteBufferOutputStream extends ByteBufferOutputStream {

	/**
	 * {@link ByteBufferProvider}.
	 */
	@Autowired
	ByteBufferProvider byteBufferProvider;

	/**
	 * List of the ByteBuffers used.
	 */
	private List<ByteBuffer> byteBuffers = new ArrayList<ByteBuffer>();

	/**
	 * Total size of bytes that has been stored in byte buffers that are cached.
	 */
	private long totalWriteSize = 0L;

	/**
	 * If stream has been closed.
	 */
	private volatile boolean closed;

	/**
	 * {@inheritDoc}
	 * <p>
	 * Since this method is called when the buffer currently used for writing is full, the method
	 * will put the full buffer in the {@link #byteBuffers} list and prepare a fresh buffer for
	 * write.
	 */
	@Override
	public void flush() throws IOException {
		flush(true);
	}

	/**
	 * Flushes the stream by taking the current {@link ByteBuffer} using for writing. Buffer is flip
	 * and put to the list of cached buffers. If the takeNewBuffer is <code>true</code> then a new
	 * buffer will be placed and ready for write.
	 * 
	 * @param takeNewBuffer
	 *            If the new buffers should be placed the super {@link OutputStream}.
	 */
	public void flush(boolean takeNewBuffer) {
		ByteBuffer fullBuffer = super.getByteBuffer();
		fullBuffer.flip();
		byteBuffers.add(fullBuffer);
		totalWriteSize += fullBuffer.limit() - fullBuffer.position();
		if (takeNewBuffer) {
			ByteBuffer byteBuffer = byteBufferProvider.acquireByteBuffer();
			byteBuffer.clear();
			super.setByteBuffer(byteBuffer);
		} else {
			super.setByteBuffer(null);
		}
	}

	/**
	 * Prepares for the write. This method will get a {@link ByteBuffer} from the
	 * {@link ByteBufferProvider} and set it as the current output.
	 */
	public void prepare() {
		byteBuffers.clear();
		totalWriteSize = 0L;
		ByteBuffer byteBuffer = byteBufferProvider.acquireByteBuffer();
		byteBuffer.clear();
		super.setByteBuffer(byteBuffer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int remaining = super.getByteBuffer().remaining();
		if (remaining >= len) {
			super.write(b, off, len);
		} else {
			super.write(b, off, remaining);
			this.flush();
			this.write(b, off + remaining, len - remaining);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Releases all byte buffers that are hold.
	 */
	@Override
	public synchronized void close() {
		if (closed) {
			return;
		}
		for (ByteBuffer byteBuffer : byteBuffers) {
			byteBufferProvider.releaseByteBuffer(byteBuffer);
		}
		byteBuffers.clear();

		ByteBuffer currentBuffer = super.getByteBuffer();
		if (null != currentBuffer) {
			byteBufferProvider.releaseByteBuffer(currentBuffer);
			super.setByteBuffer(null);
		}
		closed = true;
	}

	/**
	 * Gets all the buffers where the write was done.
	 * 
	 * @return Gets all the buffers where the write was done.
	 */
	public List<ByteBuffer> getAllByteBuffers() {
		return new ArrayList<ByteBuffer>(byteBuffers);
	}

	/**
	 * Returns the total writing size including the size that is currently available in the
	 * ByteBuffer that is currently used for write.
	 * 
	 * @return Returns the total writing size including the size that is currently available in the
	 *         ByteBuffer that is currently used for write.
	 */
	public long getTotalWriteSize() {
		return totalWriteSize;
	}

	/**
	 * Returns the total number of {@link ByteBuffer}s used.
	 * 
	 * @return Returns the total number of {@link ByteBuffer}s used.
	 */
	public int getBuffersCount() {
		return byteBuffers.size();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Closing the stream on finalize.
	 */
	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}
}
