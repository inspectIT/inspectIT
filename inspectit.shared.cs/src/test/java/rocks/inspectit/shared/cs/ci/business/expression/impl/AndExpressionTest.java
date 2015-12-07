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
@SuppressWarnings("PMD")
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

	/**
	 * Test {@link AndExpression#addOperand(AbstractExpression)} method.
	 */
	public static class AddOperand extends AndExpressionTest {
		@Test
		public void add() {
			AndExpression andExpression = new AndExpression();
			andExpression.addOperand(new OrExpression());
			assertThat(andExpression.getOperands(), hasSize(1));
		}

		@Test
		public void addTwo() {
			AndExpression andExpression = new AndExpression();
			andExpression.addOperand(new OrExpression());
			andExpression.addOperand(new OrExpression());
			assertThat(andExpression.getOperands(), hasSize(2));
		}

		@Test
		public void addDuplicate() {
			AndExpression andExpression = new AndExpression();
			OrExpression operand = new OrExpression();
			andExpression.addOperand(operand);
			andExpression.addOperand(operand);
			assertThat(andExpression.getOperands(), hasSize(1));
		}
	}

	/**
	 * Test {@link AndExpression#getOperands()} method.
	 */
	public static class GetOperands extends AndExpressionTest {
		OrExpression expr1 = new OrExpression();

		OrExpression expr2 = new OrExpression();

		OrExpression expr3 = new OrExpression();

		@Test
		public void empty() {
			AndExpression andExpression = new AndExpression();
			assertThat(andExpression.getOperands(), hasSize(0));
		}

		@Test
		public void withElements() {
			AndExpression andExpression = new AndExpression(expr1, expr2, expr3);
			assertThat(andExpression.getOperands(), hasSize(3));
			assertThat(andExpression.getOperands(), contains((AbstractExpression) expr1, expr2, expr3));
		}
	}

	/**
	 * Test {@link AndExpression#removeOperand(AbstractExpression)} method.
	 */
	public static class RemoveOperand extends AndExpressionTest {

		OrExpression expr1 = new OrExpression();

		OrExpression expr2 = new OrExpression();

		OrExpression expr3 = new OrExpression();

		@Test
		public void removeOne() {
			AndExpression andExpression = new AndExpression(expr1, expr2, expr3);
			andExpression.removeOperand(expr2);
			assertThat(andExpression.getOperands(), hasSize(2));
		}

		@Test
		public void removeNotExistent() {
			AndExpression andExpression = new AndExpression(expr1, expr3);
			andExpression.removeOperand(expr2);
			assertThat(andExpression.getOperands(), hasSize(2));
		}

		@Test
		public void removeTwice() {
			AndExpression andExpression = new AndExpression(expr1, expr2, expr3);
			andExpression.removeOperand(expr2);
			andExpression.removeOperand(expr2);
			assertThat(andExpression.getOperands(), hasSize(2));
		}
	}

	/**
	 * Test {@link AndExpression#canAddOperand()} method.
	 */
	public static class CanAddOperand extends AndExpressionTest {

		OrExpression expr1 = new OrExpression();

		OrExpression expr2 = new OrExpression();

		OrExpression expr3 = new OrExpression();

		@Test
		public void canAddIfEmpty() {
			AndExpression andExpression = new AndExpression();
			assertThat(andExpression.canAddOperand(), is(true));
		}

		@Test
		public void canAddWithExistingElements() {
			AndExpression andExpression = new AndExpression(expr1, expr2, expr3);
			assertThat(andExpression.canAddOperand(), is(true));
		}
	}

	/**
	 * Test {@link AndExpression#getNumberOfChildExpressions()} method.
	 */
	public static class GetNumberOfChildExpressions extends AndExpressionTest {

		OrExpression expr1 = new OrExpression();

		OrExpression expr2 = new OrExpression();

		OrExpression expr3 = new OrExpression();

		@Test
		public void empty() {
			AndExpression andExpression = new AndExpression();
			assertThat(andExpression.getNumberOfChildExpressions(), is(0));
		}

		@Test
		public void threeElements() {
			AndExpression andExpression = new AndExpression(expr1, expr2, expr3);
			assertThat(andExpression.getNumberOfChildExpressions(), is(3));
		}
	}
}
