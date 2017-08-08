package rocks.inspectit.shared.cs.data.invocationtree;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.cs.cmr.service.IInvocationDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;
import rocks.inspectit.shared.cs.data.invocationtree.InvocationTreeBuilder.Mode;

/**
 * Tests the {@link InvocationTreeBuilder} class.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes", "PMD" })
public class InvocationTreeBuilderTest extends InvocationTreeTestBase {

	@InjectMocks
	InvocationTreeBuilder builder;

	IInvocationDataAccessService invocationService;

	ISpanService spanService;

	@BeforeMethod
	public void beforeMethod() {
		invocationService = mock(IInvocationDataAccessService.class);
		spanService = mock(ISpanService.class);
	}

	/**
	 * Test the {@link InvocationTreeBuilder#build()} method.
	 *
	 * @author Marius Oehler
	 *
	 */
	public static class Build extends InvocationTreeBuilderTest {

		@Test
		public void useCache() {
			InvocationSequenceData invoc01 = createSequence(null);
			builder.setMode(Mode.SINGLE).setInvocationSequence(invoc01);

			InvocationTreeElement treeOne = builder.build();
			InvocationTreeElement treeTwo = builder.build();

			assertThat(treeOne, is(sameInstance(treeTwo)));
		}

		@Test
		public void disabledCache() {
			InvocationSequenceData invoc01 = createSequence(null);
			builder.setMode(Mode.SINGLE).setInvocationSequence(invoc01);

			InvocationTreeElement treeOne = builder.setUseCache(false).build();
			InvocationTreeElement treeTwo = builder.build();

			assertThat(treeOne, is(not(sameInstance(treeTwo))));
		}

		@Test(expectedExceptions = IllegalStateException.class)
		public void noTraceId() {
			builder.setSpanService(spanService).setInvocationService(invocationService);

			builder.build();
		}

		@Test
		public void buildOneInvocationSequence() {
			InvocationSequenceData invoc01 = createSequence(null);
			InvocationSequenceData _invoc02 = createSequence(null);
			InvocationSequenceData _invoc03 = createSequence(null);
			InvocationSequenceData _invoc04 = createSequence(null);
			invoc01.getNestedSequences().add(_invoc02);
			_invoc02.getNestedSequences().add(_invoc03);
			invoc01.getNestedSequences().add(_invoc04);

			builder.setMode(Mode.SINGLE).setInvocationSequence(invoc01).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(4));
			assertThat(tree.getDataElement(), is(invoc01));
			assertThat(tree.getChildren(), contains(hasProperty("dataElement", is(_invoc02)), hasProperty("dataElement", is(_invoc04))));
			assertThat(tree.getChildren().get(0).getChildren(), contains(hasProperty("dataElement", is(_invoc03))));
			assertThat(tree.getChildren().get(1).getChildren(), is(empty()));
			verifyZeroInteractions(spanService, invocationService);

			System.out.println("> one invocation\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildOneInvocationSequenceWithSpan() {
			Span span01 = createServerSpan(null);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(null);
			invoc01.getNestedSequences().add(_invoc02);
			doReturn(span01).when(spanService).get(span01.getSpanIdent());

			builder.setSpanService(spanService).setMode(Mode.SINGLE).setInvocationSequence(invoc01).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(3));
			assertThat(tree.getDataElement(), is(span01));
			assertThat(tree.getChildren(), contains(hasProperty("dataElement", is(invoc01))));
			verify(spanService).get(span01.getSpanIdent());
			verifyNoMoreInteractions(spanService);
			verifyZeroInteractions(invocationService);

			System.out.println("> one invocation with span\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildOneInvocationSequenceWithSpanWithoutSpanService() {
			Span span01 = createServerSpan(null);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(null);
			invoc01.getNestedSequences().add(_invoc02);

			builder.setMode(Mode.SINGLE).setInvocationSequence(invoc01).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(3));
			assertThat(tree.getDataElement(), isA((Class) SpanIdent.class));
			assertThat(tree.getChildren(), contains(hasProperty("dataElement", is(invoc01))));
			verifyZeroInteractions(spanService, invocationService);

			System.out.println("> one invocation with span without span service\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildOneInvocationSequenceInsideTrace() {
			Span span01 = createServerSpan(null);
			Span span02 = createServerSpan(span01);
			Span span03 = createClientSpan(span02);
			Span span04 = createServerSpan(span03);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(span02);
			InvocationSequenceData invoc03 = createSequence(span02);
			InvocationSequenceData _invoc04 = createSequence(span03);
			invoc01.getNestedSequences().add(_invoc02);
			invoc03.getNestedSequences().add(_invoc04);
			doReturn(span01).when(spanService).get(span01.getSpanIdent());
			doReturn(span02).when(spanService).get(span02.getSpanIdent());
			doReturn(span03).when(spanService).get(span03.getSpanIdent());
			doReturn(span04).when(spanService).get(span04.getSpanIdent());

			builder.setSpanService(spanService).setMode(Mode.SINGLE).setInvocationSequence(invoc03).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(4));
			assertThat(tree.getDataElement(), is(span02));
			assertThat(tree.getChildren(), contains(hasProperty("dataElement", is(invoc03))));
			assertThat(tree.getChildren().get(0).getChildren(), contains(hasProperty("dataElement", is(_invoc04))));
			assertThat(tree.getChildren().get(0).getChildren().get(0).getChildren(), contains(hasProperty("dataElement", is(span03))));
			assertThat(tree.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren(), is(empty()));
			assertThat(tree.lookup(_invoc02), is(nullValue()));
			assertThat(tree.lookup(span04), is(nullValue()));
			verify(spanService).get(span02.getSpanIdent());
			verify(spanService).get(span03.getSpanIdent());
			verifyNoMoreInteractions(spanService);
			verifyZeroInteractions(invocationService);

			System.out.println("> one invocation inside trace\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildTrace() {
			Span span01 = createServerSpan(null);
			Span span02 = createServerSpan(span01);
			Span span03 = createClientSpan(span02);
			Span span04 = createServerSpan(span03);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(span02);
			InvocationSequenceData invoc03 = createSequence(span02);
			InvocationSequenceData _invoc04 = createSequence(span03);
			invoc01.getNestedSequences().add(_invoc02);
			invoc03.getNestedSequences().add(_invoc04);
			doReturn(Arrays.asList(span01, span02, span03, span04)).when(spanService).getSpans(1337L);
			doReturn(Arrays.asList(invoc01, invoc03)).when(invocationService).getInvocationSequenceDetail(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(8));
			assertThat(tree.getDataElement(), is(span01));
			for (Object data : new Object[] { span01, span02, span03, span04, invoc01, _invoc02, invoc03, _invoc04 }) {
				assertThat(tree.lookup(data), is(not(nullValue())));
			}
			verify(spanService).getSpans(1337L);
			verify(invocationService).getInvocationSequenceDetail(1337L);
			verifyNoMoreInteractions(spanService, invocationService);

			System.out.println("> trace\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildTraceOnlySpans() {
			Span span01 = createServerSpan(null);
			Span span02 = createServerSpan(span01);
			Span span03 = createClientSpan(span02);
			Span span04 = createServerSpan(span03);
			Span span05 = createSdkSpan(span02);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(span02);
			InvocationSequenceData invoc03 = createSequence(span02);
			InvocationSequenceData _invoc04 = createSequence(span03);
			invoc01.getNestedSequences().add(_invoc02);
			invoc03.getNestedSequences().add(_invoc04);
			doReturn(Arrays.asList(span01, span02, span03, span04, span05)).when(spanService).getSpans(1337L);
			doReturn(Arrays.asList(invoc01, invoc03)).when(invocationService).getInvocationSequenceDetail(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setMode(Mode.ONLY_SPANS_WITH_SDK).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(5));
			assertThat(tree.getDataElement(), is(span01));
			for (Object data : new Object[] { span01, span02, span03, span04, span05 }) {
				assertThat(tree.lookup(data), is(not(nullValue())));
			}
			for (Object data : new Object[] { invoc01, _invoc02, invoc03, _invoc04 }) {
				assertThat(tree.lookup(data), is(nullValue()));
			}
			verify(spanService).getSpans(1337L);
			verify(invocationService).getInvocationSequenceDetail(1337L);
			verifyNoMoreInteractions(spanService, invocationService);

			System.out.println("> trace only spans\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildTraceWithMissingInvocationSequence() {
			Span span01 = createServerSpan(null);
			Span span02 = createServerSpan(span01);
			Span span03 = createClientSpan(span02);
			Span span04 = createServerSpan(span03);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(span02);
			InvocationSequenceData invoc03 = createSequence(span02);
			InvocationSequenceData _invoc04 = createSequence(span03);
			invoc01.getNestedSequences().add(_invoc02);
			invoc03.getNestedSequences().add(_invoc04);
			doReturn(Arrays.asList(span01, span02, span03, span04)).when(spanService).getSpans(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(4));
			assertThat(tree.getDataElement(), is(span01));
			assertThat(tree.getChildren(), contains(hasProperty("dataElement", is(span02))));
			assertThat(tree.getChildren().get(0).getChildren(), contains(hasProperty("dataElement", is(span03))));
			assertThat(tree.getChildren().get(0).getChildren().get(0).getChildren(), contains(hasProperty("dataElement", is(span04))));
			assertThat(tree.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren(), is(empty()));
			verify(spanService).getSpans(1337L);
			verify(invocationService).getInvocationSequenceDetail(1337L);
			verifyNoMoreInteractions(spanService, invocationService);

			System.out.println("> trace with missing invocation sequences\n" + InvocationTreeUtil.toString(tree));
		}

		@Test(invocationCount = 5)
		public void buildWideTreeShuffeledSpanProvision() {
			Span span01 = createServerSpan(null);
			Span span02 = createServerSpan(span01);
			InvocationSequenceData invoc03 = createSequence(span01); // between span02 and span03
			Span span03 = createClientSpan(span01);
			Span span04 = createServerSpan(span03);
			InvocationSequenceData invoc01 = createSequence(span02);
			InvocationSequenceData invoc02 = createSequence(span04);
			List<Span> list = Arrays.asList(span01, span02, span03, span04);
			Collections.shuffle(list);
			doReturn(list).when(spanService).getSpans(1337L);
			doReturn(Arrays.asList(invoc01, invoc02, invoc03)).when(invocationService).getInvocationSequenceDetail(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(7));
			assertThat(tree.getDataElement(), is(span01));
			assertThat(tree.getChildren(), contains(hasProperty("dataElement", is(span02)), hasProperty("dataElement", is(invoc03)), hasProperty("dataElement", is(span03))));
			assertThat(tree.lookup(span02).getChildren(), contains(hasProperty("dataElement", is(invoc01))));
			assertThat(tree.lookup(span03).getChildren(), contains(hasProperty("dataElement", is(span04))));
			assertThat(tree.lookup(span04).getChildren(), contains(hasProperty("dataElement", is(invoc02))));
			verify(spanService).getSpans(1337L);
			verify(invocationService).getInvocationSequenceDetail(1337L);
			verifyNoMoreInteractions(spanService, invocationService);

			System.out.println("> wide trace shuffeled\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildTraceManyMissingSpans() {
			Span span01 = createServerSpan(null);
			Span span02 = createServerSpan(span01);
			Span span03 = createClientSpan(span01);
			Span span04 = createServerSpan(span01);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(span02);
			InvocationSequenceData _invoc03 = createSequence(span03);
			InvocationSequenceData _invoc04 = createSequence(span04);
			invoc01.getNestedSequences().addAll(Arrays.asList(_invoc02, _invoc03, _invoc04));
			doReturn(Arrays.asList(span01)).when(spanService).getSpans(1337L);
			doReturn(Arrays.asList(invoc01)).when(invocationService).getInvocationSequenceDetail(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(8));
			assertThat(tree.getDataElement(), is(span01));
			for (Object data : new Object[] { span01, span02, span03, span04, invoc01, _invoc02, _invoc03, _invoc04 }) {
				assertThat(tree.lookup(data), is(not(nullValue())));
			}
			verify(spanService).getSpans(1337L);
			verify(invocationService).getInvocationSequenceDetail(1337L);
			verifyNoMoreInteractions(spanService, invocationService);

			System.out.println("> trace with many missing spans\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildTraceWithoutSdkSpans() {
			Span span01 = createServerSpan(null);
			Span span02 = createServerSpan(span01);
			Span span03 = createSdkSpan(span01);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(span02);
			invoc01.getNestedSequences().add(_invoc02);
			doReturn(Arrays.asList(span01, span02, span03)).when(spanService).getSpans(1337L);
			doReturn(Arrays.asList(invoc01)).when(invocationService).getInvocationSequenceDetail(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(4));
			assertThat(tree.getDataElement(), is(span01));
			for (Object data : new Object[] { span01, span02, invoc01, _invoc02 }) {
				assertThat(tree.lookup(data), is(not(nullValue())));
			}
			assertThat(tree.lookup(span03), is(nullValue()));
			verify(spanService).getSpans(1337L);
			verify(invocationService).getInvocationSequenceDetail(1337L);
			verifyNoMoreInteractions(spanService, invocationService);

			System.out.println("> trace without SDK span\n" + InvocationTreeUtil.toString(tree));
		}

		@Test
		public void buildWithNestedExceptions() {
			Span span01 = createServerSpan(null);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(null);
			invoc01.getNestedSequences().add(_invoc02);
			invoc01.setNestedExceptions(true);
			doReturn(Arrays.asList(span01)).when(spanService).getSpans(1337L);
			doReturn(Arrays.asList(invoc01)).when(invocationService).getInvocationSequenceDetail(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(3));
			assertThat(tree.hasNestedExceptions(), is(true));
			assertThat(tree.hasNestedSqls(), is(false));
		}

		@Test
		public void buildWithNestedSQLs() {
			Span span01 = createServerSpan(null);
			InvocationSequenceData invoc01 = createSequence(span01);
			InvocationSequenceData _invoc02 = createSequence(null);
			invoc01.getNestedSequences().add(_invoc02);
			invoc01.setNestedSqlStatements(true);
			doReturn(Arrays.asList(span01)).when(spanService).getSpans(1337L);
			doReturn(Arrays.asList(invoc01)).when(invocationService).getInvocationSequenceDetail(1337L);

			builder.setSpanService(spanService).setInvocationService(invocationService).setTraceId(1337L).setUseCache(false);

			InvocationTreeElement tree = builder.build();

			assertThat(tree.getSize(), is(3));
			assertThat(tree.hasNestedExceptions(), is(false));
			assertThat(tree.hasNestedSqls(), is(true));
		}
	}
}
