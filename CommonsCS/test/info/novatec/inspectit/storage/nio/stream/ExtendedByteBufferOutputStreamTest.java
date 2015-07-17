package info.novatec.inspectit.storage.nio.stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.storage.nio.ByteBufferProvider;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link ExtendedByteBufferOutputStream}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class ExtendedByteBufferOutputStreamTest {

	/**
	 * Size of buffers {@link ByteBufferProvider} will return.
	 */
	private int bufferSize = 1024;

	/**
	 * Mocked {@link ByteBufferProvider}.
	 */
	@Mock
	private ByteBufferProvider byteBufferProvider;

	/**
	 * To be tested.
	 */
	private ExtendedByteBufferOutputStream extendedByteBufferOutputStream;

	/**
	 * Init mocks.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(byteBufferProvider.acquireByteBuffer()).thenAnswer(new Answer<ByteBuffer>() {

			@Override
			public ByteBuffer answer(InvocationOnMock invocation) throws Throwable {
				return ByteBuffer.allocate(bufferSize);
			}
		});

		extendedByteBufferOutputStream = new ExtendedByteBufferOutputStream();
		extendedByteBufferOutputStream.byteBufferProvider = byteBufferProvider;
		extendedByteBufferOutputStream.prepare();
	}

	/**
	 * Test the write of the write can fit into one buffer.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@Test(invocationCount = 5)
	public void writeLessThanBufferSize() throws IOException {
		Random random = new Random();
		int writeCount = random.nextInt(bufferSize);
		byte[] array = new byte[writeCount];
		random.nextBytes(array);

		extendedByteBufferOutputStream.write(array);
		extendedByteBufferOutputStream.flush(false);

		Mockito.verify(byteBufferProvider, Mockito.times(1)).acquireByteBuffer();
		assertThat(extendedByteBufferOutputStream.getBuffersCount(), is(equalTo(1)));

		ByteBuffer buffer = extendedByteBufferOutputStream.getAllByteBuffers().get(0);
		byte[] actual = new byte[buffer.limit() - buffer.position()];
		buffer.get(actual);
		assertThat(actual, is(equalTo(array)));

		extendedByteBufferOutputStream.close();
		Mockito.verify(byteBufferProvider, Mockito.times(1)).releaseByteBuffer((ByteBuffer) Mockito.any());
	}

	/**
	 * Test the write of the write can fit into one buffer.
	 * 
	 * @throws IOException
	 *             If {@link IOException} occurs.
	 */
	@Test(invocationCount = 5)
	public void writeMoreThanBufferSize() throws IOException {
		Random random = new Random();
		int writeCount = random.nextInt(bufferSize * 5);
		byte[] array = new byte[writeCount];
		random.nextBytes(array);
		int buffersUsed = writeCount / bufferSize;
		if (writeCount % bufferSize > 0) {
			buffersUsed++;
		}

		extendedByteBufferOutputStream.write(array);
		extendedByteBufferOutputStream.flush(false);

		Mockito.verify(byteBufferProvider, Mockito.times(buffersUsed)).acquireByteBuffer();
		assertThat(extendedByteBufferOutputStream.getBuffersCount(), is(equalTo(buffersUsed)));

		byte[] actual = new byte[writeCount];
		int position = 0;
		for (ByteBuffer byteBuffer : extendedByteBufferOutputStream.getAllByteBuffers()) {
			int length = byteBuffer.limit() - byteBuffer.position();
			byteBuffer.get(actual, position, length);
			position += length;
		}
		assertThat(actual, is(equalTo(array)));

		extendedByteBufferOutputStream.close();
		Mockito.verify(byteBufferProvider, Mockito.times(buffersUsed)).releaseByteBuffer((ByteBuffer) Mockito.any());
	}

}
