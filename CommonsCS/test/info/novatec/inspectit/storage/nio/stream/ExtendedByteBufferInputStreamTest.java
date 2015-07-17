package info.novatec.inspectit.storage.nio.stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageDescriptor;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageManager;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;
import info.novatec.inspectit.storage.nio.WriteReadCompletionRunnable;
import info.novatec.inspectit.storage.nio.read.ReadingChannelManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing of the {@link ExtendedByteBufferInputStream} class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class ExtendedByteBufferInputStreamTest {

	private static final int NUMBER_OF_BUFFERS = 2;

	/**
	 * Class under test.
	 */
	private ExtendedByteBufferInputStream inputStream;

	@Mock
	private ReadingChannelManager readingChannelManager;

	@Mock
	private ByteBufferProvider byteBufferProvider;

	@Mock
	private StorageManager storageManager;

	@Mock
	private StorageData storageData;

	/**
	 * Executor service needed for the handler. Can not be mocked.
	 */
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	/**
	 * Init.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		inputStream = new ExtendedByteBufferInputStream(storageData, null, NUMBER_OF_BUFFERS);
		inputStream.setByteBufferProvider(byteBufferProvider);
		inputStream.setReadingChannelManager(readingChannelManager);
		inputStream.setStorageManager(storageManager);
		inputStream.setExecutorService(executorService);
		when(storageManager.getChannelPath(eq(storageData), Mockito.<IStorageDescriptor> anyObject())).thenReturn(Paths.get("/"));
	}

	/**
	 * Tests reading of random size.
	 * 
	 * @throws IOException
	 */
	@Test(invocationCount = 50)
	public void read() throws IOException {
		Random random = new Random();
		final int readSize = random.nextInt(8096);
		final int bufferSize = 1024;
		final byte[] array = new byte[readSize];
		random.nextBytes(array);

		when(byteBufferProvider.acquireByteBuffer()).thenAnswer(new Answer<ByteBuffer>() {
			@Override
			public ByteBuffer answer(InvocationOnMock invocation) throws Throwable {
				return ByteBuffer.allocateDirect(bufferSize);
			}
		});
		IStorageDescriptor storageDescriptor = mock(StorageDescriptor.class);
		when(storageDescriptor.getPosition()).thenReturn(0L);
		when(storageDescriptor.getSize()).thenReturn((long) readSize);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				ByteBuffer byteBuffer = (ByteBuffer) args[0];
				long position = (long) args[1];
				long size = (long) args[2];
				WriteReadCompletionRunnable writeReadCompletionRunnable = (WriteReadCompletionRunnable) args[4];

				byteBuffer.put(array, (int) position, (int) size);
				byteBuffer.flip();

				writeReadCompletionRunnable.markSuccess();
				writeReadCompletionRunnable.run();

				return null;
			}
		}).when(readingChannelManager).read(Mockito.<ByteBuffer> anyObject(), anyLong(), anyLong(), Mockito.<Path> anyObject(), Mockito.<WriteReadCompletionRunnable> anyObject());

		inputStream.setDescriptors(Collections.singletonList(storageDescriptor));
		inputStream.prepare();

		byte[] bytes = new byte[readSize];
		inputStream.read(bytes, 0, readSize);
		inputStream.close();

		try {
			assertThat(bytes, is(equalTo(array)));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		verify(byteBufferProvider, times(NUMBER_OF_BUFFERS)).releaseByteBuffer(Mockito.<ByteBuffer> anyObject());
	}
}
