package info.novatec.inspectit.ci.business.expression.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.business.valuesource.PatternMatchingType;
import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class StringMatchingExpressionTest extends TestBase {
	@InjectMocks
	StringMatchingExpression strMatchingExpression;

	/**
	 * Test
	 * {@link StringMatchingExpression#evaluate(info.novatec.inspectit.communication.data.InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class Evaluate extends StringMatchingExpressionTest {
		@Mock
		CachedDataService cachedDataService;

		@Mock
		StringValueSource stringValueSource;

		InvocationSequenceData root;
		InvocationSequenceData level_1_1;
		InvocationSequenceData level_1_2;
		InvocationSequenceData level_2_1;

		@BeforeMethod
		public void init() throws BusinessException {
			root = new InvocationSequenceData();
			root.setId(1);
			level_1_1 = new InvocationSequenceData();
			level_1_1.setId(2);
			level_1_2 = new InvocationSequenceData();
			level_1_2.setId(3);
			level_2_1 = new InvocationSequenceData();
			level_2_1.setId(4);
			root.getNestedSequences().add(level_1_1);
			root.getNestedSequences().add(level_1_2);
			level_1_2.getNestedSequences().add(level_2_1);

			when(stringValueSource.getStringValues(root, cachedDataService)).thenReturn(new String[] { "node/root/" });

			when(stringValueSource.getStringValues(level_1_1, cachedDataService)).thenReturn(new String[] { "node/level_1_1/" });

			when(stringValueSource.getStringValues(level_1_2, cachedDataService)).thenReturn(new String[] { "node/level_1_2/" });

			when(stringValueSource.getStringValues(level_2_1, cachedDataService)).thenReturn(new String[] { "node/level_2_1/", "node/level_2_1/multiple" });
			when(cachedDataService.getApplicationForId(anyInt())).thenReturn(null);
			when(cachedDataService.getBusinessTransactionForId(anyInt(), anyInt())).thenReturn(null);
		}

		@Test
		public void simpleStringMatching() {
			strMatchingExpression.setMatchingType(PatternMatchingType.CONTAINS);
			strMatchingExpression.setSnippet("root");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(true));
		}

		@Test
		public void simpleStringMatchingWithEquals() {
			strMatchingExpression.setMatchingType(PatternMatchingType.EQUALS);
			strMatchingExpression.setSnippet("node/root/");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(true));
		}

		@Test
		public void simpleStringMatchingWithStartsWith() {
			strMatchingExpression.setMatchingType(PatternMatchingType.STARTS_WITH);
			strMatchingExpression.setSnippet("node/r");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(true));
		}

		@Test
		public void simpleStringMatchingWithEndsWith() {
			strMatchingExpression.setMatchingType(PatternMatchingType.ENDS_WITH);
			strMatchingExpression.setSnippet("oot/");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(true));
		}

		@Test
		public void simpleStringMatchingWithRegEx() {
			strMatchingExpression.setMatchingType(PatternMatchingType.REGEX);
			strMatchingExpression.setSnippet(".*oo.*");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(true));
		}

		@Test
		public void searchInDepthMatching() {
			strMatchingExpression.setMatchingType(PatternMatchingType.CONTAINS);
			strMatchingExpression.setSnippet("level_1_2");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(true);
			strMatchingExpression.setMaxSearchDepth(-1);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(true));
		}

		@Test
		public void searchInDepthLimitedMatching() {
			strMatchingExpression.setMatchingType(PatternMatchingType.CONTAINS);
			strMatchingExpression.setSnippet("level_2_1");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(true);
			strMatchingExpression.setMaxSearchDepth(1);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(false));
		}

		@Test
		public void multiValueMatching() {
			strMatchingExpression.setMatchingType(PatternMatchingType.CONTAINS);
			strMatchingExpression.setSnippet("multiple");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(true);
			strMatchingExpression.setMaxSearchDepth(-1);

			assertThat(strMatchingExpression.evaluate(root, cachedDataService), is(true));
		}
	}
}
