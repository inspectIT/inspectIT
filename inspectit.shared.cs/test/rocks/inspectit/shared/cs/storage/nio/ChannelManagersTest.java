package info.novatec.inspectit.storage.nio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.storage.nio.read.ReadingChannelManager;
import info.novatec.inspectit.storage.nio.stream.ExtendedByteBufferOutputStream;
import info.novatec.inspectit.storage.nio.write.WritingChannelManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.mockito.Mockito;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test's if the data wrote by a {@link WritingChannelManager} is readable by
 * {@link ReadingChannelManager}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class ChannelManagersTest {

	/**
	 * File where data will be written/read.
	 */
	private final Path file = Paths.get("test/testFile.");

	/**
	 * Write manager.
	 */
	private WritingChannelManager writingChannelManager;

	/**
	 * Read manager.
	 */
	private ReadingChannelManager readingChannelManager;

	/**
	 * Initializes the channel managers that will use default executor service.
	 */
	@BeforeClass
	public void initChannelManagers() {
		writingChannelManager = new WritingChannelManager();
		readingChannelManager = new ReadingChannelManager();
	}

	/**
	 * Tests if writing and then reading the set of fixed sizes bytes will be correct.
	 * 
	 * @throws IOException
	 *             With {@link IOException}.
	 * @throws InterruptedException
	 *             With {@link InterruptedException}.
	 */
	@Test(invocationCount = 10)
	public void writeReadFixedSize() throws IOException, InterruptedException {
		final LinkedBlockingQueue<ByteBuffer> bufferQueue = new LinkedBlockingQueue<ByteBuffer>();

		byte[] bytes = getRandomByteArray();
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
		byteBuffer.put(bytes);

		byteBuffer.flip();
		long position = writingChannelManager.write(byteBuffer, file, new WriteReadCompletionRunnable() {
			@Override
			public void run() {
				byteBuffer.clear();
				bufferQueue.add(byteBuffer);
			}
		});

		final ByteBuffer readBuffer = bufferQueue.take();
		readingChannelManager.read(readBuffer, position, bytes.length, file, new WriteReadCompletionRunnable() {

			@Override
			public void run() {
				bufferQueue.add(readBuffer);
			}
		});

		byte[] readBytes = new byte[bytes.length];
		bufferQueue.take().get(readBytes);
		assertThat(readBytes, is(equalTo(bytes)));

		readingChannelManager.finalizeChannel(file);
		writingChannelManager.finalizeChannel(file);
	}

	/**
	 * Tests if writing and then reading the set of unknown bytes size will be correct.
	 * 
	 * @throws IOException
	 *             With {@link IOException}.
	 * @throws InterruptedException
	 *             With {@link InterruptedException}.
	 */
	@Test(invocationCount = 10)
	public void writeReadUnknownSize() throws IOException, InterruptedException {
		final LinkedBlockingQueue<ByteBuffer> bufferQueue = new LinkedBlockingQueue<ByteBuffer>();

		byte[] bytes = getRandomByteArray();
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
		byteBuffer.put(bytes);

		byteBuffer.flip();
		long position = writingChannelManager.write(byteBuffer, file, new WriteReadCompletionRunnable() {
			@Override
			public void run() {
				byteBuffer.clear();
				bufferQueue.add(byteBuffer);
			}
		});

		final ByteBuffer readBuffer = bufferQueue.take();
		readingChannelManager.read(readBuffer, position, 0, file, new WriteReadCompletionRunnable() {

			@Override
			public void run() {
				bufferQueue.add(readBuffer);
			}
		});

		byte[] readBytes = new byte[bytes.length];
		bufferQueue.take().get(readBytes);
		assertThat(readBytes, is(equalTo(bytes)));

		writingChannelManager.finalizeChannel(file);
		readingChannelManager.finalizeChannel(file);
	}

	/**
	 * Test the write in the
	 * {@link WritingChannelManager#write(ExtendedByteBufferOutputStream, Path, WriteReadCompletionRunnable)}
	 * . {@link ExtendedByteBufferOutputStream} is mocked.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 * @throws InterruptedException
	 *             If thread is interruped.
	 */
	@Test(invocationCount = 10)
	public void writeWithExtendedByteBufferOutputStream() throws IOException, InterruptedException {
		final byte[] bytes = getRandomByteArray();
		int bufferSize = 1024 * 1024;
		List<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
		for (int position = 0; position < bytes.length - 1;) {
			int size = Math.min(bufferSize, bytes.length - position);
			ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
			buffer.put(bytes, position, size);
			buffer.flip();
			buffers.add(buffer);
			position += size;
		}

		final Lock lock = new ReentrantLock();
		final Condition condition = lock.newCondition();

		ExtendedByteBufferOutputStream outputStream = Mockito.mock(ExtendedByteBufferOutputStream.class);
		Mockito.when(outputStream.getAllByteBuffers()).thenReturn(buffers);
		Mockito.when(outputStream.getTotalWriteSize()).thenReturn((long) bytes.length);
		long position = writingChannelManager.write(outputStream, file, new WriteReadCompletionRunnable(buffers.size()) {
			@Override
			public void run() {
				assertThat(isCompleted(), is(true));
				assertThat(getAttemptedWriteReadSize(), is(equalTo((long) bytes.length)));
				lock.lock();
				try {
					condition.signal();
				} finally {
					lock.unlock();
				}
			}
		});

		lock.lock();
		try {
			condition.await(30, TimeUnit.SECONDS);
		} finally {
			lock.unlock();
		}

		InputStream is = Files.newInputStream(file, StandardOpenOption.READ);
		is.skip(position);
		byte[] actual = new byte[bytes.length];
		is.read(actual);
		assertThat(actual, is(equalTo(bytes)));
	}

	/**
	 * Deletes the created file.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@AfterTest
	public void deleteFile() throws IOException {
		if (Files.exists(file)) {
			// make sure file is delete-able
			assertThat(Files.deleteIfExists(file), is(true));
		}
	}

	/**
	 * Random size byte array.
	 * 
	 * @return Random size byte array.
	 */
	private static byte[] getRandomByteArray() {
		Random random = new Random();
		// max 10MB
		int length = random.nextInt(10 * 1024 * 1024);
		byte[] array = new byte[length];
		random.nextBytes(array);
		return array;
	}
}
