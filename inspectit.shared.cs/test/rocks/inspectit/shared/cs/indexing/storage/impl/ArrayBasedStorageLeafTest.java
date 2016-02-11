package info.novatec.inspectit.indexing.storage.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.impl.IndexQuery;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.storage.IStorageDescriptor;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for {@link ArrayBasedStorageLeaf}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class ArrayBasedStorageLeafTest {

	/**
	 * ID to be assinged to the leaf.
	 */
	private static final int LEAF_ID = 1;

	/**
	 * Class under test.
	 */
	private ArrayBasedStorageLeaf<DefaultData> arrayBasedStorageLeaf;

	/**
	 * Init.
	 */
	@BeforeMethod
	public void init() {
		arrayBasedStorageLeaf = new ArrayBasedStorageLeaf<DefaultData>(LEAF_ID);
	}

	/**
	 * Get and getAndRemove on empty leaf should return <code>null</code>.
	 */
	@Test
	public void getOnEmpyLeaf() {
		DefaultData element = mock(DefaultData.class);
		when(element.getId()).thenReturn(10L);
		assertThat(arrayBasedStorageLeaf.get(element), is(nullValue()));
		assertThat(arrayBasedStorageLeaf.getAndRemove(element), is(nullValue()));
	}

	/**
	 * Indexing same element twice should raise an exception.
	 * 
	 * @throws IndexingException
	 */
	@Test(expectedExceptions = { IndexingException.class })
	public void sameElementIndexedTwice() throws IndexingException {
		DefaultData element = mock(DefaultData.class);
		when(element.getId()).thenReturn(10L);
		arrayBasedStorageLeaf.put(element);
		arrayBasedStorageLeaf.put(element);
	}

	/**
	 * With minIds we can limit the number of elements returned.
	 * 
	 * @throws IndexingException
	 */
	@Test
	public void minIdQuery() throws IndexingException {
		addElements(arrayBasedStorageLeaf, 100);
		IndexQuery indexQuery = mock(IndexQuery.class);
		when(indexQuery.getMinId()).thenReturn(51L);

		List<IStorageDescriptor> result = arrayBasedStorageLeaf.query(indexQuery);
		assertThat(result, hasSize(50));
		for (IStorageDescriptor descriptor : result) {
			assertThat(descriptor.getChannelId(), is(LEAF_ID));
		}
	}

	/**
	 * Exclude/include ID also can be define in query.
	 * 
	 * @throws IndexingException
	 */
	@Test
	public void includeExcludeIdQuery() throws IndexingException {
		addElements(arrayBasedStorageLeaf, 100);

		StorageIndexQuery indexQuery = mock(StorageIndexQuery.class);
		List<Long> includeIds = new ArrayList<Long>();
		List<Long> excludeIds = new ArrayList<Long>();
		when(indexQuery.getIncludeIds()).thenReturn(includeIds);
		when(indexQuery.getExcludeIds()).thenReturn(excludeIds);

		includeIds.add(50L);
		List<IStorageDescriptor> result = arrayBasedStorageLeaf.query(indexQuery);
		assertThat(result, hasSize(1));

		excludeIds.add(50L);
		result = arrayBasedStorageLeaf.query(indexQuery);
		assertThat(result, is(empty()));

		when(indexQuery.getIncludeIds()).thenReturn(null);
		result = arrayBasedStorageLeaf.query(indexQuery);
		assertThat(result, hasSize(99));

		when(indexQuery.getMinId()).thenReturn(100L);
		result = arrayBasedStorageLeaf.query(indexQuery);
		assertThat(result, hasSize(1));
	}

	/**
	 * Adds wanted amount of elements to the leaf.
	 * 
	 * @param treeComponent
	 *            {@link IStorageTreeComponent}.
	 * @param amount
	 *            Amount of elements to add.
	 * @throws IndexingException
	 */
	private void addElements(IStorageTreeComponent<DefaultData> treeComponent, int amount) throws IndexingException {
		DefaultData element = mock(DefaultData.class);
		for (int i = 1; i <= amount; i++) {
			when(element.getId()).thenReturn((long) i);
			treeComponent.put(element);
		}

	}
}
