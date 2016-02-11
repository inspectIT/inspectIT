package info.novatec.inspectit.cmr.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.cmr.cache.impl.AtomicBuffer;
import info.novatec.inspectit.cmr.cache.impl.BufferProperties;
import info.novatec.inspectit.cmr.property.configuration.Configuration;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Test that some of the properties updates are working fine in integration mode.
 * <p>
 * Only important properties changes are taken into account here.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class PropertyIntegrationTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	private PropertyManager propertyManager;

	@Autowired
	private BufferProperties bufferProperties;

	@Autowired
	private AtomicBuffer<?> buffer;

	@Test
	public void increaseBufferSizeWithOccupancy() throws Exception {
		Configuration configuration = propertyManager.getConfiguration();
		SingleProperty<Float> maxOldSpaceOcc = configuration.forLogicalName("buffer.maxOldSpaceOccupancy");
		SingleProperty<Float> minOldSpaceOcc = configuration.forLogicalName("buffer.minOldSpaceOccupancy");
		long oldBufferSize = bufferProperties.getInitialBufferSize();

		ConfigurationUpdate configurationUpdate = new ConfigurationUpdate();
		configurationUpdate.addPropertyUpdate(maxOldSpaceOcc.createAndValidatePropertyUpdate(Float.valueOf(maxOldSpaceOcc.getValue().floatValue() + 0.05f)));
		configurationUpdate.addPropertyUpdate(minOldSpaceOcc.createAndValidatePropertyUpdate(Float.valueOf(minOldSpaceOcc.getValue().floatValue() + 0.05f)));

		propertyManager.updateConfiguration(configurationUpdate, false);
		long newBufferSize = bufferProperties.getInitialBufferSize();
		assertThat(newBufferSize, is(greaterThan(oldBufferSize)));
		assertThat(newBufferSize, is(buffer.getMaxSize()));
	}

	@Test
	public void increaseExpansionRate() throws Exception {
		long bufferSize = bufferProperties.getInitialBufferSize();

		Configuration configuration = propertyManager.getConfiguration();
		SingleProperty<Float> maxExpansionRate = configuration.forLogicalName("buffer.maxObjectExpansionRate");
		float oldExpansionrate = bufferProperties.getObjectSecurityExpansionRate(bufferSize);

		ConfigurationUpdate configurationUpdate = new ConfigurationUpdate();
		configurationUpdate.addPropertyUpdate(maxExpansionRate.createAndValidatePropertyUpdate(Float.valueOf(maxExpansionRate.getValue().floatValue() + 0.1f)));

		propertyManager.updateConfiguration(configurationUpdate, false);
		float newExpansionrate = bufferProperties.getObjectSecurityExpansionRate(bufferSize);
		assertThat(newExpansionrate, is(greaterThan(oldExpansionrate)));
	}

	@AfterMethod
	public void deleteUpdateFiles() throws IOException {
		Files.deleteIfExists(propertyManager.getConfigurationUpdatePath());
	}

}
