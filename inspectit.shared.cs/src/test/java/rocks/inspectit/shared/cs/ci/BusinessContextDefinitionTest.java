package rocks.inspectit.shared.cs.ci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
public class BusinessContextDefinitionTest extends TestBase {

	@InjectMocks
	BusinessContextDefinition businessContextDefinition;

	final int firstApplicationId = 10;
	final int secondApplicationId = 20;

	ApplicationDefinition firstAppDefinition;
	ApplicationDefinition secondAppDefinition;

	@BeforeMethod
	public void init() {
		firstAppDefinition = new ApplicationDefinition(firstApplicationId, "firstAppDefinition", null);
		secondAppDefinition = new ApplicationDefinition(secondApplicationId, "secondAppDefinition", null);
	}

	/**
	 * Test {@link BusinessContextDefinition#addApplicationDefinition(ApplicationDefinition)} and
	 * {@link BusinessContextDefinition#addApplicationDefinition(ApplicationDefinition, int)}
	 * methods.
	 */
	public static class AddApplicationDefinition extends BusinessContextDefinitionTest {
		@Test
		public void addAtTheEnd() throws BusinessException {
			// contains default application
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(1));

			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(2));

			businessContextDefinition.addApplicationDefinition(secondAppDefinition);
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test
		public void addAtPosition() throws BusinessException {
			// contains default application
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(1));

			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(2));

			businessContextDefinition.addApplicationDefinition(secondAppDefinition, 0);
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(secondAppDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addAtNegative() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(firstAppDefinition, -1);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addAtInvalidGreatIndex() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(firstAppDefinition, businessContextDefinition.getApplicationDefinitions().size());
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addNull() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addDuplicate() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
		}
	}

	/**
	 * Test {@link BusinessContextDefinition#getApplicationDefinitions()} and
	 * {@link BusinessContextDefinition#getApplicationDefinition(int)} methods.
	 */
	public static class GetApplicationDefinition extends BusinessContextDefinitionTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
			businessContextDefinition.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void getApplicationDefitions() {
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasItem(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasItem(secondAppDefinition));
		}

		@Test
		public void getApplicationDefitionsForIds() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinition(firstApplicationId), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinition(secondApplicationId), is(secondAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinition(ApplicationDefinition.DEFAULT_ID), is(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void getInvalidApplication() throws BusinessException {
			businessContextDefinition.getApplicationDefinition(1000);
		}
	}

	/**
	 * Test {@link BusinessContextDefinition#updateApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class UpdateApplicationDefinition extends BusinessContextDefinitionTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
			businessContextDefinition.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void updateApplicationDefition() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("firstAppDefinition"));
			assertThat(businessContextDefinition.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));

			firstAppDefinition.setApplicationName("newName");
			businessContextDefinition.updateApplicationDefinition(firstAppDefinition);

			assertThat(businessContextDefinition.getApplicationDefinition(firstApplicationId).getRevision(), is(2));
			assertThat(businessContextDefinition.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("newName"));
			assertThat(businessContextDefinition.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateNull() throws BusinessException {
			businessContextDefinition.updateApplicationDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateUnknown() throws BusinessException {
			businessContextDefinition.updateApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateWithWrongRevision() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("firstAppDefinition"));
			assertThat(businessContextDefinition.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));

			Kryo kryo = new Kryo();
			ApplicationDefinition firstAppDefinitionCopy_1 = kryo.copy(firstAppDefinition);
			ApplicationDefinition firstAppDefinitionCopy_2 = kryo.copy(firstAppDefinition);

			firstAppDefinitionCopy_1.setApplicationName("newName_1");
			firstAppDefinitionCopy_2.setApplicationName("newName_2");
			businessContextDefinition.updateApplicationDefinition(firstAppDefinitionCopy_1);
			businessContextDefinition.updateApplicationDefinition(firstAppDefinitionCopy_2);
		}
	}

	/**
	 * Test {@link BusinessContextDefinition#moveApplicationDefinition(ApplicationDefinition, int)}
	 * method.
	 */
	public static class MoveApplicationDefinition extends BusinessContextDefinitionTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
			businessContextDefinition.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void moveApplicationDefinition() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));

			businessContextDefinition.moveApplicationDefinition(secondAppDefinition, 0);

			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(secondAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(firstAppDefinition));

			businessContextDefinition.moveApplicationDefinition(secondAppDefinition, 1);

			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test
		public void moveToSameIndex() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));

			businessContextDefinition.moveApplicationDefinition(secondAppDefinition, 1);

			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveToFar() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));

			businessContextDefinition.moveApplicationDefinition(secondAppDefinition, 2);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveToNegativeIndex() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));

			businessContextDefinition.moveApplicationDefinition(secondAppDefinition, -1);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveNull() throws BusinessException {
			businessContextDefinition.moveApplicationDefinition(null, 0);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveUnknown() throws BusinessException {
			businessContextDefinition.moveApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null), 0);
		}
	}

	/**
	 * Test {@link BusinessContextDefinition#deleteApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class DeleteApplicationDefinition extends BusinessContextDefinitionTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextDefinition.addApplicationDefinition(firstAppDefinition);
			businessContextDefinition.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void deleteApplicationDefition() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(secondAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(2), is(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION));

			businessContextDefinition.deleteApplicationDefinition(secondAppDefinition);

			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(2));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(1), is(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION));

			businessContextDefinition.deleteApplicationDefinition(firstAppDefinition);

			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(1));
			assertThat(businessContextDefinition.getApplicationDefinitions().get(0), is(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION));
		}

		@Test
		public void deleteNull() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
			businessContextDefinition.deleteApplicationDefinition(null);
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteUnknown() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
			businessContextDefinition.deleteApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteDefaultApplication() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
			businessContextDefinition.deleteApplicationDefinition(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION);
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteTwice() throws BusinessException {
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(3));
			businessContextDefinition.deleteApplicationDefinition(firstAppDefinition);
			businessContextDefinition.deleteApplicationDefinition(firstAppDefinition);
			assertThat(businessContextDefinition.getApplicationDefinitions(), hasSize(2));
		}
	}
}
