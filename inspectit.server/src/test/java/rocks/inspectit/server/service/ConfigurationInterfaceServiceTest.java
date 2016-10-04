package rocks.inspectit.server.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.ConfigurationInterfaceManager;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.TechnicalException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.BusinessContextDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
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

	final String firstAlertingDefinitionId = "firstId";
	final String secondAlertingDefinitionId = "secondId";
	AlertingDefinition firstAlertingDefinition;
	AlertingDefinition secondAlertingDefinition;

	@BeforeMethod
	public void init() throws BusinessException, JAXBException, IOException {
		// ApplicationDefinition
		firstAppDefinition = new ApplicationDefinition(firstApplicationId, "firstAppDefinition", null);
		secondAppDefinition = new ApplicationDefinition(secondApplicationId, "secondAppDefinition", null);
		businessContextDef = new BusinessContextDefinition();
		when(ciManager.getBusinessconContextDefinition()).thenReturn(businessContextDef);
		when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenReturn(businessContextDef);

		// AlertingDefinition
		firstAlertingDefinition = new AlertingDefinition();
		firstAlertingDefinition.setId(firstAlertingDefinitionId);
		secondAlertingDefinition = new AlertingDefinition();
		secondAlertingDefinition.setId(secondAlertingDefinitionId);
	}

	/**
	 * Test {@link ConfigurationInterfaceService#addApplicationDefinition(ApplicationDefinition)}
	 * and
	 * {@link ConfigurationInterfaceService#addApplicationDefinition(ApplicationDefinition, int)}
	 * methods.
	 */
	public static class AddApplicationDefinition extends ConfigurationInterfaceServiceTest {
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

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void jaxbExceptionThrown() throws BusinessException, JAXBException, IOException {
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(JAXBException.class);
			ciService.addApplicationDefinition(firstAppDefinition);
		}

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void ioExceptionThrown() throws BusinessException, JAXBException, IOException {
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(IOException.class);
			ciService.addApplicationDefinition(firstAppDefinition);
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#getApplicationDefinitions()} and
	 * {@link ConfigurationInterfaceService#getApplicationDefinition(int)} methods.
	 */
	public static class GetApplicationDefinition extends ConfigurationInterfaceServiceTest {
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
			assertThat(ciService.getApplicationDefinition(ApplicationDefinition.DEFAULT_ID), equalTo(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION));
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#updateApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class UpdateApplicationDefinition extends ConfigurationInterfaceServiceTest {
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

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void jaxbExceptionThrown() throws BusinessException, JAXBException, IOException {
			ciService.addApplicationDefinition(firstAppDefinition);
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(JAXBException.class);
			ciService.updateApplicationDefinition(firstAppDefinition);
		}

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void ioExceptionThrown() throws BusinessException, JAXBException, IOException {
			ciService.addApplicationDefinition(firstAppDefinition);
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(IOException.class);
			ciService.updateApplicationDefinition(firstAppDefinition);
		}
	}

	/**
	 * Test
	 * {@link ConfigurationInterfaceService#moveApplicationDefinition(ApplicationDefinition, int)}
	 * method.
	 */
	public static class MoveApplicationDefinition extends ConfigurationInterfaceServiceTest {
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

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void jaxbExceptionThrown() throws BusinessException, JAXBException, IOException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(JAXBException.class);

			ciService.moveApplicationDefinition(secondAppDefinition, 0);
		}

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void ioExceptionThrown() throws BusinessException, JAXBException, IOException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(IOException.class);

			ciService.moveApplicationDefinition(secondAppDefinition, 0);
		}
	}

	/**
	 * Test {@link ConfigurationInterfaceService#deleteApplicationDefinition(ApplicationDefinition)}
	 * method.
	 */
	public static class DeleteApplicationDefinition extends ConfigurationInterfaceServiceTest {
		@Test
		public void deleteApplicationDefition() throws BusinessException {
			ciService.addApplicationDefinition(firstAppDefinition);
			ciService.addApplicationDefinition(secondAppDefinition);

			ciService.deleteApplicationDefinition(secondAppDefinition);

			assertThat(ciService.getApplicationDefinitions(), hasSize(2));
			assertThat(ciService.getApplicationDefinitions().get(0), is(firstAppDefinition));
			assertThat(ciService.getApplicationDefinitions().get(1), equalTo(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION));

			ciService.deleteApplicationDefinition(firstAppDefinition);

			assertThat(ciService.getApplicationDefinitions(), hasSize(1));
			assertThat(ciService.getApplicationDefinitions().get(0), equalTo(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION));
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

			ciService.deleteApplicationDefinition(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION);
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

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void jaxbExceptionThrown() throws BusinessException, JAXBException, IOException {
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(JAXBException.class);
			ciService.deleteApplicationDefinition(firstAppDefinition);
		}

		@SuppressWarnings("unchecked")
		@Test(expectedExceptions = { TechnicalException.class })
		public void ioExceptionThrown() throws BusinessException, JAXBException, IOException {
			when(ciManager.updateBusinessContextDefinition(any(BusinessContextDefinition.class))).thenThrow(IOException.class);
			ciService.deleteApplicationDefinition(firstAppDefinition);
		}
	}

	/**
	 * Tests {@link ConfigurationInterfaceService#createAlertingDefinition(AlertingDefinition)}
	 * method.
	 */
	public static class CreateAlertingDefinition extends ConfigurationInterfaceServiceTest {
		@Test
		public void createAlertingDefinition() throws BusinessException, JAXBException, IOException {
			when(ciManager.createAlertingDefinition(firstAlertingDefinition)).thenReturn(firstAlertingDefinition);

			AlertingDefinition returnedDefinition = ciService.createAlertingDefinition(firstAlertingDefinition);

			assertThat(returnedDefinition, is(firstAlertingDefinition));
		}

		@Test(expectedExceptions = { TechnicalException.class })
		public void jaxbExceptionThrown() throws BusinessException, JAXBException, IOException {
			doThrow(JAXBException.class).when(ciManager).createAlertingDefinition(firstAlertingDefinition);
			ciService.createAlertingDefinition(firstAlertingDefinition);
		}

		@Test(expectedExceptions = { TechnicalException.class })
		public void ioExceptionThrown() throws BusinessException, JAXBException, IOException {
			doThrow(IOException.class).when(ciManager).createAlertingDefinition(firstAlertingDefinition);
			ciService.createAlertingDefinition(firstAlertingDefinition);
		}

	}

	/**
	 * Tests {@link ConfigurationInterfaceService#getAlertingDefinitions()} and
	 * {@link ConfigurationInterfaceService#getAlertingDefinition(String)} method.
	 */
	public static class GetAlertingDefinition extends ConfigurationInterfaceServiceTest {
		@Test
		public void getAlertingDefinitions() throws BusinessException, JAXBException, IOException {
			List<AlertingDefinition> definitions = Arrays.asList(firstAlertingDefinition, secondAlertingDefinition);
			when(ciManager.getAlertingDefinitions()).thenReturn(definitions);

			List<AlertingDefinition> alertingDefinitions = ciService.getAlertingDefinitions();

			assertThat(alertingDefinitions, is(definitions));
		}

		@Test
		public void getAlertingDefinition() throws BusinessException, JAXBException, IOException {
			when(ciManager.getAlertingDefinition(secondAlertingDefinitionId)).thenReturn(secondAlertingDefinition);

			AlertingDefinition alertingDefinition = ciService.getAlertingDefinition(secondAlertingDefinitionId);

			assertThat(alertingDefinition, is(secondAlertingDefinition));
		}
	}

	/**
	 * Tests {@link ConfigurationInterfaceService#updateAlertingDefinition(AlertingDefinition)}
	 * method.
	 */
	public static class UpdateAlertingDefinition extends ConfigurationInterfaceServiceTest {
		@Test
		public void updateAlertingDefinition() throws BusinessException, JAXBException, IOException {
			when(ciManager.updateAlertingDefinition(firstAlertingDefinition)).thenReturn(firstAlertingDefinition);

			AlertingDefinition alertingDefinition = ciService.updateAlertingDefinition(firstAlertingDefinition);

			assertThat(alertingDefinition, is(firstAlertingDefinition));
		}

		@Test(expectedExceptions = { TechnicalException.class })
		public void jaxbExceptionThrown() throws BusinessException, JAXBException, IOException {
			doThrow(JAXBException.class).when(ciManager).updateAlertingDefinition(firstAlertingDefinition);
			ciService.updateAlertingDefinition(firstAlertingDefinition);
		}

		@Test(expectedExceptions = { TechnicalException.class })
		public void ioExceptionThrown() throws BusinessException, JAXBException, IOException {
			doThrow(IOException.class).when(ciManager).updateAlertingDefinition(firstAlertingDefinition);
			ciService.updateAlertingDefinition(firstAlertingDefinition);
		}
	}

	/**
	 * Tests {@link ConfigurationInterfaceService#deleteAlertingDefinition(AlertingDefinition)}
	 * method.
	 */
	public static class DeleteAlertingDefinition extends ConfigurationInterfaceServiceTest {
		@Test
		public void deleteAlertingDefinition() throws BusinessException, JAXBException, IOException {
			ciService.deleteAlertingDefinition(firstAlertingDefinition);

			verify(ciManager, only()).deleteAlertingDefinition(firstAlertingDefinition);
		}

		@Test(expectedExceptions = { TechnicalException.class })
		public void ioExceptionThrown() throws BusinessException, JAXBException, IOException {
			doThrow(IOException.class).when(ciManager).deleteAlertingDefinition(firstAlertingDefinition);
			ciService.deleteAlertingDefinition(firstAlertingDefinition);
		}
	}
}
