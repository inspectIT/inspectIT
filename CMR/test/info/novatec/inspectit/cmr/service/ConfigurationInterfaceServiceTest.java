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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class ConfigurationInterfaceServiceTest extends TestBase {

	@InjectMocks
	ConfigurationInterfaceService ciService;

	@Mock
	ConfigurationInterfaceManager ciManager;

	final int firstApplicationId = 10;
	final int secondApplicationId = 20;

	ApplicationDefinition firstAppDefinition;
	ApplicationDefinition secondAppDefinition;
	BusinessContextDefinition businessContextDef;

	@BeforeMethod
	public void init() throws BusinessException, JAXBException, IOException {
		firstAppDefinition = new ApplicationDefinition(firstApplicationId, "firstAppDefinition", null);
		secondAppDefinition = new ApplicationDefinition(secondApplicationId, "secondAppDefinition", null);
		businessContextDef = new BusinessContextDefinition();
		when(ciManager.getBusinessconContextDefinition()).thenReturn(businessContextDef);
		when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenReturn(businessContextDef);
	}

	/**
	 * Test {@link ConfigurationInterfaceService#addApplicationDefinition(ApplicationDefinition)}
	 * and
	 * {@link ConfigurationInterfaceService#addApplicationDefinition(ApplicationDefinition, int)}
	 * methods.
	 */
	public static class AddApplicationTest extends ConfigurationInterfaceServiceTest {
		@Test
		public void addAtTheEnd() throws BusinessException {
			// contains default application
			assertThat(ciService.getApplicationDefinitions(), hasSize(1));

			ciService.addApplicationDefinition(firstAppDefinition);
			assertThat(ciService.getApplicationDefinitions(), hasSize(2));

			ciService.addApplicationDefinition(secondAppDefinition);
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test
		public void addAtPosition() throws BusinessException {
			// contains default application
			assertThat(ciService.getApplicationDefinitions(), hasSize(1));

			ciService.addApplicationDefinition(firstAppDefinition);
			assertThat(ciService.getApplicationDefinitions(), hasSize(2));

			ciService.addApplicationDefinition(secondAppDefinition, 0);
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			assertThat(ciService.getApplicationDefinitions().get(0), is(secondAppDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addNull() throws BusinessException {
			ciService.addApplicationDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void addDuplicate() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(firstAppDefinition);
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#getApplicationDefinitions()} and
	 * {@link ConfigurationInterfaceService#getApplicationDefinition(int)} methods.
	 */
	public static class GetApplicationTest extends ConfigurationInterfaceServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void getApplicationDefitions() {
			assertThat(ciService.getApplicationDefinitions(), hasItem(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions(), hasItem(secondAppDefinition));
		}

		@Test
		public void getApplicationDefitionsForIds() throws BusinessException {
			assertThat(ciService.getApplicationDefinition(firstApplicationId), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinition(secondApplicationId), is(secondAppDefinition));
			assertThat(ciService.getApplicationDefinition(ApplicationDefinition.DEFAULT_ID), equalTo(businessContextDef.getDefaultApplicationDefinition()));
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#updateApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class UpdateApplicationTest extends ConfigurationInterfaceServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void updateApplicationDefition() throws BusinessException {
			assertThat(ciService.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("firstAppDefinition"));
			assertThat(ciService.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));

			firstAppDefinition.setApplicationName("newName");
			ciService.updateApplicationDefinition(firstAppDefinition);

			assertThat(ciService.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("newName"));
			assertThat(ciService.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateNull() throws BusinessException {
			ciService.updateApplicationDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateUnknown() throws BusinessException {
			ciService.updateApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
		}
	}

	/**
	 * Test
	 * {@link ConfigurationInterfaceService#moveApplicationDefinition(ApplicationDefinition, int)}
	 * method.
	 */
	public static class MoveApplicationTest extends ConfigurationInterfaceServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void moveApplicationDefinition() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));

			ciService.moveApplicationDefinition(secondAppDefinition, 0);

			assertThat(ciService.getApplicationDefinitions().get(0), is(secondAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(firstAppDefinition));

			ciService.moveApplicationDefinition(secondAppDefinition, 1);

			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test
		public void moveToSameIndex() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));

			ciService.moveApplicationDefinition(secondAppDefinition, 1);

			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveToFar() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));

			ciService.moveApplicationDefinition(secondAppDefinition, 2);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveNull() throws BusinessException {
			ciService.moveApplicationDefinition(null, 0);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveUnknown() throws BusinessException {
			ciService.moveApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null), 0);
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#deleteApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class DeleteApplicationTest extends ConfigurationInterfaceServiceTest {

		@BeforeMethod
		public void initialize() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);
		}

		@Test
		public void deleteApplicationDefition() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(2), equalTo(businessContextDef.getDefaultApplicationDefinition()));

			ciService.deleteApplicationDefinition(secondAppDefinition);

			assertThat(ciService.getApplicationDefinitions(), hasSize(2));
			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), equalTo(businessContextDef.getDefaultApplicationDefinition()));

			ciService.deleteApplicationDefinition(firstAppDefinition);

			assertThat(ciService.getApplicationDefinitions(), hasSize(1));
			assertThat(ciService.getApplicationDefinitions().get(0), equalTo(businessContextDef.getDefaultApplicationDefinition()));
		}

		@Test
		public void deleteNull() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			ciService.deleteApplicationDefinition(null);
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteUnknown() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			ciService.deleteApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteDefaultApplication() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			ciService.deleteApplicationDefinition(businessContextDef.getDefaultApplicationDefinition());
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteTwice() throws BusinessException {
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			ciService.deleteApplicationDefinition(firstAppDefinition);
			ciService.deleteApplicationDefinition(firstAppDefinition);
			assertThat(ciService.getApplicationDefinitions(), hasSize(2));
		}
	}
}
