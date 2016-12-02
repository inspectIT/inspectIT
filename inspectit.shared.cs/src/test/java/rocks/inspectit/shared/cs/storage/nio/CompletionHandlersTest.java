package rocks.inspectit.shared.cs.storage.nio;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.Random;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import rocks.inspectit.shared.cs.storage.nio.read.ReadingCompletionHandler;
import rocks.inspectit.shared.cs.storage.nio.write.WritingCompletionHandler;

@SuppressWarnings("PMD")
public class CompletionHandlersTest {

	private WritingCompletionHandler writingCompletionHandler = new WritingCompletionHandler();

	private ReadingCompletionHandler readingCompletionHandler = new ReadingCompletionHandler();

	@Mock
	private WriteReadAttachment attachment;

	@Mock
	private ByteBuffer byteBuffer;

	@Mock
	private WriteReadCompletionRunnable.RunnableFuture writeReadRunnableFuture;

	@Mock
	private WriteReadCompletionRunnable writeReadRunnable;

	@Mock
	private AsynchronousFileChannel channel;

	@BeforeMethod
	public void initMock() {
		MockitoAnnotations.initMocks(this);
		when(writeReadRunnableFuture.getWriteReadCompletionRunnable()).thenReturn(writeReadRunnable);
		when(attachment.getCompletionRunnableFuture()).thenReturn(writeReadRunnableFuture);
		when(attachment.getFileChannel()).thenReturn(channel);
		when(attachment.getByteBuffer()).thenReturn(byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFailedNotCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		Exception exception = new Exception();
		when(writeReadRunnable.isFinished()).thenReturn(false);
		handler.failed(exception, attachment);

		verify(attachment).getCompletionRunnableFuture();
		verify(writeReadRunnable).isFinished();
		verify(writeReadRunnable).markFailed();
		verify(writeReadRunnableFuture, times(2)).getWriteReadCompletionRunnable();

		verifyNoMoreInteractions(writeReadRunnable, writeReadRunnableFuture);
		verifyZeroInteractions(channel, byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFailedCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		Exception exception = new Exception();
		when(writeReadRunnable.isFinished()).thenReturn(true);
		handler.failed(exception, attachment);

		verify(attachment).getCompletionRunnableFuture();
		verify(writeReadRunnable).isFinished();
		verify(writeReadRunnable).markFailed();
		verify(writeReadRunnableFuture).run();
		verify(writeReadRunnableFuture, times(2)).getWriteReadCompletionRunnable();

		verifyNoMoreInteractions(writeReadRunnable, writeReadRunnableFuture);
		verifyZeroInteractions(channel, byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFinishedNotCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		int size = 50;
		Integer bytesRead = Integer.valueOf(size);
		when(attachment.getPosition()).thenReturn(0L);
		when(attachment.getSize()).thenReturn((long) size);

		when(writeReadRunnable.isFinished()).thenReturn(false);
		handler.completed(bytesRead, attachment);

		verify(attachment).getCompletionRunnableFuture();
		verify(writeReadRunnable).isFinished();
		verify(writeReadRunnable).markSuccess();
		verify(writeReadRunnableFuture, times(2)).getWriteReadCompletionRunnable();

		verifyNoMoreInteractions(writeReadRunnable, writeReadRunnableFuture);
		verifyZeroInteractions(channel, byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFinishedCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		int size = 50;
		Integer bytesRead = Integer.valueOf(size);
		when(attachment.getPosition()).thenReturn(0L);
		when(attachment.getSize()).thenReturn((long) size);

		when(writeReadRunnable.isFinished()).thenReturn(true);
		handler.completed(bytesRead, attachment);

		verify(attachment).getCompletionRunnableFuture();
		verify(writeReadRunnable).isFinished();
		verify(writeReadRunnable).markSuccess();
		verify(writeReadRunnableFuture).run();
		verify(writeReadRunnableFuture, times(2)).getWriteReadCompletionRunnable();

		verifyNoMoreInteractions(writeReadRunnable, writeReadRunnableFuture);
		verifyZeroInteractions(channel, byteBuffer);
	}

	@DataProvider(name = "Handlers")
	public Object[][] handlerProviders() {
		return new Object[][] { { writingCompletionHandler }, { readingCompletionHandler } };
	}

	@Test(dataProvider = "Write-Read-Data-Provider")
	public void hasMoreToWrite(int written, long wantedPosition, long wantedSize) {
		Integer bytesRead = Integer.valueOf(written);
		when(attachment.getPosition()).thenReturn(wantedPosition);
		when(attachment.getSize()).thenReturn(wantedSize);

		when(writeReadRunnable.isFinished()).thenReturn(false);
		writingCompletionHandler.completed(bytesRead, attachment);

		verify(attachment).setPosition(wantedPosition + written);
		verify(attachment).setSize(wantedSize - written);
		verify(channel, times(1)).write(byteBuffer, wantedPosition + written, attachment, writingCompletionHandler);

		verifyZeroInteractions(writeReadRunnableFuture, byteBuffer);
	}

	@Test(dataProvider = "Write-Read-Data-Provider")
	public void hasMoreToRead(int read, long wantedPosition, long wantedSize) {
		Integer bytesRead = Integer.valueOf(read);
		when(attachment.getPosition()).thenReturn(wantedPosition);
		when(attachment.getSize()).thenReturn(wantedSize);

		when(writeReadRunnable.isFinished()).thenReturn(false);
		readingCompletionHandler.completed(bytesRead, attachment);

		verify(attachment).setPosition(wantedPosition + read);
		verify(attachment).setSize(wantedSize - read);
		verify(channel, times(1)).read(byteBuffer, wantedPosition + read, attachment, readingCompletionHandler);

		verifyZeroInteractions(writeReadRunnableFuture, byteBuffer);
	}

	@DataProvider(name = "Write-Read-Data-Provider")
	public Object[][] writeDataProvider() {
		Random random = new Random();
		int size = random.nextInt(25);
		Object[][] data = new Object[size][3];
		for (int i = 0; i < size; i++) {
			int written = random.nextInt(10000);
			long wantedSize = random.nextInt(100000) + written;
			long wantedPosition = random.nextInt(100000);

			data[i][0] = written;
			data[i][1] = wantedPosition;
			data[i][2] = wantedSize;
		}
		return data;
	}

}
