package info.novatec.inspectit.ci.business.expression.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.business.expression.AbstractExpression;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class OrExpressionTest extends TestBase {
	@InjectMocks
	OrExpression orExpression;

	/**
	 * Test
	 * {@link OrExpression#evaluate(info.novatec.inspectit.communication.data.InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class Evaluate extends OrExpressionTest {
		@Mock
		AbstractExpression expr_1;

		@Mock
		AbstractExpression expr_2;

		@Mock
		AbstractExpression expr_3;

		@Mock
		InvocationSequenceData invocation;

		@Mock
		CachedDataService cachedDataService;

		@Test
		public void noOperand() {
			assertThat(orExpression.evaluate(invocation, cachedDataService), is(false));
		}

		@Test
		public void oneOperandTrue() {
			when(expr_1.evaluate(invocation, cachedDataService)).thenReturn(true);
			orExpression.getOperands().add(expr_1);

			assertThat(orExpression.evaluate(invocation, cachedDataService), is(true));
		}

		@Test
		public void oneOperandFalse() {
			when(expr_1.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_1);

			assertThat(orExpression.evaluate(invocation, cachedDataService), is(false));
		}

		@Test
		public void twoOperandsTrue() {
			when(expr_1.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_1);

			when(expr_2.evaluate(invocation, cachedDataService)).thenReturn(true);
			orExpression.getOperands().add(expr_2);

			assertThat(orExpression.evaluate(invocation, cachedDataService), is(true));
		}

		@Test
		public void twoOperandsFalse() {
			when(expr_1.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_1);

			when(expr_2.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_2);

			assertThat(orExpression.evaluate(invocation, cachedDataService), is(false));
		}

		@Test
		public void threeOperandsTrue() {
			when(expr_1.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_1);

			when(expr_2.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_2);

			when(expr_3.evaluate(invocation, cachedDataService)).thenReturn(true);
			orExpression.getOperands().add(expr_3);

			assertThat(orExpression.evaluate(invocation, cachedDataService), is(true));
		}

		@Test
		public void threeOperandsFalse() {
			when(expr_1.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_1);

			when(expr_2.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_2);

			when(expr_3.evaluate(invocation, cachedDataService)).thenReturn(false);
			orExpression.getOperands().add(expr_3);

			assertThat(orExpression.evaluate(invocation, cachedDataService), is(false));
		}
	}
}
