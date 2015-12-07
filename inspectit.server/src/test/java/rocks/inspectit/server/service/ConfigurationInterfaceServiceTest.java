package rocks.inspectit.server.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.ConfigurationInterfaceManager;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.BusinessContextDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;

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
			ciService.addApplicationDefinition(firstAppDefinition);
			assertThat(ciService.getApplicationDefinitions(), hasSize(2));

			ciService.addApplicationDefinition(secondAppDefinition);
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test
		public void addAtPosition() throws BusinessException {
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
		@Test
		public void getApplicationDefitions() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			assertThat(ciService.getApplicationDefinitions(), hasItem(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions(), hasItem(secondAppDefinition));
		}

		@Test
		public void getApplicationDefitionsForIds() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			assertThat(ciService.getApplicationDefinition(firstApplicationId), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinition(secondApplicationId), is(secondAppDefinition));
			assertThat(ciService.getApplicationDefinition(ApplicationDefinition.DEFAULT_ID), equalTo(BusinessContextDefinition.DEFAULT_APPLICATION_DEFINITION));
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#updateApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class UpdateApplicationTest extends ConfigurationInterfaceServiceTest {
		@Test
		public void updateApplicationDefition() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			firstAppDefinition.setApplicationName("newName");
			ciService.updateApplicationDefinition(firstAppDefinition);

			assertThat(ciService.getApplicationDefinition(firstApplicationId).getApplicationName(), equalTo("newName"));
			assertThat(ciService.getApplicationDefinition(secondApplicationId).getApplicationName(), equalTo("secondAppDefinition"));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateNull() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.updateApplicationDefinition(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void updateUnknown() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.updateApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
		}
	}

	/**
	 * Test
	 * {@link ConfigurationInterfaceService#moveApplicationDefinition(ApplicationDefinition, int)}
	 * method.
	 */
	public static class MoveApplicationTest extends ConfigurationInterfaceServiceTest {
		@Test
		public void moveApplicationDefinitionUp() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.moveApplicationDefinition(secondAppDefinition, 0);

			assertThat(ciService.getApplicationDefinitions().get(0), is(secondAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(firstAppDefinition));
		}

		@Test
		public void moveApplicationDefinitionDown() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.moveApplicationDefinition(firstAppDefinition, 1);

			assertThat(ciService.getApplicationDefinitions().get(0), is(secondAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(firstAppDefinition));
		}

		@Test
		public void moveToSameIndex() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.moveApplicationDefinition(secondAppDefinition, 1);

			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), is(secondAppDefinition));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveToFar() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.moveApplicationDefinition(secondAppDefinition, 2);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveNull() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.moveApplicationDefinition(null, 0);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void moveUnknown() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.moveApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null), 0);
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#deleteApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class DeleteApplicationTest extends ConfigurationInterfaceServiceTest {
		@Test
		public void deleteApplicationDefition() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.deleteApplicationDefinition(secondAppDefinition);

			assertThat(ciService.getApplicationDefinitions(), hasSize(2));
			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), equalTo(BusinessContextDefinition.DEFAULT_APPLICATION_DEFINITION));

			ciService.deleteApplicationDefinition(firstAppDefinition);

			assertThat(ciService.getApplicationDefinitions(), hasSize(1));
			assertThat(ciService.getApplicationDefinitions().get(0), equalTo(BusinessContextDefinition.DEFAULT_APPLICATION_DEFINITION));
		}

		@Test
		public void deleteNull() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.deleteApplicationDefinition(null);
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteUnknown() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.deleteApplicationDefinition(new ApplicationDefinition(123456789, "unknown", null));
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteDefaultApplication() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.deleteApplicationDefinition(BusinessContextDefinition.DEFAULT_APPLICATION_DEFINITION);
			assertThat(ciService.getApplicationDefinitions(), hasSize(3));
		}

		@Test
		public void deleteTwice() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.deleteApplicationDefinition(firstAppDefinition);
			ciService.deleteApplicationDefinition(firstAppDefinition);
			assertThat(ciService.getApplicationDefinitions(), hasSize(2));
		}
	}
}
