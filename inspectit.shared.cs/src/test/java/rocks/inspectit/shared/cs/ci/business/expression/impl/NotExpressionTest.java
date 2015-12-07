package rocks.inspectit.shared.cs.ci.business.expression.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.business.expression.AbstractExpression;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
public class NotExpressionTest extends TestBase {
	/**
	 * Test
	 * {@link NotExpression#evaluate(rocks.inspectit.shared.all.communication.data.InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class Evaluate extends NotExpressionTest {
		@Mock
		AbstractExpression expr;

		@Mock
		InvocationSequenceData invocation;

		@Mock
		CachedDataService cachedDataService;

		@Test
		public void testTrue() {
			when(expr.evaluate(invocation, cachedDataService)).thenReturn(true);

			NotExpression notExpression = new NotExpression(expr);
			boolean evaluationResult = notExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void testFalse() {
			when(expr.evaluate(invocation, cachedDataService)).thenReturn(false);

			NotExpression notExpression = new NotExpression(expr);
			boolean evaluationResult = notExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}
	}
}
