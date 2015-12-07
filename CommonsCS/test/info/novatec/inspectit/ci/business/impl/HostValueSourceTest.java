/**
 *
 */
package info.novatec.inspectit.ci.business.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.testbase.TestBase;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class HostValueSourceTest extends TestBase {
	@InjectMocks
	HostValueSource valueSource;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	InvocationSequenceData invocationSeuence;

	@Mock
	PlatformIdent platformIdent;

	/**
	 * Test
	 * {@link HostValueSource#getStringValues(InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringVlaues extends HostValueSourceTest {
		private static final Long PLATFORM_IDENT_ID = 30L;
		private static final String IP_1 = "127.0.0.1";
		private static final String IP_2 = "17.23.31.11";

		@BeforeMethod
		public void init() {
			MockitoAnnotations.initMocks(this);
			List<String> ips = new ArrayList<>();
			ips.add(IP_1);
			ips.add(IP_2);

			Mockito.doReturn(PLATFORM_IDENT_ID).when(invocationSeuence).getPlatformIdent();
			Mockito.doReturn(platformIdent).when(cachedDataService).getPlatformIdentForId(PLATFORM_IDENT_ID);
			Mockito.doReturn(ips).when(platformIdent).getDefinedIPs();
		}

		@Test
		public void retrieveURI() {
			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(IP_1));
			assertThat(values, hasItemInArray(IP_2));
		}
	}
}
