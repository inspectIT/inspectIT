package info.novatec.inspectit.storage.nio;

import info.novatec.inspectit.cmr.property.spring.PropertyUpdate;
import info.novatec.inspectit.spring.logger.Log;
import info.novatec.inspectit.storage.nio.bytebuffer.ByteBufferFactory;
import info.novatec.inspectit.util.UnderlyingSystemInfo;
import info.novatec.inspectit.util.UnderlyingSystemInfo.JvmProvider;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This component enables reusing of the {@link ByteBuffer}s. It creates a set of initial buffers
 * that are available for "rent". The amount of buffers created at the beginning is going to be
 * enough to satisfies {@link #poolMinCapacity}. The total amount of buffers created will never go
 * above {@link #poolMaxCapacity}.
 * <P>
 * This class does not have protection against not returning a buffer, thus all components using the
 * provider have to ensure they will return the buffer to the provider.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ByteBufferProvider extends GenericObjectPool<ByteBuffer> implements InitializingBean {

	/**
	 * The log of this class.
	 */
	@Log
	Logger log;

	/**
	 * Default buffer size that will be used if not initial buffer list size is provided to create
	 * one buffer.
	 */
	public static final int DEFAULT_BUFFER_CAPACITY = 1024 * 1024;

	/**
	 * Max waiting for the buffer to be available in milliseconds.
	 */
	private static final long MAX_WAIT = 60000;

	/**
	 * Buffer size.
	 */
	@Value(value = "${storage.bufferSize}")
	private int bufferSize = DEFAULT_BUFFER_CAPACITY;

	/**
	 * Pool factory.
	 */
	private ByteBufferFactory poolFactory;

	/**
	 * Amount of bytes this pool should have at minimum.
	 */
	@Value(value = "${storage.bufferPoolMinCapacity}")
	private long poolMinCapacity;

	/**
	 * Amount of bytes this pool can acquire in total.
	 */
	@Value(value = "${storage.bufferPoolMaxCapacity}")
	private long poolMaxCapacity;

	/**
	 * Min size based on the percentage of the direct memory available to the JVM.
	 */
	@Value(value = "${storage.bufferPoolMinDirectMemoryOccupancy}")
	private float bufferPoolMinDirectMemoryOccupancy;

	/**
	 * Min size based on the percentage of the direct memory available to the JVM.
	 */
	@Value(value = "${storage.bufferPoolMaxDirectMemoryOccupancy}")
	private float bufferPoolMaxDirectMemoryOccupancy;

	/**
	 * Default constructor.
	 */
	public ByteBufferProvider() {
		this(new ByteBufferFactory(DEFAULT_BUFFER_CAPACITY));
	}

	/**
	 * @param poolFactory
	 *            Pool factory to be used.
	 */
	protected ByteBufferProvider(ByteBufferFactory poolFactory) {
		super(poolFactory);
		this.poolFactory = poolFactory;
		this.setMaxWait(MAX_WAIT);
		this.setWhenExhaustedAction(WHEN_EXHAUSTED_BLOCK);
	}

	/**
	 * Returns buffer from the pool. This method waits for buffer to be available or creates a new
	 * buffer if the {@link #createdCapacity} is less than {@link #poolMaxCapacity}.
	 * 
	 * @return {@link ByteBuffer}.
	 */
	public ByteBuffer acquireByteBuffer() {
		try {
			return super.borrowObject();
		} catch (Exception e) {
			log.error("Byte buffer pool can not borrow a valid byte buffer.", e);
			return null;
		}
	}

	/**
	 * Gives back the {@link ByteBuffer} to the pool, so that others can use it.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} to put back to the pool.
	 * @throws InterruptedException
	 */
	public void releaseByteBuffer(ByteBuffer byteBuffer) {
		try {
			super.returnObject(byteBuffer);
		} catch (Exception e) {
			log.error("Byte buffer can not be returned to the byte buffer pool.", e);
			return;
		}
	}

	/**
	 * @return Returns the pool size.
	 */
	public int getBufferPoolSize() {
		return super.getNumIdle();
	}

	/**
	 * Gets {@link #createdCapacity}.
	 * 
	 * @return {@link #createdCapacity}
	 */
	public long getCreatedCapacity() {
		return (super.getNumActive() + super.getNumIdle()) * bufferSize;
	}

	/**
	 * Gets {@link #availableCapacity}.
	 * 
	 * @return {@link #availableCapacity}
	 */
	public long getAvailableCapacity() {
		return super.getNumIdle() * bufferSize;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 * 
	 * @param poolMaxCapacity
	 *            the poolMaxCapacity to set
	 */
	public void setPoolMaxCapacity(long poolMaxCapacity) {
		this.poolMaxCapacity = poolMaxCapacity;
	}

	/**
	 * Sets {@link #poolMinCapacity}.
	 * 
	 * @param poolMinCapacity
	 *            New value for {@link #poolMinCapacity}
	 */
	public void setPoolMinCapacity(long poolMinCapacity) {
		this.poolMinCapacity = poolMinCapacity;
	}

	/**
	 * Sets {@link #bufferPoolMinDirectMemoryOccupancy}.
	 * 
	 * @param bufferPoolMinDirectMemoryOccupancy
	 *            New value for {@link #bufferPoolMinDirectMemoryOccupancy}
	 */
	public void setBufferPoolMinDirectMemoryOccupancy(float bufferPoolMinDirectMemoryOccupancy) {
		this.bufferPoolMinDirectMemoryOccupancy = bufferPoolMinDirectMemoryOccupancy;
	}

	/**
	 * Sets {@link #bufferPoolMaxDirectMemoryOccupancy}.
	 * 
	 * @param bufferPoolMaxDirectMemoryOccupancy
	 *            New value for {@link #bufferPoolMaxDirectMemoryOccupancy}
	 */
	public void setBufferPoolMaxDirectMemoryOccupancy(float bufferPoolMaxDirectMemoryOccupancy) {
		this.bufferPoolMaxDirectMemoryOccupancy = bufferPoolMaxDirectMemoryOccupancy;
	}

	/**
	 * Sets {@link #bufferSize}.
	 * 
	 * @param bufferSize
	 *            New value for {@link #bufferSize}
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Initializes the pool.
	 */
	protected void init() {
		if (bufferPoolMinDirectMemoryOccupancy > bufferPoolMaxDirectMemoryOccupancy) {
			throw new BeanInitializationException("Settings for the byte buffer pool are not correct. bufferPoolMinDirectMemoryOccupancy (" + bufferPoolMaxDirectMemoryOccupancy
					+ ") is greater than bufferPoolMaxDirectMemoryOccupancy (" + bufferPoolMinDirectMemoryOccupancy + ")");
		}
		if (poolMinCapacity > poolMaxCapacity) {
			throw new BeanInitializationException("Settings for the byte buffer pool are not correct. poolMinCapacity (" + poolMinCapacity + ") is greater than poolMaxCapacity (" + poolMaxCapacity
					+ ")");
		}

		updatePoolProperties();
	}

	/**
	 * Updates the buffer capacity, min & max idle buffers based on the current properties.
	 * <p>
	 * This is an automated properties update execution method.
	 */
	@PropertyUpdate(properties = { "storage.bufferSize", "storage.bufferPoolMinCapacity", "storage.bufferPoolMaxCapacity", "storage.bufferPoolMinDirectMemoryOccupancy",
			"storage.bufferPoolMaxDirectMemoryOccupancy" })
	protected void updatePoolProperties() {
		// assume that the maxDirect memory is 64MB
		long maxDirectMemory = 64 * 1024 * 1024;
		try {
			if (UnderlyingSystemInfo.JVM_PROVIDER.equals(JvmProvider.SUN) || UnderlyingSystemInfo.JVM_PROVIDER.equals(JvmProvider.ORACLE)) {
				Class<?> vmClazz = Class.forName("sun.misc.VM");
				Method directMemoryMethod = vmClazz.getMethod("maxDirectMemory");
				directMemoryMethod.setAccessible(true);
				maxDirectMemory = (Long) directMemoryMethod.invoke(null);
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception occurred trying to use the class sun.misc.VM via reflection", e);
			}
		}

		if (poolMinCapacity > (long) (maxDirectMemory * bufferPoolMinDirectMemoryOccupancy)) {
			poolMinCapacity = (long) (maxDirectMemory * bufferPoolMinDirectMemoryOccupancy);
		}
		if (poolMaxCapacity > (long) (maxDirectMemory * bufferPoolMaxDirectMemoryOccupancy)) {
			poolMaxCapacity = (long) (maxDirectMemory * bufferPoolMaxDirectMemoryOccupancy);
		}
		poolFactory.setBufferCapacity(bufferSize);
		int maxIdle = (int) (poolMinCapacity / bufferSize);
		int maxActive = (int) (poolMaxCapacity / bufferSize);
		super.setMaxIdle(maxIdle);
		super.setMaxActive(maxActive);
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		init();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("poolMinCapacity", poolMinCapacity);
		toStringBuilder.append("poolMaxCapacity", poolMaxCapacity);
		return toStringBuilder.toString();
	}

}
