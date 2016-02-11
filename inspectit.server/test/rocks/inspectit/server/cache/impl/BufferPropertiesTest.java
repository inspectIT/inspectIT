package info.novatec.inspectit.cmr.cache.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.Mockito.mock;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;

import org.slf4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Testing the calculations inside of {@link BufferProperties} class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class BufferPropertiesTest extends AbstractTestNGLogSupport {

	/**
	 * Buffer properties to test.
	 */
	private BufferProperties bufferProperties;

	/**
	 * Initialization of the buffer properties.
	 * 
	 * @throws Exception
	 *             if an exception is thrown while executing the post construct method in the buffer
	 *             properties.
	 */
	@BeforeClass
	public void init() throws Exception {
		bufferProperties = new BufferProperties();
		bufferProperties.log = mock(Logger.class);
		bufferProperties.bytesMaintenancePercentage = 0.2f;
		bufferProperties.evictionFragmentSizePercentage = 0.1f;
		bufferProperties.evictionOccupancyPercentage = 0.8f;
		bufferProperties.indexingTreeCleaningThreads = 1;
		bufferProperties.indexingWaitTime = 500l;
		bufferProperties.maxObjectExpansionRate = 0.05f;
		bufferProperties.maxObjectExpansionRateActiveTillBufferSize = 10;
		bufferProperties.maxObjectExpansionRateActiveFromOccupancy = 0.75f;
		bufferProperties.maxOldSpaceOccupancy = 0.9f;
		bufferProperties.maxOldSpaceOccupancyActiveFromOldGenSize = 1024 * 1024 * 100;
		bufferProperties.minObjectExpansionRate = 0.02f;
		bufferProperties.minObjectExpansionRateActiveFromBufferSize = 100;
		bufferProperties.minObjectExpansionRateActiveTillOccupancy = 0.35f;
		bufferProperties.minOldSpaceOccupancy = 0.2f;
		bufferProperties.minOldSpaceOccupancyActiveTillOldGenSize = 1024 * 1024 * 200;
		bufferProperties.postConstruct();
	}

	/**
	 * General Parameterized test to assure that no matter how big buffer size is, expansion rate
	 * will be between min and max related.
	 * 
	 * @param bufferSize
	 *            Buffer size.
	 */
	@Test(dataProvider = "Buffer-Size-Provider")
	public void parametrizedExpansionRateTest(long bufferSize) {
		float expansionRate = bufferProperties.getObjectSecurityExpansionRate(bufferSize);
		assertThat(expansionRate, is(lessThanOrEqualTo(bufferProperties.getMaxObjectExpansionRate())));
		assertThat(expansionRate, is(greaterThanOrEqualTo(bufferProperties.getMinObjectExpansionRate())));

		float relatedToSize = bufferProperties.getObjectSecurityExpansionRateBufferSize(bufferSize);
		float relatedToOccupancy = bufferProperties.getObjectSecurityExpansionRateBufferOccupancy(bufferSize, bufferProperties.getOldGenMax());
		assertThat(expansionRate, is(equalTo((relatedToSize + relatedToOccupancy) / 2)));
	}

	/**
	 * Parameterized test to assure that no matter how big buffer size is, expansion rate will be
	 * between min and max related to buffers size.
	 * 
	 * @param bufferSize
	 *            Buffer size.
	 */
	@Test(dataProvider = "Buffer-Size-Provider")
	public void parametrizedExpansionRateTestBufferSize(long bufferSize) {
		float expansionRate = bufferProperties.getObjectSecurityExpansionRateBufferSize(bufferSize);
		assertThat(expansionRate, is(lessThanOrEqualTo(bufferProperties.getMaxObjectExpansionRate())));
		assertThat(expansionRate, is(greaterThanOrEqualTo(bufferProperties.getMinObjectExpansionRate())));

		if (bufferSize > bufferProperties.getMaxObjectExpansionRateActiveTillBufferSize() && bufferSize < bufferProperties.getMinObjectExpansionRateActiveFromBufferSize()) {
			assertThat(expansionRate, is(lessThan(bufferProperties.getMaxObjectExpansionRate())));
			assertThat(expansionRate, is(greaterThan(bufferProperties.getMinObjectExpansionRate())));
		}
	}

	/**
	 * Parameterized test to assure that no matter how big buffer size is, expansion rate will be
	 * between min and max related to buffer occupancy.
	 * 
	 * @param bufferSize
	 *            Buffer size.
	 */
	@Test(dataProvider = "Buffer-Size-Provider")
	public void parametrizedExpansionRateTestBufferOccupancy(long bufferSize) {
		float expansionRate = bufferProperties.getObjectSecurityExpansionRateBufferOccupancy(bufferSize, bufferProperties.getOldGenMax());
		assertThat(expansionRate, is(lessThanOrEqualTo(bufferProperties.getMaxObjectExpansionRate())));
		assertThat(expansionRate, is(greaterThanOrEqualTo(bufferProperties.getMinObjectExpansionRate())));

		long maxOldGen = bufferProperties.getOldGenMax();
		float occupancy = bufferSize / maxOldGen;

		if (occupancy > bufferProperties.getMinObjectExpansionRateActiveTillOccupancy() && occupancy < bufferProperties.getMaxObjectExpansionRateActiveFromOccupancy()) {
			assertThat(expansionRate, is(lessThan(bufferProperties.getMaxObjectExpansionRate())));
			assertThat(expansionRate, is(greaterThan(bufferProperties.getMinObjectExpansionRate())));
		}
	}

	/**
	 * Single expansion rate test for buffer size.
	 */
	@Test
	public void singleExpansionRateTestBufferSize() {
		long bufferSize = bufferProperties.getMaxObjectExpansionRateActiveTillBufferSize()
				+ (bufferProperties.getMinObjectExpansionRateActiveFromBufferSize() - bufferProperties.getMaxObjectExpansionRateActiveTillBufferSize()) / 2;
		float expansionRate = bufferProperties.getObjectSecurityExpansionRateBufferSize(bufferSize);
		float expectedRate = bufferProperties.getMinObjectExpansionRate() + (bufferProperties.getMaxObjectExpansionRate() - bufferProperties.getMinObjectExpansionRate()) / 2;
		assertThat(expansionRate, is(equalTo(expectedRate)));
	}

	/**
	 * Single expansion rate test for buffer occupancy.
	 */
	@Test
	public void singleExpansionRateTestBufferOccupancy() {
		long oldGenMax = 100;
		long bufferSize = (long) (oldGenMax * (bufferProperties.maxObjectExpansionRateActiveFromOccupancy - (bufferProperties.getMaxObjectExpansionRateActiveFromOccupancy() - bufferProperties
				.getMinObjectExpansionRateActiveTillOccupancy()) / 2));

		float expansionRate = bufferProperties.getObjectSecurityExpansionRateBufferOccupancy(bufferSize, oldGenMax);
		float expectedRate = bufferProperties.getMinObjectExpansionRate() + (bufferProperties.getMaxObjectExpansionRate() - bufferProperties.getMinObjectExpansionRate()) / 2;
		assertThat(expansionRate, is(equalTo(expectedRate)));
	}

	/**
	 * Parameters generation for {@link #parametrizedExpansionRateTest(long)}.
	 * 
	 * @return Buffer size.
	 */
	@DataProvider(name = "Buffer-Size-Provider")
	public Object[][] bufferSizeParameterProvider() {
		int size = 50;
		Object[][] parameters = new Object[size][1];
		for (int i = 0; i < size; i++) {
			parameters[i][0] = getRandomLong(2000000000L);
		}
		return parameters;
	}

	/**
	 * Returns random positive long number smaller than given max value.
	 * 
	 * @param max
	 *            Max value.
	 * @return Long.
	 */
	private long getRandomLong(long max) {
		long value = (long) (Math.random() * max);
		return value - value % 10 + 10;
	}

}
