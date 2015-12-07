/**
 *
 */
package info.novatec.inspectit.ci.business.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class ApplicationDefinitionTest extends TestBase {

	@InjectMocks
	ApplicationDefinition applicationDefinition;

	final int firstBusinessTxDefinitionId = 10;
	final int secondBusinessTxDefinitionId = 20;

	BusinessTransactionDefinition firstBusinessTxDefinition;
	BusinessTransactionDefinition secondBusinessTxDefinition;

	@BeforeMethod
	public void init() {
		firstBusinessTxDefinition = new BusinessTransactionDefinition(firstBusinessTxDefinitionId, "firstBusinessTxDefinition", null);
		secondBusinessTxDefinition = new BusinessTransactionDefinition(secondBusinessTxDefinitionId, "secondBusinessTxDefinition", null);
	}

	/**
	 * Test
	 * {@link ApplicationDefinition#addBusinessTransactionDefinition(BusinessTransactionDefinition)}
	 * and
	 * {@link ApplicationDefinition#addBusinessTransactionDefinition(BusinessTransactionDefinition, int)}
	 * methods.
	 */
	public static class AddBusinessTransactionDefinitionTest extends ApplicationDefinitionTest {
		@Test
		public void addAtTheEnd() throws BusinessException {
			// contains default application
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(1));

			applicationDefinition.addBusinessTransactionDefinition(firstBusinessTxDefinition);
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(2));

			applicationDefinition.addBusinessTransactionDefinition(secondBusinessTxDefinition);
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(secondBusinessTxDefinition));
		}

		@Test
		public void addAtPosition() throws BusinessException {
			// contains default application
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(1));

			applicationDefinition.addBusinessTransactionDefinition(firstBusinessTxDefinition);
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(2));

			applicationDefinition.addBusinessTransactionDefinition(secondBusinessTxDefinition, 0);
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(secondBusinessTxDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addNull() throws BusinessException {
			applicationDefinition.addBusinessTransactionDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addDuplicate() throws BusinessException {
			applicationDefinition.addBusinessTransactionDefinition(firstBusinessTxDefinition);
			applicationDefinition.addBusinessTransactionDefinition(firstBusinessTxDefinition);
		}
	}

	/**
	 * Test {@link ApplicationDefinition#getBusinessTransactionDefinition(int)} and
	 * {@link ApplicationDefinition#getBusinessTransactionDefinition(int)} methods.
	 */
	public static class GetApplicationTest extends ApplicationDefinitionTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			applicationDefinition.addBusinessTransactionDefinition(firstBusinessTxDefinition);
			applicationDefinition.addBusinessTransactionDefinition(secondBusinessTxDefinition);
		}

		@Test
		public void getBusinessTransactionDefitions() {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasItem(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasItem(secondBusinessTxDefinition));
		}

		@Test
		public void getBusinessTransactionsForIds() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinition(firstBusinessTxDefinitionId), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinition(secondBusinessTxDefinitionId), is(secondBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinition(BusinessTransactionDefinition.DEFAULT_ID), is(applicationDefinition.getDefaultBusinessTransactionDefinition()));
		}
	}

	/**
	 * Test
	 * {@link ApplicationDefinition#moveBusinessTransactionDefinition(BusinessTransactionDefinition, int)}
	 * method.
	 */
	public static class MoveApplicationTest extends ApplicationDefinitionTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			applicationDefinition.addBusinessTransactionDefinition(firstBusinessTxDefinition);
			applicationDefinition.addBusinessTransactionDefinition(secondBusinessTxDefinition);
		}

		@Test
		public void moveBusinessTransactionDefition() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(secondBusinessTxDefinition));

			applicationDefinition.moveBusinessTransactionDefinition(secondBusinessTxDefinition, 0);

			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(secondBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(firstBusinessTxDefinition));

			applicationDefinition.moveBusinessTransactionDefinition(secondBusinessTxDefinition, 1);

			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(secondBusinessTxDefinition));
		}

		@Test
		public void moveToSameIndex() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(secondBusinessTxDefinition));

			applicationDefinition.moveBusinessTransactionDefinition(secondBusinessTxDefinition, 1);

			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(secondBusinessTxDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveToFar() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(secondBusinessTxDefinition));

			applicationDefinition.moveBusinessTransactionDefinition(secondBusinessTxDefinition, 2);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveNull() throws BusinessException {
			applicationDefinition.moveBusinessTransactionDefinition(null, 0);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveUnknown() throws BusinessException {
			applicationDefinition.moveBusinessTransactionDefinition(new BusinessTransactionDefinition(123456789, "unknown", null), 0);
		}
	}

	/**
	 * Test
	 * {@link ApplicationDefinition#deleteBusinessTransactionDefinition(BusinessTransactionDefinition)}
	 * method.
	 */
	public static class DeleteApplicationTest extends ApplicationDefinitionTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			applicationDefinition.addBusinessTransactionDefinition(firstBusinessTxDefinition);
			applicationDefinition.addBusinessTransactionDefinition(secondBusinessTxDefinition);
		}

		@Test
		public void deleteBusinessTransactionDefinition() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(secondBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(2), is(applicationDefinition.getDefaultBusinessTransactionDefinition()));

			applicationDefinition.deleteBusinessTransactionDefinition(secondBusinessTxDefinition);

			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(2));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(firstBusinessTxDefinition));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(1), is(applicationDefinition.getDefaultBusinessTransactionDefinition()));

			applicationDefinition.deleteBusinessTransactionDefinition(firstBusinessTxDefinition);

			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(1));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions().get(0), is(applicationDefinition.getDefaultBusinessTransactionDefinition()));
		}

		@Test
		public void deleteNull() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
			applicationDefinition.deleteBusinessTransactionDefinition(null);
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
		}

		@Test
		public void deleteUnknown() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
			applicationDefinition.deleteBusinessTransactionDefinition(new BusinessTransactionDefinition(123456789, "unknown", null));
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
		}

		@Test
		public void deleteDefaultBusinessTransaction() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
			applicationDefinition.deleteBusinessTransactionDefinition(applicationDefinition.getDefaultBusinessTransactionDefinition());
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
		}

		@Test
		public void deleteTwice() throws BusinessException {
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(3));
			applicationDefinition.deleteBusinessTransactionDefinition(firstBusinessTxDefinition);
			applicationDefinition.deleteBusinessTransactionDefinition(firstBusinessTxDefinition);
			assertThat(applicationDefinition.getBusinessTransactionDefinitions(), hasSize(2));
		}
	}
}
