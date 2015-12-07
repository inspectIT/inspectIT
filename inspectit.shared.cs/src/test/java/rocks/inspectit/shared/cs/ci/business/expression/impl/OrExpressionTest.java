package rocks.inspectit.shared.cs.ci.business.expression.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
public class OrExpressionTest extends TestBase {
	/**
	 * Test
	 * {@link OrExpression#evaluate(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class Evaluate extends OrExpressionTest {
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
			boolean evaluationResult = new OrExpression().evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void oneOperandTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(true);

			OrExpression orExpression = new OrExpression(expr1);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void oneOperandFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);

			OrExpression orExpression = new OrExpression(expr1);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void twoOperandsTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(true);

			OrExpression orExpression = new OrExpression(expr1, expr2);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void twoOperandsFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(false);

			OrExpression orExpression = new OrExpression(expr1, expr2);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}

		@Test
		public void threeOperandsTrue() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr3.evaluate(invocation, cachedDataService)).thenReturn(true);

			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(true));
		}

		@Test
		public void threeOperandsFalse() {
			when(expr1.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr2.evaluate(invocation, cachedDataService)).thenReturn(false);
			when(expr3.evaluate(invocation, cachedDataService)).thenReturn(false);

			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			boolean evaluationResult = orExpression.evaluate(invocation, cachedDataService);

			assertThat(evaluationResult, is(false));
		}
	}

	/**
	 * Test {@link OrExpression#addOperand(AbstractExpression)} method.
	 */
	public static class AddOperand extends OrExpressionTest {
		@Test
		public void add() {
			OrExpression orExpression = new OrExpression();
			orExpression.addOperand(new BooleanExpression());
			assertThat(orExpression.getOperands(), hasSize(1));
		}

		@Test
		public void addTwo() {
			OrExpression orExpression = new OrExpression();
			orExpression.addOperand(new BooleanExpression());
			orExpression.addOperand(new BooleanExpression());
			assertThat(orExpression.getOperands(), hasSize(2));
		}

		@Test
		public void addDuplicate() {
			OrExpression orExpression = new OrExpression();
			BooleanExpression operand = new BooleanExpression();
			orExpression.addOperand(operand);
			orExpression.addOperand(operand);
			assertThat(orExpression.getOperands(), hasSize(1));
		}
	}

	/**
	 * Test {@link OrExpression#getOperands()} method.
	 */
	public static class GetOperands extends OrExpressionTest {
		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void empty() {
			OrExpression orExpression = new OrExpression();
			assertThat(orExpression.getOperands(), hasSize(0));
		}

		@Test
		public void withElements() {
			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			assertThat(orExpression.getOperands(), hasSize(3));
			assertThat(orExpression.getOperands(), contains((AbstractExpression) expr1, expr2, expr3));
		}
	}

	/**
	 * Test {@link OrExpression#removeOperand(AbstractExpression)} method.
	 */
	public static class RemoveOperand extends OrExpressionTest {

		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void removeOne() {
			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			orExpression.removeOperand(expr2);
			assertThat(orExpression.getOperands(), hasSize(2));
		}

		@Test
		public void removeNotExistent() {
			OrExpression orExpression = new OrExpression(expr1, expr3);
			orExpression.removeOperand(expr2);
			assertThat(orExpression.getOperands(), hasSize(2));
		}

		@Test
		public void removeTwice() {
			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			orExpression.removeOperand(expr2);
			orExpression.removeOperand(expr2);
			assertThat(orExpression.getOperands(), hasSize(2));
		}
	}

	/**
	 * Test {@link OrExpression#canAddOperand()} method.
	 */
	public static class CanAddOperand extends OrExpressionTest {

		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void canAddIfEmpty() {
			OrExpression orExpression = new OrExpression();
			assertThat(orExpression.canAddOperand(), is(true));
		}

		@Test
		public void canAddWithExistingElements() {
			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			assertThat(orExpression.canAddOperand(), is(true));
		}
	}

	/**
	 * Test {@link OrExpression#getNumberOfChildExpressions()} method.
	 */
	public static class GetNumberOfChildExpressions extends OrExpressionTest {

		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void empty() {
			OrExpression orExpression = new OrExpression();
			assertThat(orExpression.getNumberOfChildExpressions(), is(0));
		}

		@Test
		public void threeElements() {
			OrExpression orExpression = new OrExpression(expr1, expr2, expr3);
			assertThat(orExpression.getNumberOfChildExpressions(), is(3));
		}
	}
}
