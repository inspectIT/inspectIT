package rocks.inspectit.agent.java.sensor.method.remote.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.sensor.method.remote.extractor.http.RemoteHttpParameterExtractor;
import rocks.inspectit.shared.all.communication.data.RemoteCallData;

public class RemoteHttpParameterExtractorTest extends AbstractLogSupport {

	@InjectMocks
	RemoteHttpParameterExtractor extractor;

	@Mock
	HttpServletRequest httpServletRequest;

	@Mock
	Logger log;

	public static class GetRemoteCallData extends RemoteHttpParameterExtractorTest {

		@Test
		public void readInspectITHeader() {
			long platformID = 1;
			long identification = 123456789;
			final String param1V = platformID + ";" + identification;

			when(httpServletRequest.getHeader("inspectITHeader")).thenReturn(param1V);

			RemoteCallData result = extractor.getRemoteCallData(httpServletRequest);

			assertThat(result.getRemotePlatformIdent(), is(platformID));
			assertThat(result.getIdentification(), is(identification));
			assertThat(result.isCalling(), is(false));
		}

		@Test
		public void readInspectITHeaderNull() {

			RemoteCallData result = extractor.getRemoteCallData(httpServletRequest);

			assertThat(result, is(nullValue()));
		}

	}

}
