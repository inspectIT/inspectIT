package info.novatec.inspectit.storage.nio;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.storage.nio.read.ReadingCompletionHandler;
import info.novatec.inspectit.storage.nio.write.WritingCompletionHandler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.Random;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class CompletionHandlersTest {

	private WritingCompletionHandler writingCompletionHandler = new WritingCompletionHandler();

	private ReadingCompletionHandler readingCompletionHandler = new ReadingCompletionHandler();

	@Mock
	private WriteReadAttachment attachment;

	@Mock
	private ByteBuffer byteBuffer;

	@Mock
	private WriteReadCompletionRunnable writeReadCompletionRunnable;

	@Mock
	private AsynchronousFileChannel channel;

	@BeforeMethod
	public void initMock() {
		MockitoAnnotations.initMocks(this);
		when(attachment.getCompletionRunnable()).thenReturn(writeReadCompletionRunnable);
		when(attachment.getFileChannel()).thenReturn(channel);
		when(attachment.getByteBuffer()).thenReturn(byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFailedNotCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		Exception exception = new Exception();
		when(writeReadCompletionRunnable.isFinished()).thenReturn(false);
		handler.failed(exception, attachment);

		verify(attachment).getCompletionRunnable();
		verify(writeReadCompletionRunnable).isFinished();
		verify(writeReadCompletionRunnable).markFailed();

		verifyNoMoreInteractions(writeReadCompletionRunnable);
		verifyZeroInteractions(channel, byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFailedCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		Exception exception = new Exception();
		when(writeReadCompletionRunnable.isFinished()).thenReturn(true);
		handler.failed(exception, attachment);

		verify(attachment).getCompletionRunnable();
		verify(writeReadCompletionRunnable).isFinished();
		verify(writeReadCompletionRunnable).markFailed();
		verify(writeReadCompletionRunnable).run();

		verifyNoMoreInteractions(writeReadCompletionRunnable);
		verifyZeroInteractions(channel, byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFinishedNotCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		int size = 50;
		Integer bytesRead = Integer.valueOf(size);
		when(attachment.getPosition()).thenReturn(0L);
		when(attachment.getSize()).thenReturn((long) size);

		when(writeReadCompletionRunnable.isFinished()).thenReturn(false);
		handler.completed(bytesRead, attachment);

		verify(attachment).getCompletionRunnable();
		verify(writeReadCompletionRunnable).isFinished();
		verify(writeReadCompletionRunnable).markSuccess();

		verifyNoMoreInteractions(writeReadCompletionRunnable);
		verifyZeroInteractions(channel, byteBuffer);
	}

	@Test(dataProvider = "Handlers")
	public void writeReadFinishedCompleted(CompletionHandler<Integer, WriteReadAttachment> handler) {
		int size = 50;
		Integer bytesRead = Integer.valueOf(size);
		when(attachment.getPosition()).thenReturn(0L);
		when(attachment.getSize()).thenReturn((long) size);

		when(writeReadCompletionRunnable.isFinished()).thenReturn(true);
		handler.completed(bytesRead, attachment);

		verify(attachment).getCompletionRunnable();
		verify(writeReadCompletionRunnable).isFinished();
		verify(writeReadCompletionRunnable).markSuccess();
		verify(writeReadCompletionRunnable).run();

		verifyNoMoreInteractions(writeReadCompletionRunnable);
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

		when(writeReadCompletionRunnable.isFinished()).thenReturn(false);
		writingCompletionHandler.completed(bytesRead, attachment);

		verify(attachment).setPosition(wantedPosition + written);
		verify(attachment).setSize(wantedSize - written);
		verify(channel, times(1)).write(byteBuffer, wantedPosition + written, attachment, writingCompletionHandler);

		verifyZeroInteractions(writeReadCompletionRunnable, byteBuffer);
	}

	@Test(dataProvider = "Write-Read-Data-Provider")
	public void hasMoreToRead(int read, long wantedPosition, long wantedSize) {
		Integer bytesRead = Integer.valueOf(read);
		when(attachment.getPosition()).thenReturn(wantedPosition);
		when(attachment.getSize()).thenReturn(wantedSize);

		when(writeReadCompletionRunnable.isFinished()).thenReturn(false);
		readingCompletionHandler.completed(bytesRead, attachment);

		verify(attachment).setPosition(wantedPosition + read);
		verify(attachment).setSize(wantedSize - read);
		verify(channel, times(1)).read(byteBuffer, wantedPosition + read, attachment, readingCompletionHandler);

		verifyZeroInteractions(writeReadCompletionRunnable, byteBuffer);
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
