package rocks.inspectit.shared.cs.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.io.Output;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.serializer.ISerializer;
import rocks.inspectit.shared.all.serializer.SerializationException;
import rocks.inspectit.shared.all.storage.nio.stream.ExtendedByteBufferOutputStream;
import rocks.inspectit.shared.all.storage.nio.stream.StreamProvider;
import rocks.inspectit.shared.cs.indexing.impl.IndexingException;
import rocks.inspectit.shared.cs.storage.StorageWriter.WriteTask;
import rocks.inspectit.shared.cs.storage.nio.WriteReadCompletionRunnable;
import rocks.inspectit.shared.cs.storage.nio.write.WritingChannelManager;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;
import rocks.inspectit.shared.cs.storage.processor.write.AbstractWriteDataProcessor;
import rocks.inspectit.shared.cs.storage.util.DeleteFileVisitor;

@SuppressWarnings("PMD")
public class StorageWriterTest {

	private StorageWriter storageWriter;

	@Mock
	private ISerializer serializer;

	@Mock
	private StreamProvider streamProvider;

	@Mock
	private ExtendedByteBufferOutputStream extendedByteBufferOutputStream;

	@Mock
	private WritingChannelManager writingChannelManager;

	@Mock
	private StorageIndexingTreeHandler storageIndexingTreeHandler;

	@Mock
	private StorageManager storageManager;

	@Mock
	private BlockingQueue<ISerializer> serializerQueue;

	@Mock
	private ScheduledExecutorService scheduledExecutorService;

	@Mock
	private AbstractWriteDataProcessor writeDataProcessor;

	@SuppressWarnings("rawtypes")
	@Mock
	private ScheduledFuture future;

	private Path testPath = Paths.get("myTestPath" + File.separator);

	@SuppressWarnings({ "unchecked" })
	@BeforeMethod
	public void init() throws IndexingException, InterruptedException, IOException {
		MockitoAnnotations.initMocks(this);
		storageWriter = new StorageWriter();
		when(streamProvider.getExtendedByteBufferOutputStream()).thenReturn(extendedByteBufferOutputStream);
		when(storageIndexingTreeHandler.startWrite(Matchers.<WriteTask> anyObject())).thenReturn(1);
		when(storageManager.canWriteMore()).thenReturn(true);
		when(storageManager.getChannelPath(Matchers.<IStorageData> anyObject(), anyInt())).thenReturn(Paths.get("test"));
		when(serializerQueue.take()).thenReturn(serializer);
		when(scheduledExecutorService.scheduleWithFixedDelay(Matchers.<Runnable> anyObject(), anyLong(), anyLong(), Matchers.<TimeUnit> anyObject())).thenReturn(future);
		storageWriter.indexingTreeHandler = storageIndexingTreeHandler;
		storageWriter.storageManager = storageManager;
		storageWriter.writingChannelManager = writingChannelManager;
		storageWriter.streamProvider = streamProvider;
		storageWriter.serializerQueue = serializerQueue;
		storageWriter.scheduledExecutorService = scheduledExecutorService;
		storageWriter.writeDataProcessors = Collections.singletonList(writeDataProcessor);
		storageWriter.log = LoggerFactory.getLogger(storageWriter.getClass());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void nonSyncProcessing() {
		DefaultData defaultData = mock(DefaultData.class);
		AbstractDataProcessor dataProcessor = mock(AbstractDataProcessor.class);
		Future<Void> future1 = mock(Future.class);
		Future<Void> future2 = mock(Future.class);
		when(dataProcessor.process(defaultData)).thenReturn(Collections.singletonList(future1));
		when(dataProcessor.flush()).thenReturn(Collections.singletonList(future2));

		Collection<Future<Void>> futures = storageWriter.process(Collections.singletonList(defaultData), Collections.singletonList(dataProcessor));

		verify(dataProcessor, times(1)).setStorageWriter(storageWriter);
		verify(dataProcessor, times(1)).process(defaultData);
		verify(dataProcessor, times(1)).flush();
		verify(dataProcessor, times(1)).setStorageWriter(null);

		assertThat(futures, hasSize(2));
		assertThat(futures, hasItem(future1));
		assertThat(futures, hasItem(future2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void syncProcessing() {
		DefaultData defaultData = mock(DefaultData.class);
		AbstractDataProcessor dataProcessor = mock(AbstractDataProcessor.class);
		Future<Void> future1 = mock(Future.class);
		when(dataProcessor.process(defaultData)).thenReturn(Collections.singletonList(future1));
		when(future1.isDone()).thenReturn(true);

		storageWriter.process(Collections.singletonList(defaultData), Collections.singletonList(dataProcessor));

		verify(dataProcessor, times(1)).setStorageWriter(storageWriter);
		verify(dataProcessor, times(1)).process(defaultData);
		verify(dataProcessor, times(1)).flush();
		verify(dataProcessor, times(1)).setStorageWriter(null);
	}

	@Test
	public void writeTaskWriteNotAllowedByStorageManager() {
		when(storageManager.canWriteMore()).thenReturn(false);
		storageWriter.new WriteTask(new TimerData(), Collections.emptyMap()).run();
		verifyZeroInteractions(storageIndexingTreeHandler, extendedByteBufferOutputStream, streamProvider, serializer, serializerQueue, writingChannelManager);
	}

	@Test
	public void writeTaskFailedIndexing() throws IndexingException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(new IndexingException("Test msg")).when(storageIndexingTreeHandler).startWrite(writeTask);

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(serializer, serializerQueue, streamProvider, writingChannelManager);
	}

	@Test
	public void writeTaskZeroChannelReturnedFromIndexing() throws IndexingException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		when(storageIndexingTreeHandler.startWrite(writeTask)).thenReturn(0);

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(serializer, serializerQueue, streamProvider, writingChannelManager);
	}

	@Test
	public void writeTaskNoSerializerAvailable() throws InterruptedException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		when(serializerQueue.take()).thenReturn(null);

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void writeTaskSerializerQueueInterrupted() throws InterruptedException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(InterruptedException.class).when(serializerQueue).take();

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void writeTaskFailedSerialization() throws SerializationException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(SerializationException.class).when(serializer).serialize(anyObject(), Matchers.<Output> anyObject(), Matchers.<Map<?, ?>> anyObject());

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
		verifyZeroInteractions(writingChannelManager);
	}

	@Test
	public void writeTaskExceptionDuringWrite() throws IOException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(IOException.class).when(writingChannelManager).write(Matchers.<ExtendedByteBufferOutputStream> anyObject(), Matchers.<Path> anyObject(),
				Matchers.<WriteReadCompletionRunnable> anyObject());

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
	}

