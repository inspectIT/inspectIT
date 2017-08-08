package rocks.inspectit.shared.cs.data.invocationtree;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
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

	InvocationSequenceData invoc03;

	Span span01;

	Span span02;

	Span span03;

	Span span04;

	@BeforeMethod
	public void beforeMethod() throws Exception {
		ISpanService spanService = mock(ISpanService.class);
		IInvocationDataAccessService invocationService = mock(IInvocationDataAccessService.class);
		span01 = createServerSpan(null);
		span02 = createServerSpan(span01);
		span03 = createClientSpan(span02);
		span04 = createServerSpan(span03);
		invoc01 = createSequence(span01);
		InvocationSequenceData _invoc02 = createSequence(span02);
		invoc03 = createSequence(span02);
		InvocationSequenceData _invoc04 = createSequence(span03);
		invoc01.getNestedSequences().add(_invoc02);
		invoc03.getNestedSequences().add(_invoc04);
		_invoc02.setParentSequence(invoc01);
		_invoc04.setParentSequence(invoc03);
		doReturn(Arrays.asList(span01, span02, span03, span04)).when(spanService).getSpans(1337L);
		doReturn(Arrays.asList(invoc01, invoc03)).when(invocationService).getInvocationSequenceDetail(1337L);

		tree = new InvocationTreeBuilder().setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false).build();

		System.out.println("> trace\n" + InvocationTreeUtil.toString(tree));
	}

	public static class GetInvocationSequences extends InvocationTreeUtilTest {

		@Test
		public void buildTrace() {
			List<InvocationSequenceData> sequences = InvocationTreeUtil.getInvocationSequences(tree);

			assertThat(sequences, containsInAnyOrder(is(invoc01), is(invoc03)));
		}

	}

	public static class GetDataElements extends InvocationTreeUtilTest {

		@Test
		public void buildTrace() {
			List<Object> sequences = InvocationTreeUtil.getDataElements(tree);

			assertThat(sequences, containsInAnyOrder(is(invoc01), is(invoc03), is(span01), is(span02), is(span03), is(span04)));
		}
	}
}
