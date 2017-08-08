package rocks.inspectit.shared.cs.data.invocationtree;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.opentracing.References;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;

/**
 * Tests the {@link InvocationTreeUtil} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings({ "unchecked", "PMD" })
public class InvocationTreeUtilTest extends InvocationTreeTestBase {

	InvocationTreeElement tree;

	InvocationSequenceData invoc01;

	InvocationSequenceData _invoc02;

	InvocationSequenceData invoc03;

	InvocationSequenceData _invoc04;

	Span span01;

	Span span02;

	Span span03;

	Span span04;

	Span span05;

	/**
	 * The exclusive times in the tree are shown in the following tree:
	 *
	 * <code>
	 * 130ms
	 *  > 100ms
	 *  > > 10ms * async
	 *  > > > 40ms
	 *  > 30ms
	 * </code>
	 *
	 */
	@BeforeMethod
	public void beforeMethod() throws Exception {
		ISpanService spanService = mock(ISpanService.class);
		IInvocationDataAccessService invocationService = mock(IInvocationDataAccessService.class);
		span01 = createServerSpan(null);
		span02 = createServerSpan(span01);
		span03 = createClientSpan(span02);
		span04 = createServerSpan(span03);
		span05 = createServerSpan(span01);
		invoc01 = createSequence(span01);
		_invoc02 = createSequence(span02);
		invoc03 = createSequence(span02);
		_invoc04 = createSequence(span03);
		invoc01.getNestedSequences().add(_invoc02);
		invoc03.getNestedSequences().add(_invoc04);
		_invoc02.setParentSequence(invoc01);
		_invoc04.setParentSequence(invoc03);

		((ServerSpan) span01).setDuration(260);
		((ServerSpan) span02).setDuration(100);
		((ClientSpan) span03).setDuration(50);
		((ServerSpan) span04).setDuration(40);
		((ServerSpan) span05).setDuration(30);

		((ClientSpan) span03).setReferenceType(References.FOLLOWS_FROM);

		doReturn(Arrays.asList(span01, span02, span03, span04, span05)).when(spanService).getSpans(1337L);
		doReturn(Arrays.asList(invoc01, invoc03)).when(invocationService).getInvocationSequenceDetail(1337L);

		tree = new InvocationTreeBuilder().setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).build();
	}

	/**
	 * Tests the {@link InvocationTreeUtil#getInvocationSequences(InvocationTreeElement)} method.
	 */
	public static class GetInvocationSequences extends InvocationTreeUtilTest {

		@Test
		public void successful() {
			List<InvocationSequenceData> sequences = InvocationTreeUtil.getInvocationSequences(tree);

			assertThat(sequences, containsInAnyOrder(is(invoc01), is(invoc03)));
		}

		@Test
		public void nullValue() {
			List<InvocationSequenceData> sequences = InvocationTreeUtil.getInvocationSequences(null);

			assertThat(sequences, is(empty()));
		}
	}

	/**
	 * Tests the {@link InvocationTreeUtil#getDataElements(InvocationTreeElement)} method.
	 */
	public static class GetDataElements extends InvocationTreeUtilTest {

		@Test
		public void successful() {
			List<Object> sequences = InvocationTreeUtil.getDataElements(tree);

			assertThat(sequences, containsInAnyOrder(is(invoc01), is(invoc03), is(span01), is(span02), is(span03), is(span04), is(span05)));
		}

		@Test
		public void nullValue() {
			List<Object> sequences = InvocationTreeUtil.getDataElements(null);

			assertThat(sequences, is(empty()));
		}
	}

	/**
	 * Tests the {@link InvocationTreeUtil#calculateSpanExclusiveDuration(InvocationTreeElement)}
	 * method.
	 */
	public static class CalculateSpanExclusiveDuration extends InvocationTreeUtilTest {

		@Test
		public void successfulAtRoot() {
			double duration = InvocationTreeUtil.calculateSpanExclusiveDuration(tree);

			assertThat(duration, is(130D));
		}

		@Test
		public void successfulInTree() {
			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(InvocationTreeUtil.buildLookupMap(tree), span02);

			double duration = InvocationTreeUtil.calculateSpanExclusiveDuration(treeElement);

			assertThat(duration, is(100D));
		}

		@Test
		public void nullValue() {
			double duration = InvocationTreeUtil.calculateSpanExclusiveDuration(null);

			assertThat(duration, is(Double.NaN));
		}
	}

	/**
	 * Tests the {@link InvocationTreeUtil#calculateSpanExclusivePercentage(InvocationTreeElement)}
	 * method.
	 */
	public static class CalculateSpanExclusivePercentage extends InvocationTreeUtilTest {

		@Test
		public void successfulAtRoot() {
			double duration = InvocationTreeUtil.calculateSpanExclusivePercentage(tree);

			assertThat(duration, is(0.5D));
		}

		@Test
		public void successfulInTree() {
			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(InvocationTreeUtil.buildLookupMap(tree), span02);

			double duration = InvocationTreeUtil.calculateSpanExclusivePercentage(treeElement);

			assertThat(duration, is(closeTo(0.384D, 0.001)));
		}

		@Test
		public void successfulOfAsync() {
			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(InvocationTreeUtil.buildLookupMap(tree), span03);

			double duration = InvocationTreeUtil.calculateSpanExclusivePercentage(treeElement);

			assertThat(duration, is(0.2D));
		}

		@Test
		public void nullValue() {
			double duration = InvocationTreeUtil.calculateSpanExclusivePercentage(null);

			assertThat(duration, is(Double.NaN));
		}
	}

	/**
	 * Tests the {@link InvocationTreeUtil#getRoot(InvocationTreeElement)} method.
	 */
	public static class GetRoot extends InvocationTreeUtilTest {

		@Test
		public void successful() {
			InvocationTreeElement root = InvocationTreeUtil.getRoot(tree);

			assertThat(root, is(tree));
		}

		@Test
		public void successfulInTree() {
			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(InvocationTreeUtil.buildLookupMap(tree), span03);

			InvocationTreeElement root = InvocationTreeUtil.getRoot(treeElement);

			assertThat(root, is(tree));
		}

		@Test
		public void useNullValue() {
			InvocationTreeElement root = InvocationTreeUtil.getRoot(null);

			assertThat(root, is(nullValue()));
		}
	}

	/**
	 * Tests the {@link InvocationTreeUtil#isChildOfSpan(InvocationTreeElement)} method.
	 */
	public static class IsChildOfSpan extends InvocationTreeUtilTest {

		@Test
		public void parentIsSpan() {
			InvocationTreeElement child = tree.getChildren().get(0);

			boolean result = InvocationTreeUtil.isChildOfSpan(child);

			assertThat(result, is(true));
		}

		@Test
		public void parentIsNotSpan() {
			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(InvocationTreeUtil.buildLookupMap(tree), _invoc02);

			boolean result = InvocationTreeUtil.isChildOfSpan(treeElement);

			assertThat(result, is(false));
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void useNullValue() {
			InvocationTreeUtil.isChildOfSpan(null);
		}
	}

	/**
	 * Tests the {@link InvocationTreeUtil#lookupTreeElement(java.util.Map, Object)} method.
	 */
	public static class LookupTreeElement extends InvocationTreeUtilTest {

		@Test
		public void successful01() {
			Map<Object, InvocationTreeElement> lookupMap = InvocationTreeUtil.buildLookupMap(tree);

			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(lookupMap, _invoc02);

			assertThat(treeElement.getDataElement(), is(_invoc02));
		}

		@Test
		public void successful02() {
			Map<Object, InvocationTreeElement> lookupMap = InvocationTreeUtil.buildLookupMap(tree);

			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(lookupMap, span01);

			assertThat(treeElement.getDataElement(), is(span01));
			assertThat(treeElement, is(tree));
		}

		@Test
		public void useNullValue() {
			Map<Object, InvocationTreeElement> lookupMap = InvocationTreeUtil.buildLookupMap(tree);

			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(lookupMap, null);

			assertThat(treeElement, is(nullValue()));
		}

		@Test
		public void nullLookupMap() {
			InvocationTreeElement treeElement = InvocationTreeUtil.lookupTreeElement(null, span01);

			assertThat(treeElement, is(nullValue()));
		}
	}
}
