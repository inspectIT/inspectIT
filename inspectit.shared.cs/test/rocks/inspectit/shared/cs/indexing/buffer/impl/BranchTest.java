package info.novatec.inspectit.indexing.buffer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.ITreeComponent;
import info.novatec.inspectit.indexing.buffer.IBufferBranchIndexer;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing of the buffer branch class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class BranchTest {

	/**
	 * Class under test.
	 */
	private Branch<DefaultData> branch;

	/**
	 * Indexer needed because of the constructor.
	 */
	@Mock
	private IBufferBranchIndexer<DefaultData> bufferBranchIndexer;

	/**
	 * Init.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		branch = Mockito.spy(new Branch<DefaultData>(bufferBranchIndexer));
	}

	/**
	 * Tests that clean of the branch is correct.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void clean() {
		IBufferTreeComponent<DefaultData> component1 = Mockito.mock(IBufferTreeComponent.class);
		when(component1.clean()).thenReturn(true);

		IBufferTreeComponent<DefaultData> component2 = Mockito.mock(IBufferTreeComponent.class);
		when(component2.clean()).thenReturn(false);

		Map<Object, ITreeComponent<DefaultData, DefaultData>> componentMap = MapUtils.putAll(new HashMap<Object, IBufferTreeComponent<DefaultData>>(), new Object[] { "c1", component1, "c2",
				component2 });
		when(branch.getComponentMap()).thenReturn(componentMap);

		boolean isClean = branch.clean();

		assertThat(isClean, is(false));
		assertThat(componentMap, not(hasValue((ITreeComponent<DefaultData, DefaultData>) component1)));
		assertThat(componentMap, hasValue((ITreeComponent<DefaultData, DefaultData>) component2));

		when(component2.clean()).thenReturn(true);

		isClean = branch.clean();

		assertThat(isClean, is(true));
		assertThat(componentMap.values(), is(empty()));
	}

	/**
	 * Test that clearing of empty components is correct.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void clearEmptyComponents() {
		IBufferTreeComponent<DefaultData> component1 = Mockito.mock(IBufferTreeComponent.class);
		when(component1.clearEmptyComponents()).thenReturn(true);

		IBufferTreeComponent<DefaultData> component2 = Mockito.mock(IBufferTreeComponent.class);
		when(component2.clearEmptyComponents()).thenReturn(false);

		Map<Object, ITreeComponent<DefaultData, DefaultData>> componentMap = MapUtils.putAll(new HashMap<Object, IBufferTreeComponent<DefaultData>>(), new Object[] { "c1", component1, "c2",
				component2 });
		when(branch.getComponentMap()).thenReturn(componentMap);

		boolean isClear = branch.clearEmptyComponents();

		assertThat(isClear, is(false));
		assertThat(componentMap, not(hasValue((ITreeComponent<DefaultData, DefaultData>) component1)));
		assertThat(componentMap, hasValue((ITreeComponent<DefaultData, DefaultData>) component2));

		when(component2.clearEmptyComponents()).thenReturn(true);

		isClear = branch.clearEmptyComponents();

		assertThat(isClear, is(true));
		assertThat(componentMap.values(), is(empty()));
	}
}
