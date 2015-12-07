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

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class StringMatchingExpressionTest extends TestBase {
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
		InvocationSequenceData level11;
		InvocationSequenceData level12;
		InvocationSequenceData level21;

		@BeforeMethod
		public void init() throws BusinessException {
			root = new InvocationSequenceData();
			root.setId(1);
			level11 = new InvocationSequenceData();
			level11.setId(2);
			level12 = new InvocationSequenceData();
			level12.setId(3);
			level21 = new InvocationSequenceData();
			level21.setId(4);
			root.getNestedSequences().add(level11);
			root.getNestedSequences().add(level12);
			level12.getNestedSequences().add(level21);

			when(stringValueSource.getStringValues(root, cachedDataService)).thenReturn(new String[] { "node/root/" });

			when(stringValueSource.getStringValues(level11, cachedDataService)).thenReturn(new String[] { "node/level_1_1/" });

			when(stringValueSource.getStringValues(level12, cachedDataService)).thenReturn(new String[] { "node/level_1_2/" });

			when(stringValueSource.getStringValues(level21, cachedDataService)).thenReturn(new String[] { "node/level_2_1/", "node/level_2_1/multiple" });
			when(cachedDataService.getApplicationForId(anyInt())).thenReturn(null);
			when(cachedDataService.getBusinessTransactionForId(anyInt(), anyInt())).thenReturn(null);
		}

		@Test
		public void simpleStringMatching() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void simpleStringMatchingWithEquals() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.EQUALS, "node/root/");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void simpleStringMatchingWithStartsWith() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.STARTS_WITH, "node/r");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void simpleStringMatchingWithEndsWith() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.ENDS_WITH, "oot/");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void simpleStringMatchingWithRegEx() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.REGEX, ".*oo.*");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(false);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void searchInDepthMatching() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "level_1_2");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(true);
			strMatchingExpression.setMaxSearchDepth(-1);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void searchInDepthLimitedMatching() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "level_2_1");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(true);
			strMatchingExpression.setMaxSearchDepth(1);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void multiValueMatching() {
			StringMatchingExpression strMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "multiple");
			strMatchingExpression.setStringValueSource(stringValueSource);
			strMatchingExpression.setSearchNodeInTrace(true);
			strMatchingExpression.setMaxSearchDepth(-1);

			boolean evaluationResult = strMatchingExpression.evaluate(root, cachedDataService);

			assertThat(evaluationResult, is(true));
		}
	}
}
