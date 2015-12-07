package info.novatec.inspectit.cmr.service.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.cmr.service.IBusinessContextManagementService;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.ApplicationData;
import info.novatec.inspectit.communication.data.cmr.BusinessTransactionData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.testbase.TestBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Testing the caching abilities of {@link CachedDataService}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class CachedDataServiceTest extends TestBase {

	/**
	 * Class under test.
	 */
	@InjectMocks
	CachedDataService cachedDataService;

	@Mock
	IGlobalDataAccessService globalDataAccessService;

	@Mock
	IBusinessContextManagementService businessContextService;

	/**
	 * Tests the Idents cache.
	 */
	public static class IdentsCacheTest extends CachedDataServiceTest {
		private static final long PLATFORM_ID = 10L;
		private PlatformIdent platformIdent;
		private static final long METHOD_SENSOR_ID = 20L;
		private MethodIdent methodIdent;
		private static final long SENSOR_ID = 20L;
		private SensorTypeIdent sensorType;

		@BeforeMethod
		public void initialize() throws BusinessException {
			platformIdent = mock(PlatformIdent.class);
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);

			when(globalDataAccessService.getAgentsOverview()).thenReturn(Collections.<PlatformIdent, AgentStatusData> singletonMap(platformIdent, null));
			when(globalDataAccessService.getCompleteAgent(PLATFORM_ID)).thenReturn(platformIdent);

			methodIdent = mock(MethodIdent.class);
			when(methodIdent.getId()).thenReturn(METHOD_SENSOR_ID);
			when(platformIdent.getMethodIdents()).thenReturn(Collections.singleton(methodIdent));

			sensorType = mock(SensorTypeIdent.class);
			when(sensorType.getId()).thenReturn(SENSOR_ID);
			when(platformIdent.getSensorTypeIdents()).thenReturn(Collections.singleton(sensorType));
		}

		@Test
		public void testIdentsCache() throws BusinessException {
			assertThat(cachedDataService.getPlatformIdentForId(PLATFORM_ID), is(equalTo(platformIdent)));
			assertThat(cachedDataService.getMethodIdentForId(METHOD_SENSOR_ID), is(equalTo(methodIdent)));
			assertThat(cachedDataService.getSensorTypeIdentForId(SENSOR_ID), is(equalTo(sensorType)));

			verify(globalDataAccessService, times(1)).getAgentsOverview();
			verify(globalDataAccessService, times(1)).getCompleteAgent(PLATFORM_ID);
			verifyNoMoreInteractions(globalDataAccessService);

			assertThat(cachedDataService.getPlatformIdentForId(100L), is(nullValue()));
			assertThat(cachedDataService.getMethodIdentForId(100L), is(nullValue()));
			assertThat(cachedDataService.getSensorTypeIdentForId(100L), is(nullValue()));

			verify(globalDataAccessService, times(4)).getAgentsOverview();
			verify(globalDataAccessService, times(4)).getCompleteAgent(PLATFORM_ID);
			verifyNoMoreInteractions(globalDataAccessService);

			verifyNoMoreInteractions(businessContextService);
		}
	}

	/**
	 * Tests the Business Context cache.
	 */
	public static class BusinessContextCacheTest extends CachedDataServiceTest {
		private final int FIRST_APPLICATION_ID = 123;
		private final int FIRST_BUSINESS_TX_ID = 456;
		private final int SECOND_BUSINESS_TX_ID = 45678;

		private final int SECOND_APPLICATION_ID = 12378;
		private final int THIRD_BUSINESS_TX_ID = 49978;

		private ApplicationData firstApplication;
		private ApplicationData secondApplication;
		private BusinessTransactionData firstBusinessTx;
		private BusinessTransactionData secondBusinessTx;
		private BusinessTransactionData thirdBusinessTx;

		@BeforeMethod
		public void initialize() throws BusinessException {
			firstApplication = new ApplicationData(FIRST_APPLICATION_ID, FIRST_APPLICATION_ID, "firstApplication");
			firstBusinessTx = new BusinessTransactionData(FIRST_BUSINESS_TX_ID, FIRST_BUSINESS_TX_ID, firstApplication, "firstBusinessTx");
			secondBusinessTx = new BusinessTransactionData(SECOND_BUSINESS_TX_ID, SECOND_BUSINESS_TX_ID, firstApplication, "secondBusinessTx");

			secondApplication = new ApplicationData(SECOND_APPLICATION_ID, SECOND_APPLICATION_ID, "secondApplication");
			thirdBusinessTx = new BusinessTransactionData(THIRD_BUSINESS_TX_ID, THIRD_BUSINESS_TX_ID, secondApplication, "thirdBusinessTx");

			List<BusinessTransactionData> businessTxs = new ArrayList<>();
			businessTxs.add(firstBusinessTx);
			businessTxs.add(secondBusinessTx);
			businessTxs.add(thirdBusinessTx);
			when(businessContextService.getBusinessTransactions()).thenReturn(businessTxs);
		}

		@Test
		public void testBusinessContextCache() throws BusinessException {
			assertThat(cachedDataService.getApplicationForId(FIRST_APPLICATION_ID), is(equalTo(firstApplication)));
			assertThat(cachedDataService.getApplicationForId(SECOND_APPLICATION_ID), is(equalTo(secondApplication)));
			assertThat(cachedDataService.getBusinessTransactionForId(FIRST_APPLICATION_ID, FIRST_BUSINESS_TX_ID), is(equalTo(firstBusinessTx)));
			assertThat(cachedDataService.getBusinessTransactionForId(FIRST_APPLICATION_ID, SECOND_BUSINESS_TX_ID), is(equalTo(secondBusinessTx)));
			assertThat(cachedDataService.getBusinessTransactionForId(SECOND_APPLICATION_ID, THIRD_BUSINESS_TX_ID), is(equalTo(thirdBusinessTx)));

			verify(businessContextService, times(1)).getBusinessTransactions();
			verifyNoMoreInteractions(businessContextService);

			assertThat(cachedDataService.getApplicationForId(1), is(nullValue()));
			assertThat(cachedDataService.getApplicationForId(1), is(nullValue()));
			assertThat(cachedDataService.getBusinessTransactionForId(SECOND_APPLICATION_ID, FIRST_BUSINESS_TX_ID), is(nullValue()));
			assertThat(cachedDataService.getBusinessTransactionForId(FIRST_APPLICATION_ID, THIRD_BUSINESS_TX_ID), is(nullValue()));
			assertThat(cachedDataService.getBusinessTransactionForId(1, THIRD_BUSINESS_TX_ID), is(nullValue()));
			assertThat(cachedDataService.getBusinessTransactionForId(1, 1), is(nullValue()));

			verify(businessContextService, times(7)).getBusinessTransactions();
			verifyNoMoreInteractions(businessContextService);
		}
	}
}