	@Test
	public void writeTaskThrowableDuringWrite() throws IOException {
		TimerData timerData = new TimerData();
		WriteTask writeTask = storageWriter.new WriteTask(timerData, Collections.emptyMap());
		doThrow(Throwable.class).when(writingChannelManager).write(Matchers.<ExtendedByteBufferOutputStream> anyObject(), Matchers.<Path> anyObject(),
				Matchers.<WriteReadCompletionRunnable> anyObject());

		writeTask.run();

		verify(storageIndexingTreeHandler, times(1)).writeFailed(writeTask);
		verify(extendedByteBufferOutputStream, times(1)).close();
		verify(serializerQueue, times(1)).add(serializer);
	}

	@Test
	public void objectWriteNoSerializerAvailable() throws InterruptedException {
		when(serializerQueue.take()).thenReturn(null);
		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void objectWriteSerializerQueueInterrupted() throws InterruptedException {
		doThrow(InterruptedException.class).when(serializerQueue).take();
		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verifyZeroInteractions(writingChannelManager, streamProvider, extendedByteBufferOutputStream);
	}

	@Test
	public void objectWriteFailedSerialization() throws Exception {
		StorageData storageData = new StorageData();
		when(storageManager.getStoragePath(storageData)).thenReturn(testPath);
		storageWriter.prepareForWrite(storageData);

		doThrow(SerializationException.class).when(serializer).serialize(anyObject(), Matchers.<Output> anyObject(), Matchers.<Map<?, ?>> anyObject());
		doThrow(SerializationException.class).when(serializer).serialize(anyObject(), Matchers.<Output> anyObject());
		storageWriter.writeNonDefaultDataObject(new Object(), "myFile");

		verify(serializerQueue, times(1)).add(serializer);
		verifyZeroInteractions(writingChannelManager, extendedByteBufferOutputStream);
	}

	@AfterTest
	public void cleanUp() throws IOException {
		if (Files.exists(testPath)) {
			Files.walkFileTree(testPath, new DeleteFileVisitor());
		}

		Files.deleteIfExists(testPath);
	}

}
