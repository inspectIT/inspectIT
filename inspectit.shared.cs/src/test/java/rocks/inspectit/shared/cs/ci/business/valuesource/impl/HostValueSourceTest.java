package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
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
	 * {@link HostValueSource#getStringValues(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringVlaues extends HostValueSourceTest {
		private static final Long PLATFORM_IDENT_ID = 30L;
		private static final String IP_1 = "127.0.0.1";
		private static final String IP_2 = "17.23.31.11";

		@Test
		public void retrieveURI() {
			List<String> ips = new ArrayList<>();
			ips.add(IP_1);
			ips.add(IP_2);

			Mockito.doReturn(PLATFORM_IDENT_ID).when(invocationSeuence).getPlatformIdent();
			Mockito.doReturn(platformIdent).when(cachedDataService).getPlatformIdentForId(PLATFORM_IDENT_ID);
			Mockito.doReturn(ips).when(platformIdent).getDefinedIPs();

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(IP_1));
			assertThat(values, hasItemInArray(IP_2));
		}

		@Test
		public void platformIdentNull() {
			Mockito.doReturn(null).when(cachedDataService).getPlatformIdentForId(PLATFORM_IDENT_ID);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}
	}
}
