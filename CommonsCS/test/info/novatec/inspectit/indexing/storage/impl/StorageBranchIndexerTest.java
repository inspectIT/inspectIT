package info.novatec.inspectit.indexing.storage.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;
import info.novatec.inspectit.indexing.storage.IStorageBranchIndexer;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link StorageBranchIndexer} class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class StorageBranchIndexerTest {

	/**
	 * Class under test.
	 */
	private StorageBranchIndexer<DefaultData> storageBranchIndexer;

	@Mock
	private IBranchIndexer<DefaultData> delegateIndexer;

	@Mock
	private StorageBranchIndexer<DefaultData> childIndexer;

	/**
	 * Init method.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		storageBranchIndexer = new StorageBranchIndexer<DefaultData>(delegateIndexer, childIndexer, 1, true);
	}

	/**
	 * Test the creation of new component when branch should be created.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void nextComponentIsBranch() {
		int id = 1;
		storageBranchIndexer = new StorageBranchIndexer<DefaultData>(delegateIndexer, childIndexer, id, true);
		DefaultData defaultData = mock(DefaultData.class);

		// first with pass id and not shared child indexer
		when(childIndexer.isPassId()).thenReturn(false);
		when(childIndexer.getNewInstance()).thenReturn(mock(IStorageBranchIndexer.class));

		IStorageTreeComponent<DefaultData> component = storageBranchIndexer.getNextTreeComponent(defaultData);
		assertThat(component, is(instanceOf(StorageBranch.class)));
		StorageBranch<DefaultData> branch = (StorageBranch<DefaultData>) component;
		assertThat(branch.getStorageBranchIndexer(), is(not((IStorageBranchIndexer<DefaultData>) childIndexer)));
		verify(branch.getStorageBranchIndexer()).setId(id);

		// then with not pass id
		storageBranchIndexer = new StorageBranchIndexer<DefaultData>(delegateIndexer, childIndexer, id, false);

		// first with not shared child indexer
		when(childIndexer.isPassId()).thenReturn(true);
		when(childIndexer.getNewInstance()).thenReturn(mock(IStorageBranchIndexer.class));

		component = storageBranchIndexer.getNextTreeComponent(defaultData);
		assertThat(component, is(instanceOf(StorageBranch.class)));
		branch = (StorageBranch<DefaultData>) component;
		assertThat(branch.getStorageBranchIndexer(), is(not((IStorageBranchIndexer<DefaultData>) childIndexer)));
		verify(branch.getStorageBranchIndexer(), times(0)).setId(anyInt());

		// then with shared indexer
		when(childIndexer.isPassId()).thenReturn(false);
		component = storageBranchIndexer.getNextTreeComponent(defaultData);
		assertThat(component, is(instanceOf(StorageBranch.class)));
		branch = (StorageBranch<DefaultData>) component;
		assertThat(branch.getStorageBranchIndexer(), is((IStorageBranchIndexer<DefaultData>) childIndexer));
		verify(childIndexer, times(0)).setId(anyInt());
	}

	/**
	 * Test the creation of new component when leaf should be created.
	 */
	@Test
	public void nextComponentIsLeaf() {
		int id = 1;
		storageBranchIndexer = new StorageBranchIndexer<DefaultData>(delegateIndexer, null, id, true);
		DefaultData defaultData = mock(DefaultData.class);
		InvocationSequenceData invocationSequenceData = mock(InvocationSequenceData.class);

		// first pass id
		IStorageTreeComponent<DefaultData> component = storageBranchIndexer.getNextTreeComponent(defaultData);
		assertThat(component, is(instanceOf(LeafWithNoDescriptors.class)));
		LeafWithNoDescriptors<DefaultData> leaf = (LeafWithNoDescriptors<DefaultData>) component;
		assertThat(leaf.getId(), is(id));

		component = storageBranchIndexer.getNextTreeComponent(invocationSequenceData);
		assertThat(component, is(instanceOf(ArrayBasedStorageLeaf.class)));
		ArrayBasedStorageLeaf<DefaultData> arrayLeaf = (ArrayBasedStorageLeaf<DefaultData>) component;
		assertThat(arrayLeaf.getId(), is(id));

		storageBranchIndexer = new StorageBranchIndexer<DefaultData>(delegateIndexer, null, id, false);

		// then don't pass id
		component = storageBranchIndexer.getNextTreeComponent(defaultData);
		assertThat(component, is(instanceOf(LeafWithNoDescriptors.class)));
		leaf = (LeafWithNoDescriptors<DefaultData>) component;
		assertThat(leaf.getId(), is(not(id)));

		component = storageBranchIndexer.getNextTreeComponent(invocationSequenceData);
		assertThat(component, is(instanceOf(ArrayBasedStorageLeaf.class)));
		arrayLeaf = (ArrayBasedStorageLeaf<DefaultData>) component;
		assertThat(arrayLeaf.getId(), is(not(id)));
	}

	/**
	 * Test that new instance is correctly created based on the delegate indexer settings.
	 */
	@Test
	public void newInstance() {
		when(delegateIndexer.sharedInstance()).thenReturn(true);
		IStorageBranchIndexer<DefaultData> instance = storageBranchIndexer.getNewInstance();
		verify(delegateIndexer, times(0)).getNewInstance();
		assertThat(instance, is(instanceOf(StorageBranchIndexer.class)));
		assertThat(((StorageBranchIndexer<DefaultData>) instance).getDelegateIndexer(), is(delegateIndexer));

		when(delegateIndexer.sharedInstance()).thenReturn(false);
		instance = storageBranchIndexer.getNewInstance();
		verify(delegateIndexer, times(1)).getNewInstance();
		assertThat(instance, is(instanceOf(StorageBranchIndexer.class)));
		assertThat(((StorageBranchIndexer<DefaultData>) instance).getDelegateIndexer(), is(not(delegateIndexer)));
	}
}
