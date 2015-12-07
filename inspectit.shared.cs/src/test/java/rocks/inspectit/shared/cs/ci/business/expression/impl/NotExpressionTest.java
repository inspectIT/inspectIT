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

	/**
	 * Test {@link NotExpression#addOperand(AbstractExpression)} method.
	 */
	public static class AddOperand extends NotExpressionTest {
		@Test
		public void add() {
			NotExpression orExpression = new NotExpression();
			orExpression.addOperand(new BooleanExpression());
			assertThat(orExpression.getOperands(), hasSize(1));
		}

		@Test
		public void addTwo() {
			NotExpression orExpression = new NotExpression();
			BooleanExpression firstExpr = new BooleanExpression();
			orExpression.addOperand(firstExpr);
			orExpression.addOperand(new BooleanExpression());
			assertThat(orExpression.getOperands(), hasSize(1));
			assertThat(orExpression.getOperands(), contains((AbstractExpression) firstExpr));
		}

		@Test
		public void addDuplicate() {
			NotExpression orExpression = new NotExpression();
			BooleanExpression operand = new BooleanExpression();
			orExpression.addOperand(operand);
			orExpression.addOperand(operand);
			assertThat(orExpression.getOperands(), hasSize(1));
		}
	}

	/**
	 * Test {@link NotExpression#getOperands()} method.
	 */
	public static class GetOperands extends NotExpressionTest {
		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void empty() {
			NotExpression orExpression = new NotExpression();
			assertThat(orExpression.getOperands(), hasSize(0));
		}

		@Test
		public void withElements() {
			NotExpression orExpression = new NotExpression(expr1);
			assertThat(orExpression.getOperands(), hasSize(1));
			assertThat(orExpression.getOperands(), contains((AbstractExpression) expr1));
		}
	}

	/**
	 * Test {@link NotExpression#removeOperand(AbstractExpression)} method.
	 */
	public static class RemoveOperand extends NotExpressionTest {

		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void removeOne() {
			NotExpression orExpression = new NotExpression(expr1);
			orExpression.removeOperand(expr1);
			assertThat(orExpression.getOperands(), hasSize(0));
		}

		@Test
		public void removeNotExistent() {
			NotExpression orExpression = new NotExpression(expr1);
			orExpression.removeOperand(expr2);
			assertThat(orExpression.getOperands(), hasSize(1));
		}

		@Test
		public void removeTwice() {
			NotExpression orExpression = new NotExpression(expr1);
			orExpression.removeOperand(expr1);
			orExpression.removeOperand(expr1);
			assertThat(orExpression.getOperands(), hasSize(0));
		}
	}

	/**
	 * Test {@link NotExpression#canAddOperand()} method.
	 */
	public static class CanAddOperand extends NotExpressionTest {

		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void canAddIfEmpty() {
			NotExpression orExpression = new NotExpression();
			assertThat(orExpression.canAddOperand(), is(true));
		}

		@Test
		public void canAddWithExistingElements() {
			NotExpression orExpression = new NotExpression(expr1);
			assertThat(orExpression.canAddOperand(), is(false));
		}
	}

	/**
	 * Test {@link NotExpression#getNumberOfChildExpressions()} method.
	 */
	public static class GetNumberOfChildExpressions extends NotExpressionTest {

		BooleanExpression expr1 = new BooleanExpression();

		BooleanExpression expr2 = new BooleanExpression();

		BooleanExpression expr3 = new BooleanExpression();

		@Test
		public void empty() {
			NotExpression orExpression = new NotExpression();
			assertThat(orExpression.getNumberOfChildExpressions(), is(0));
		}

		@Test
		public void threeElements() {
			NotExpression orExpression = new NotExpression(expr1);
			assertThat(orExpression.getNumberOfChildExpressions(), is(1));
		}
	}
}
