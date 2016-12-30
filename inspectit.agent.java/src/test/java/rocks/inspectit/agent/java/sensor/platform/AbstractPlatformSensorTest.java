package rocks.inspectit.agent.java.sensor.platform;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.shared.all.communication.SystemSensorData;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test class for {@link AbstractPlatformSensor}.
 *
 * @author Max Wassiljew (NovaTec Consulting GmbH)
 */
@SuppressWarnings("PMD")
public class AbstractPlatformSensorTest extends TestBase {

	/** Class under test. */
	@InjectMocks
	AbstractPlatformSensorFakeImpl cut;

	/** The mocked {@link IConfigurationStorage}. */
	@Mock
	IConfigurationStorage configurationStorage;

	/** The mocked {@link IPlatformManager}. */
	@Mock
	IPlatformManager platformManager;

	/** The mocked {@link PlatformSensorTypeConfig}. */
	@Mock
	PlatformSensorTypeConfig sensorTypeConfig;

	/** The mocked {@link SystemSensorData}. */
	@Mock
	SystemSensorData systemSensorData;

	/** The mocked {@link Logger}. */
	@Mock
	Logger log;

	/**
	 * Tests the {@link AbstractPlatformSensor#afterPropertiesSet()}.
	 *
	 * @author Max Wassiljew (NovaTec Consulting GmbH)
	 */
	public static class AfterPropertiesSet extends AbstractPlatformSensorTest {

		@Test
		void noPlatformSensorTypeConfigFound() throws Exception {
			PlatformSensorTypeConfig platformSensorTypeConfigA = Mockito.mock(PlatformSensorTypeConfig.class);

			when(platformSensorTypeConfigA.getClassName()).thenReturn("A");
			when(this.configurationStorage.getPlatformSensorTypes()).thenReturn(Arrays.asList(platformSensorTypeConfigA));

			this.cut.afterPropertiesSet();

			verifyZeroInteractions(this.sensorTypeConfig);
		}

		@Test
		void platformSensorTypeConfigFound() throws Exception {
			PlatformSensorTypeConfig platformSensorTypeConfigA = Mockito.mock(PlatformSensorTypeConfig.class);
			PlatformSensorTypeConfig platformSensorTypeConfigB = Mockito.mock(PlatformSensorTypeConfig.class);

			when(platformSensorTypeConfigA.getClassName()).thenReturn("A");
			when(platformSensorTypeConfigB.getClassName()).thenReturn("rocks.inspectit.agent.java.sensor.platform.AbstractPlatformSensorFakeImpl");
			when(this.configurationStorage.getPlatformSensorTypes()).thenReturn(Arrays.asList(platformSensorTypeConfigA, platformSensorTypeConfigB));

			this.cut.afterPropertiesSet();

			assertThat(this.cut.getSensorTypeConfig(), sameInstance(platformSensorTypeConfigB));
		}

		@Test
		void initExistingPlatformSensorData() throws Exception {
			PlatformSensorTypeConfig platformSensorTypeConfigA = Mockito.mock(PlatformSensorTypeConfig.class);

			when(platformSensorTypeConfigA.getClassName()).thenReturn("rocks.inspectit.agent.java.sensor.platform.AbstractPlatformSensorFakeImpl");
			when(this.configurationStorage.getPlatformSensorTypes()).thenReturn(Arrays.asList(platformSensorTypeConfigA));
			when(this.platformManager.getPlatformId()).thenReturn(1337L);
			when(platformSensorTypeConfigA.getId()).thenReturn(73L);

			this.cut.afterPropertiesSet();

			SystemSensorData systemSensorData = this.cut.getSystemSensorData();

			verify(systemSensorData).setPlatformIdent(1337L);
			verify(systemSensorData).setSensorTypeIdent(73L);
		}

	}
}
