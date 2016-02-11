package info.novatec.inspectit.indexing.buffer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.buffer.IBufferBranchIndexer;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.indexer.IBranchIndexer;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for the {@link BufferBranchIndexer} class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class BufferBranchIndexerTest {

	/**
	 * Class under test.
	 */
	private BufferBranchIndexer<DefaultData> bufferBranchIndexer;

	@Mock
	private IBranchIndexer<DefaultData> delegateIndexer;

	@Mock
	private BufferBranchIndexer<DefaultData> childBufferIndexer;

	/**
	 * Init method.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		bufferBranchIndexer = new BufferBranchIndexer<DefaultData>(delegateIndexer, childBufferIndexer);
	}

	/**
	 * Tests creation of the next tree component.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void nextTreeComponent() {
		// when there is child indexer create branch
		bufferBranchIndexer = new BufferBranchIndexer<DefaultData>(delegateIndexer, childBufferIndexer);

		when(childBufferIndexer.sharedInstance()).thenReturn(true);
		IBufferTreeComponent<DefaultData> component = bufferBranchIndexer.getNextTreeComponent();
		assertThat(component, is(instanceOf(Branch.class)));
		assertThat(((Branch<DefaultData>) component).getBufferBranchIndexer(), is((IBufferBranchIndexer<DefaultData>) childBufferIndexer));

		when(childBufferIndexer.sharedInstance()).thenReturn(false);
		IBufferBranchIndexer<DefaultData> createdChildInstance = mock(IBufferBranchIndexer.class);
		when(childBufferIndexer.getNewInstance()).thenReturn(createdChildInstance);
		component = bufferBranchIndexer.getNextTreeComponent();
		assertThat(component, is(instanceOf(Branch.class)));
		assertThat(((Branch<DefaultData>) component).getBufferBranchIndexer(), is(createdChildInstance));

		// when there is not child indexer create leaf
		bufferBranchIndexer = new BufferBranchIndexer<DefaultData>(delegateIndexer, null);
		component = bufferBranchIndexer.getNextTreeComponent();
		assertThat(component, is(instanceOf(Leaf.class)));
	}

	/**
	 * Test creation of new instance.
	 */
	@Test
	public void newInstance() {
		when(delegateIndexer.sharedInstance()).thenReturn(false);
		IBufferBranchIndexer<DefaultData> createdInstance = bufferBranchIndexer.getNewInstance();
		verify(delegateIndexer, times(1)).getNewInstance();
		assertThat(createdInstance, is(instanceOf(BufferBranchIndexer.class)));
		assertThat(((BufferBranchIndexer<DefaultData>) createdInstance).getDelegateIndexer(), is(not(delegateIndexer)));
	}

	/**
	 * Test the not supported creation of new instance.
	 */
	@Test(expectedExceptions = { UnsupportedOperationException.class })
	public void newInstanceUnsupported() {
		when(delegateIndexer.sharedInstance()).thenReturn(true);
		bufferBranchIndexer.getNewInstance();
	}
}
