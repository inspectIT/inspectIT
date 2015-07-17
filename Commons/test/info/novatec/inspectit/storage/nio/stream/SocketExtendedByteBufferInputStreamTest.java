package info.novatec.inspectit.storage.nio.stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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

public class SocketExtendedByteBufferInputStreamTest {

	/**
	 * Class under test.
	 */
	private SocketExtendedByteBufferInputStream inputStream;

	@Mock
	private ByteBufferProvider byteBufferProvider;

	@Mock
	private SocketChannel socketChannel;

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
		inputStream = new SocketExtendedByteBufferInputStream();
		inputStream.setByteBufferProvider(byteBufferProvider);
		inputStream.setExecutorService(executorService);
		inputStream.setSocketChannel(socketChannel);
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
			public ByteBuffer answer(InvocationOnMock invocation) throws Throwable {
				return ByteBuffer.allocateDirect(bufferSize);
			}
		});

		when(socketChannel.read(Mockito.<ByteBuffer> any())).thenAnswer(new Answer<Integer>() {
			int readPosition = 0;

			public Integer answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				ByteBuffer byteBuffer = (ByteBuffer) args[0];

				int remaining = byteBuffer.remaining();

				int toRead = remaining > array.length - readPosition ? array.length - readPosition : remaining;
				byteBuffer.put(array, readPosition, toRead);
				int newRemaning = byteBuffer.remaining();

				readPosition += remaining - newRemaning;

				return remaining - newRemaning;
			}
		});

		inputStream.prepare();
		inputStream.reset(readSize);

		byte[] bytes = new byte[readSize];
		inputStream.read(bytes, 0, readSize);

		assertThat(bytes, is(equalTo(array)));
	}

}
