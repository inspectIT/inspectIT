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
public class AndExpressionTest extends TestBase {
	/**
	 * Test
	 * {@link AndExpression#evaluate(rocks.inspectit.shared.all.communication.data.InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class Evaluate extends AndExpressionTest {
		@Mock
		AbstractExpression expr1;

		@Mock
		AbstractExpression expr2;

		@Mock
		AbstractExpression expr3;

		@Mock
		InvocationSequenceData invocation;

		@Mock
		CachedDataService cachedDataService;

		@Test
		public void noOperand() {
			boolean evaluationResult = new AndExpression().evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void oneOperandTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(true);

			AndExpression andExpression = new AndExpression(expr1);
			boolean evaluationResult = andExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void oneOperandFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);

			AndExpression andExpression = new AndExpression(expr1);
			boolean evaluationResult = andExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void twoOperandsTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(true);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(true);

			AndExpression andExpression = new AndExpression(expr1, expr2);
			boolean evaluationResult = andExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void twoOperandsFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(true);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(false);

			AndExpression andExpression = new AndExpression(expr1, expr2);
			boolean evaluationResult = andExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void threeOperandsTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(true);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(true);
			when(expr3.evaluate(invocation, cachedDataService)).thenReturn(true);

			AndExpression andExpression = new AndExpression(expr1, expr2, expr3);
			boolean evaluationResult = andExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void threeOperandsFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(true);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(true);
			when(expr3.evaluate(invocation, cachedDataService)).thenReturn(false);

			AndExpression andExpression = new AndExpression(expr1, expr2, expr3);
			boolean evaluationResult = andExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}
	}
}
