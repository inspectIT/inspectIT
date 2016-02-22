package rocks.inspectit.shared.cs.cmr.service.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * Testing the caching abilities of {@link CachedDataService}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class CachedDataServiceTest {

	/**
	 * Class under test.
	 */
	private CachedDataService cachedDataService;

	@Mock
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Init.
	 */
	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		cachedDataService = new CachedDataService(globalDataAccessService);
	}

	/**
	 * Test that caching is working.
	 */
	@Test
	public void cacheWorks() throws BusinessException {
		long platformId = 10L;
		long sensorId = 20L;
		long methodSensorId = 30L;

		PlatformIdent platformIdent = mock(PlatformIdent.class);
		when(platformIdent.getId()).thenReturn(platformId);

		SensorTypeIdent sensorType = mock(SensorTypeIdent.class);
		when(sensorType.getId()).thenReturn(sensorId);
		when(platformIdent.getSensorTypeIdents()).thenReturn(Collections.singleton(sensorType));

		MethodIdent methodIdent = mock(MethodIdent.class);
		when(methodIdent.getId()).thenReturn(methodSensorId);
		when(platformIdent.getMethodIdents()).thenReturn(Collections.singleton(methodIdent));

		when(globalDataAccessService.getAgentsOverview()).thenReturn(Collections.<PlatformIdent, AgentStatusData> singletonMap(platformIdent, null));
		when(globalDataAccessService.getCompleteAgent(platformId)).thenReturn(platformIdent);

		assertThat(cachedDataService.getPlatformIdentForId(platformId), is(equalTo(platformIdent)));
		assertThat(cachedDataService.getSensorTypeIdentForId(sensorId), is(equalTo(sensorType)));
		assertThat(cachedDataService.getMethodIdentForId(methodSensorId), is(equalTo(methodIdent)));

		verify(globalDataAccessService, times(1)).getAgentsOverview();
		verify(globalDataAccessService, times(1)).getCompleteAgent(platformId);
		verifyNoMoreInteractions(globalDataAccessService);

		assertThat(cachedDataService.getPlatformIdentForId(100L), is(nullValue()));
		assertThat(cachedDataService.getSensorTypeIdentForId(100L), is(nullValue()));
		assertThat(cachedDataService.getMethodIdentForId(100L), is(nullValue()));

		verify(globalDataAccessService, times(4)).getAgentsOverview();
		verify(globalDataAccessService, times(4)).getCompleteAgent(platformId);
		verifyNoMoreInteractions(globalDataAccessService);

	}
}
