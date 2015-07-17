package info.novatec.inspectit.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.storage.StorageWriter.WriteTask;
import info.novatec.inspectit.storage.util.StorageIndexTreeProvider;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class StorageIndexingTreeHandlerTest {

	private StorageIndexingTreeHandler indexingTreeHandler;

	@Mock
	private StorageWriter storageWriter;

	@Mock
	private StorageIndexTreeProvider<DefaultData> storageIndexTreeProvider;

	@Mock
	private ScheduledExecutorService executorService;

	@Mock
	private IObjectSizes objectSizes;

	@Mock
	private IStorageTreeComponent<DefaultData> indexingTree;

	@SuppressWarnings("rawtypes")
	@Mock
	private ScheduledFuture future;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		indexingTreeHandler = new StorageIndexingTreeHandler();
		indexingTreeHandler.registerStorageWriter(storageWriter);
		indexingTreeHandler.executorService = executorService;
		indexingTreeHandler.objectSizes = objectSizes;
		indexingTreeHandler.storageIndexTreeProvider = storageIndexTreeProvider;
		when(storageIndexTreeProvider.getStorageIndexingTree()).thenReturn(indexingTree);
		when(executorService.scheduleWithFixedDelay(Mockito.<Runnable> anyObject(), anyLong(), anyLong(), Mockito.<TimeUnit> anyObject())).thenReturn(future);
		indexingTreeHandler.prepare();
	}

	@Test(expectedExceptions = { IndexingException.class })
	public void writeTaskWithoutData() throws IndexingException {
		indexingTreeHandler.startWrite(storageWriter.new WriteTask(null, null));
	}

	@Test(expectedExceptions = { IndexingException.class })
	public void noDescriptorFromTree() throws IndexingException {
		DefaultData defaultData = mock(DefaultData.class);
		when(indexingTree.put(defaultData)).thenReturn(null);
		indexingTreeHandler.startWrite(storageWriter.new WriteTask(defaultData, null));
	}

	@Test
	public void successfulWrite() throws IndexingException {
		IStorageDescriptor storageDescriptor = mock(IStorageDescriptor.class);
		when(storageDescriptor.getChannelId()).thenReturn(1);
		DefaultData defaultData = mock(DefaultData.class);
		when(indexingTree.put(defaultData)).thenReturn(storageDescriptor);
		WriteTask writeTask = mock(WriteTask.class);
		when(writeTask.getData()).thenReturn(defaultData);

		int channel = indexingTreeHandler.startWrite(writeTask);

		assertThat(channel, is(1));
		assertThat(indexingTreeHandler.getWriteTaskInProgressCount(), is(1));

		long position = 20L;
		long size = 30L;
		indexingTreeHandler.writeSuccessful(writeTask, position, size);

		assertThat(indexingTreeHandler.getWriteTaskInProgressCount(), is(0));
		verify(storageDescriptor, times(1)).setPositionAndSize(position, size);
	}

	@Test
	public void failedWrite() throws IndexingException {
		IStorageDescriptor storageDescriptor = mock(IStorageDescriptor.class);
		when(storageDescriptor.getChannelId()).thenReturn(1);
		DefaultData defaultData = mock(DefaultData.class);
		when(indexingTree.put(defaultData)).thenReturn(storageDescriptor);
		WriteTask writeTask = mock(WriteTask.class);
		when(writeTask.getData()).thenReturn(defaultData);

		int channel = indexingTreeHandler.startWrite(writeTask);

		assertThat(channel, is(1));
		assertThat(indexingTreeHandler.getWriteTaskInProgressCount(), is(1));

		indexingTreeHandler.writeFailed(writeTask);

		assertThat(indexingTreeHandler.getWriteTaskInProgressCount(), is(0));
		verify(storageDescriptor, times(0)).setPositionAndSize(anyLong(), anyLong());
	}

	@Test
	public void treeWrittenOnFinish() {
		when(future.isDone()).thenReturn(true);
		when(storageWriter.writeNonDefaultDataObject(eq(indexingTree), anyString())).thenReturn(true);
		indexingTreeHandler.finish();
		verify(storageWriter, times(1)).writeNonDefaultDataObject(eq(indexingTree), anyString());
	}

	@Test
	public void indexingTreeSavingTask() {
		reset(executorService);
		indexingTreeHandler.maximumIndexingTreeSize = 10L;
		when(indexingTree.getComponentSize(objectSizes)).thenReturn(5L);

		indexingTreeHandler.new IndexingTreeSavingTask().run();
		verifyZeroInteractions(executorService);

		when(indexingTree.getComponentSize(objectSizes)).thenReturn(15L);
		indexingTreeHandler.new IndexingTreeSavingTask().run();
		ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
		verify(executorService, times(1)).submit(captor.capture());

		when(storageWriter.writeNonDefaultDataObject(eq(indexingTree), anyString())).thenReturn(true);
		captor.getValue().run();
		verify(storageWriter, times(1)).writeNonDefaultDataObject(eq(indexingTree), anyString());
	}
}
