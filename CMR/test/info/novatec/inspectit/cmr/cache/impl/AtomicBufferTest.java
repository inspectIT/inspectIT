package info.novatec.inspectit.cmr.cache.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.cache.IBufferElement;
import info.novatec.inspectit.cmr.cache.IObjectSizes;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;

import java.util.Random;
import java.util.concurrent.ExecutorService;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing of the functionality of the {@link AtomicBuffer}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class AtomicBufferTest extends AbstractTestNGLogSupport {

	/**
	 * Class under test.
	 */
	private AtomicBuffer<DefaultData> buffer;

	@Mock
	private BufferProperties bufferProperties;

	@Mock
	private IObjectSizes objectSizes;

	@Mock
	private IBufferTreeComponent<DefaultData> indexingTree;

	/**
	 * Init.
	 * 
	 * @throws Exception
	 */
	@BeforeMethod
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		buffer = new AtomicBuffer<>();
		buffer.bufferProperties = bufferProperties;
		buffer.objectSizes = objectSizes;
		buffer.indexingTree = indexingTree;
		buffer.log = LoggerFactory.getLogger(AtomicBuffer.class);
		when(bufferProperties.getIndexingTreeCleaningThreads()).thenReturn(1);
		buffer.postConstruct();
	}

	/**
	 * Test that insertion will be in order.
	 */
	@Test
	public void insertElements() {
		DefaultData defaultData = mock(DefaultData.class);
		IBufferElement<DefaultData> element1 = new BufferElement<DefaultData>(defaultData);
		IBufferElement<DefaultData> element2 = new BufferElement<DefaultData>(defaultData);

		buffer.put(element1);
		buffer.put(element2);

		assertThat(buffer.getInsertedElemenets(), is(2L));
		assertThat(element1.getNextElement(), is(equalTo(element2)));
	}

	/**
	 * Tests that eviction will remove right amount of elements.
	 * 
	 * @throws Exception
	 */
	@Test(invocationCount = 5)
	public void eviction() throws Exception {
		Random random = new Random();
		long elements = 1 + random.nextInt(10000);
		elements += elements % 2;
		int analyzers = 1 + random.nextInt(3);

		// evict half of the buffer
		when(bufferProperties.getInitialBufferSize()).thenReturn(elements);
		when(bufferProperties.getEvictionOccupancyPercentage()).thenReturn(0.1f);
		when(bufferProperties.getEvictionFragmentSizePercentage()).thenReturn(0.5f);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		BufferAnalyzer[] analyzerArray = new BufferAnalyzer[analyzers];
		for (int i = 0; i < analyzers; i++) {
			BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
			bufferAnalyzer.start();
			analyzerArray[i] = bufferAnalyzer;
		}

		for (int i = 0; i < elements; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait to be analyzed
		while (buffer.getAnalyzedElements() < elements) {
			Thread.sleep(50);
		}

		buffer.evict();

		for (BufferAnalyzer bufferAnalyzer : analyzerArray) {
			bufferAnalyzer.interrupt();
		}

		assertThat(buffer.getCurrentSize(), is(elements / 2));
		assertThat(buffer.getInsertedElemenets(), is(elements));
		assertThat(buffer.getEvictedElemenets(), is(elements / 2));
	}

	/**
	 * Tests that size of the elements is correctly analyzed and added to the buffer size.
	 * 
	 * @throws Exception
	 */
	@Test(invocationCount = 5)
	public void analysisAndSize() throws Exception {
		Random random = new Random();
		// tests needs at least three elements
		long elements = 3 + random.nextInt(10000);
		elements += elements % 2;
		int analyzers = 1 + random.nextInt(3);

		// eviction needed when 99% of the buffer is full
		when(bufferProperties.getInitialBufferSize()).thenReturn(elements);
		when(bufferProperties.getEvictionOccupancyPercentage()).thenReturn(0.99f);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		// start analyzers
		BufferAnalyzer[] analyzerArray = new BufferAnalyzer[analyzers];
		for (int i = 0; i < analyzers; i++) {
			BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
			bufferAnalyzer.start();
			analyzerArray[i] = bufferAnalyzer;
		}

		IBufferElement<DefaultData> first = null;
		long firstRunElements = (long) (elements * 0.99f) - 1;
		for (int i = 0; i < firstRunElements; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			if (0 == i) {
				first = bufferElement;
			}
			buffer.put(bufferElement);
		}

		// wait to be analyzed
		while (buffer.getAnalyzedElements() < firstRunElements) {
			Thread.sleep(50);
		}

		assertThat(buffer.getCurrentSize(), is(firstRunElements));
		assertThat(buffer.getOccupancyPercentage(), is((float) firstRunElements / elements));
		assertThat(buffer.shouldEvict(), is(false));

		// add rest for activating eviction
		for (int i = 0; i < elements - firstRunElements; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait to be analyzed
		while (buffer.getAnalyzedElements() < elements) {
			Thread.sleep(50);
		}

		// interrupt analyzers
		for (BufferAnalyzer bufferAnalyzer : analyzerArray) {
			bufferAnalyzer.interrupt();
		}

		assertThat(buffer.getCurrentSize(), is(elements));
		assertThat(buffer.getOccupancyPercentage(), is(1f));
		assertThat(buffer.shouldEvict(), is(true));

		assertThat(buffer.getAnalyzedElements(), is(elements));
		for (int i = 0; i < elements; i++) {
			assertThat(first.isAnalyzed(), is(true));
			first = first.getNextElement();
		}
	}

	/**
	 * Tests that expansion rate will be used on elements size.
	 * 
	 * @throws Exception
	 */
	@Test(invocationCount = 5)
	public void analysisAndSizeWithExpansionRate() throws Exception {
		Random random = new Random();
		long elements = 1 + random.nextInt(10000);
		elements += elements % 2;
		int analyzers = 1 + random.nextInt(3);

		float expansionRate = 0.1f;
		long elementSize = 10;
		when(objectSizes.getObjectSecurityExpansionRate()).thenReturn(expansionRate);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(elementSize);

		// start analyzers
		BufferAnalyzer[] analyzerArray = new BufferAnalyzer[analyzers];
		for (int i = 0; i < analyzers; i++) {
			BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
			bufferAnalyzer.start();
			analyzerArray[i] = bufferAnalyzer;
		}

		for (int i = 0; i < elements; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait to be analyzed
		while (buffer.getAnalyzedElements() < elements) {
			Thread.sleep(50);
		}

		// interrupt analyzers
		for (BufferAnalyzer bufferAnalyzer : analyzerArray) {
			bufferAnalyzer.interrupt();
		}

		assertThat(buffer.getCurrentSize(), is((long) (elements * elementSize * (1 + expansionRate))));
	}

	/**
	 * Test that elements are correctly indexed.
	 * 
	 * @throws Exception
	 */
	@Test(invocationCount = 5)
	public void indexing() throws Exception {
		Random random = new Random();
		long elements = 1 + random.nextInt(10000);
		elements += elements % 2;
		int indexers = 1 + random.nextInt(3);

		when(bufferProperties.getIndexingWaitTime()).thenReturn(10L);

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		// start analyzer
		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();

		// start indexers
		BufferIndexer[] indexerArray = new BufferIndexer[indexers];
		for (int i = 0; i < indexers; i++) {
			BufferIndexer bufferIndexer = new BufferIndexer(buffer);
			bufferIndexer.start();
			indexerArray[i] = bufferIndexer;
		}

		IBufferElement<DefaultData> first = null;

		for (int i = 0; i < elements; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			if (0 == i) {
				first = bufferElement;
			}
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed and indexed
		while (buffer.getAnalyzedElements() < elements || buffer.getIndexedElements() < elements) {
			Thread.sleep(50);
		}

		// interrupt workers
		bufferAnalyzer.interrupt();
		for (BufferIndexer bufferIndexer : indexerArray) {
			bufferIndexer.interrupt();
		}

		for (int i = 0; i < elements; i++) {
			assertThat(first.isIndexed(), is(true));
			first = first.getNextElement();
		}

		assertThat(buffer.getIndexedElements(), is(elements));
		verify(indexingTree, times((int) elements)).put(defaultData);
	}

	/**
	 * Tests that the tree size calculations and maintenance is done.
	 * 
	 * @throws Exception
	 */
	@Test(invocationCount = 5)
	public void indexingTreeMaintenance() throws Exception {
		Random random = new Random();
		long flagsSetOnBytes = 30L;
		long elements = 1 + random.nextInt(10000);

		// when adding 30 bytes, maintenance should be done
		// indexing tree always reports 10 bytes size
		when(bufferProperties.getInitialBufferSize()).thenReturn(elements);
		when(bufferProperties.getEvictionOccupancyPercentage()).thenReturn(0.5f);
		when(bufferProperties.getEvictionFragmentSizePercentage()).thenReturn(0.35f);
		when(bufferProperties.getFlagsSetOnBytes(anyLong())).thenReturn(flagsSetOnBytes);
		when(bufferProperties.getIndexingWaitTime()).thenReturn(10L);
		when(indexingTree.getComponentSize(objectSizes)).thenReturn(10L);
		buffer.postConstruct();

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();

		BufferIndexer bufferIndexer = new BufferIndexer(buffer);
		bufferIndexer.start();

		for (int i = 0; i < elements; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed and indexed
		while (buffer.getAnalyzedElements() < elements || buffer.getIndexedElements() < elements) {
			Thread.sleep(50);
		}

		if (elements > flagsSetOnBytes) {
			assertThat(buffer.getCurrentSize(), is(elements + 10L));
			verify(indexingTree, atLeast(1)).getComponentSize(objectSizes);
		} else {
			assertThat(buffer.getCurrentSize(), is(elements));
			verify(indexingTree, times(0)).getComponentSize(objectSizes);
		}

		// evict
		assertThat(buffer.shouldEvict(), is(true));
		buffer.evict();
		long evicted = buffer.getEvictedElemenets();

		// now add two more element to active the cleaning of the indexing tree
		// the cleaning should be done after adding the first one, so we can execute the verify
		// afterwards
		buffer.put(new BufferElement<DefaultData>(defaultData));
		buffer.put(new BufferElement<DefaultData>(defaultData));

		// wait for the element to be analyzed and indexed
		while (buffer.getAnalyzedElements() < elements + 2 || buffer.getIndexedElements() < elements + 2) {
			Thread.sleep(50);
		}

		bufferAnalyzer.interrupt();
		bufferIndexer.interrupt();

		// only if we evicted enough we can expect the clean
		if (evicted > flagsSetOnBytes) {
			verify(indexingTree, times(1)).cleanWithRunnable(Mockito.<ExecutorService> anyObject());
		} else {
			verify(indexingTree, times(0)).cleanWithRunnable(Mockito.<ExecutorService> anyObject());
		}
	}

	/**
	 * Tests that the clean works properly.
	 * 
	 * @throws Exception
	 */
	@Test(invocationCount = 5)
	public void clean() throws Exception {
		Random random = new Random();
		long elements = 1 + random.nextInt(10000);

		when(bufferProperties.getInitialBufferSize()).thenReturn(elements);
		when(bufferProperties.getIndexingWaitTime()).thenReturn(5L);

		DefaultData defaultData = mock(DefaultData.class);
		when(defaultData.getObjectSize(objectSizes)).thenReturn(1L);

		BufferAnalyzer bufferAnalyzer = new BufferAnalyzer(buffer);
		bufferAnalyzer.start();

		BufferIndexer bufferIndexer = new BufferIndexer(buffer);
		bufferIndexer.start();

		for (int i = 0; i < elements / 2; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
			buffer.put(bufferElement);

			// execute clear all the time, let s push this all threads
			buffer.clearAll();
		}

		for (int i = 0; i < elements; i++) {
			IBufferElement<DefaultData> bufferElement = new BufferElement<DefaultData>(defaultData);
			buffer.put(bufferElement);
		}

		// wait for the elements to be analyzed and indexed
		while (buffer.getAnalyzedElements() < elements || buffer.getIndexedElements() < elements) {
			Thread.sleep(500);
		}

		bufferAnalyzer.interrupt();
		bufferIndexer.interrupt();

		assertThat(buffer.getCurrentSize(), is(elements));
		assertThat(buffer.getAnalyzedElements(), is(elements));
		assertThat(buffer.getIndexedElements(), is(elements));
		assertThat(buffer.getEvictedElemenets(), is(0L));
	}
}
