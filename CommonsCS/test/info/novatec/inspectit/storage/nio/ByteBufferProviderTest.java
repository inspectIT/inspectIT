package info.novatec.inspectit.storage.nio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import info.novatec.inspectit.storage.nio.bytebuffer.ByteBufferFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link ByteBufferProvider}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ByteBufferProviderTest {

	/**
	 * To be tested.
	 */
	private ByteBufferProvider byteBufferProvider;

	/**
	 * Creates new instance before each test.
	 */
	@BeforeMethod
	public void init() {
		byteBufferProvider = new ByteBufferProvider(new ByteBufferFactory(1), 1);
		byteBufferProvider.setBufferPoolMaxDirectMemoryOccupancy(0.6f);
		byteBufferProvider.setBufferPoolMinDirectMemoryOccupancy(0.3f);
	}

	/**
	 * Test that the created capacity of the buffer will be as wanted.
	 */
	@Test(invocationCount = 5)
	public void capacity() throws IOException {
		int maxCapacity = 1000;
		Random random = new Random();
		// at least 1
		int wantedCapacity = 1 + random.nextInt(maxCapacity);
		byteBufferProvider.setBufferSize(wantedCapacity);
		byteBufferProvider.setPoolMaxCapacity(maxCapacity);
		byteBufferProvider.init();
		ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
		assertThat(buffer, is(notNullValue()));
		assertThat(buffer.capacity(), is(equalTo(wantedCapacity)));
	}

	/**
	 * Tests that no buffer will be created after max pool capacity has been reached.
	 */
	@Test
	public void creationUntilMax() throws IOException {
		int maxCapacity = 3;
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(maxCapacity);
		byteBufferProvider.init();
		for (int i = 0; i < maxCapacity; i++) {
			ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
			assertThat(buffer, is(notNullValue()));
		}
		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(0)));
	}

	/**
	 * Tests that a buffer will not be returned to the queue after a release when the available
	 * capacity is above or equal to min capacity.
	 */
	@Test
	public void relaseAfterMin() throws IOException {
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(3);
		byteBufferProvider.setPoolMinCapacity(1);
		byteBufferProvider.init();

		ByteBuffer buffer1 = byteBufferProvider.acquireByteBuffer();
		ByteBuffer buffer2 = byteBufferProvider.acquireByteBuffer();
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(2L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(0L)));

		byteBufferProvider.releaseByteBuffer(buffer1);
		byteBufferProvider.releaseByteBuffer(buffer2);
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(1L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(1L)));

		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(1)));
	}

	/**
	 * Tests that acquire and release of the buffer will have the correct side effects.
	 */
	@Test
	public void acquireAndRelease() throws IOException {
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(2);
		byteBufferProvider.setPoolMinCapacity(1);
		byteBufferProvider.init();

		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(0L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(0L)));

		ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
		assertThat(buffer, is(notNullValue()));
		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(0)));
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(1L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(0L)));

		byteBufferProvider.releaseByteBuffer(buffer);
		assertThat(byteBufferProvider.getBufferPoolSize(), is(equalTo(1)));
		assertThat(byteBufferProvider.getCreatedCapacity(), is(equalTo(1L)));
		assertThat(byteBufferProvider.getAvailableCapacity(), is(equalTo(1L)));
	}

	/**
	 * Test that IOException will be thrown when there is no buffer available any more.
	 */
	@Test(expectedExceptions = IOException.class)
	public void bufferNotAvailable() throws IOException {
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(1);
		byteBufferProvider.setPoolMinCapacity(0);
		byteBufferProvider.init();

		// first acquire to work
		ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
		assertThat(buffer, is(notNullValue()));

		// second to fail
		buffer = byteBufferProvider.acquireByteBuffer();
	}

	/**
	 * Stress the provider with several thread proving that the byte buffer provider won't block
	 * under heavy load.
	 */
	@Test
	public void providerStressed() throws Throwable {
		byteBufferProvider.setBufferSize(1);
		byteBufferProvider.setPoolMaxCapacity(3);
		byteBufferProvider.setPoolMinCapacity(1);
		byteBufferProvider.init();

		int threadCount = 5;
		final int iterationsPerThread = 100000;
		final AtomicInteger totalCount = new AtomicInteger(threadCount * iterationsPerThread);

		for (int i = 0; i < threadCount; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < iterationsPerThread; i++) {
						try {
							ByteBuffer buffer = byteBufferProvider.acquireByteBuffer();
							assertThat(buffer, is(notNullValue()));
							byteBufferProvider.releaseByteBuffer(buffer);
							totalCount.decrementAndGet();
						} catch (IOException ioException) {
							// if IOException occurs we will repeat the try
							i--;
						}
					}
				}
			}).start();
		}

		int sleepTime = 500;
		int totalSlept = 0;
		int maxSleepTime = 2 * 60 * 1000;
		while (totalCount.get() > 0) {
			try {
				Thread.sleep(sleepTime);
				totalSlept += sleepTime;
				if (totalSlept > maxSleepTime) {
					Assert.fail("Waiting for the byte buffer stressed test is over " + maxSleepTime + " milliseconds. Test is failed.");
				}
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
	}
}
