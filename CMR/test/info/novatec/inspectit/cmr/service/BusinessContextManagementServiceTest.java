/**
 *
 */
package info.novatec.inspectit.cmr.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.BusinessContextDefinition;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.cmr.ci.ConfigurationInterfaceManager;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.testbase.TestBase;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class BusinessContextManagementServiceTest extends TestBase {

	@InjectMocks
	BusinessContextManagementService businessContextManagementService;

	@Mock
	ConfigurationInterfaceManager ciManager;

	final int firstApplicationId = 10;
	final int secondApplicationId = 20;

	ApplicationDefinition firstAppDefinition;
	ApplicationDefinition secondAppDefinition;
	BusinessContextDefinition businessContextDef;

	@BeforeMethod
	public void init() throws BusinessException, JAXBException, IOException {
		MockitoAnnotations.initMocks(this);
		firstAppDefinition = new ApplicationDefinition(firstApplicationId, "firstAppDefinition", null);
		secondAppDefinition = new ApplicationDefinition(secondApplicationId, "secondAppDefinition", null);
		businessContextDef = new BusinessContextDefinition();
		when(ciManager.getBusinessconContextDefinition()).thenReturn(businessContextDef);
		when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenReturn(businessContextDef);
	}

	/**
	 * Test {@link BusinessContextManagementService#addApplicationDefinition(ApplicationDefinition)}
	 * and
	 * {@link BusinessContextManagementService#addApplicationDefinition(ApplicationDefinition, int)}
	 * methods.
	 */
	public static class AddApplicationTest extends BusinessContextManagementServiceTest {
		@Test
		public void addAtTheEnd() throws BusinessException {
			// contains default application
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(1));

			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(2));

			businessContextManagementService.addApplicationDefinition(secondAppDefinition);
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test
		public void addAtPosition() throws BusinessException {
			// contains default application
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(1));

			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(2));

			businessContextManagementService.addApplicationDefinition(secondAppDefinition, 0);
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(secondAppDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addNull() throws BusinessException {
			businessContextManagementService.addApplicationDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addDuplicate() throws BusinessException {
			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
		}
	}

	/**
	 * Test {@link BusinessContextManagementService#getApplicationDefinitions()} and
	 * {@link BusinessContextManagementService#getApplicationDefinition(int)} methods.
	 */
	public static class GetApplicationTest extends BusinessContextManagementServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
			businessContextManagementService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void getApplicationDefitions() {
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasItem(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasItem(secondAppDefinition));
		}

		@Test
		public void getApplicationDefitionsForIds() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinition(firstApplicationId), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinition(secondApplicationId), is(secondAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinition(ApplicationDefinition.DEFAULT_ID), equalTo(businessContextDef.getDefaultApplicationDefinition()));
		}
	}

	/**
	 * Test
	 * {@link BusinessContextManagementService#updateApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class UpdateApplicationTest extends BusinessContextManagementServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
			businessContextManagementService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void updateApplicationDefition() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("firstAppDefinition"));
			assertThat(businessContextManagementService.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));

			firstAppDefinition.setApplicationName("newName");
			businessContextManagementService.updateApplicationDefinition(firstAppDefinition);

			assertThat(businessContextManagementService.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("newName"));
			assertThat(businessContextManagementService.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateNull() throws BusinessException {
			businessContextManagementService.updateApplicationDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateUnknown() throws BusinessException {
			businessContextManagementService.updateApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
		}
	}

	/**
	 * Test
	 * {@link BusinessContextManagementService#moveApplicationDefinition(ApplicationDefinition, int)}
	 * method.
	 */
	public static class MoveApplicationTest extends BusinessContextManagementServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
			businessContextManagementService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void moveApplicationDefinition() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(secondAppDefinition));

			businessContextManagementService.moveApplicationDefinition(secondAppDefinition, 0);

			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(secondAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(firstAppDefinition));

			businessContextManagementService.moveApplicationDefinition(secondAppDefinition, 1);

			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test
		public void moveToSameIndex() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(secondAppDefinition));

			businessContextManagementService.moveApplicationDefinition(secondAppDefinition, 1);

			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveToFar() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(secondAppDefinition));

			businessContextManagementService.moveApplicationDefinition(secondAppDefinition, 2);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveNull() throws BusinessException {
			businessContextManagementService.moveApplicationDefinition(null, 0);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveUnknown() throws BusinessException {
			businessContextManagementService.moveApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null), 0);
		}
	}

	/**
	 * Test
	 * {@link BusinessContextManagementService#deleteApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class DeleteApplicationTest extends BusinessContextManagementServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			businessContextManagementService.addApplicationDefinition(firstAppDefinition);
			businessContextManagementService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void deleteApplicationDefition() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), is(secondAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(2), equalTo(businessContextDef.getDefaultApplicationDefinition()));

			businessContextManagementService.deleteApplicationDefinition(secondAppDefinition);

			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(2));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(1), equalTo(businessContextDef.getDefaultApplicationDefinition()));

			businessContextManagementService.deleteApplicationDefinition(firstAppDefinition);

			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(1));
			assertThat(businessContextManagementService.getApplicationDefinitions().get(0), equalTo(businessContextDef.getDefaultApplicationDefinition()));
		}

		@Test
		public void deleteNull() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
			businessContextManagementService.deleteApplicationDefinition(null);
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteUnknown() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
			businessContextManagementService.deleteApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteDefaultApplication() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
			businessContextManagementService.deleteApplicationDefinition(businessContextDef.getDefaultApplicationDefinition());
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteTwice() throws BusinessException {
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(3));
			businessContextManagementService.deleteApplicationDefinition(firstAppDefinition);
			businessContextManagementService.deleteApplicationDefinition(firstAppDefinition);
			assertThat(businessContextManagementService.getApplicationDefinitions(), hasSize(2));
		}
	}
}
