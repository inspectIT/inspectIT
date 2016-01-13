package info.novatec.inspectit.agent.sensor.method.remote.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.sensor.method.remote.extractor.http.RemoteHttpParameterExtractor;
import info.novatec.inspectit.communication.data.RemoteCallData;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RemoteHttpParameterExtractorTest extends AbstractLogSupport {

	@Mock
	private RemoteHttpParameterExtractor extractor;

	@Mock
	private HttpServletRequest httpServletRequest;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		extractor = new RemoteHttpParameterExtractor();
	}

	@Test
	public void readInspectITHeader() {
		long platformID = 1;
		long identification = 123456789;
		final String param1V = platformID + ";" + identification;

		when(httpServletRequest.getHeader("inspectITHeader")).thenReturn(param1V);

		RemoteCallData result = extractor.getRemoteCallData(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result.getRemotePlatformIdent(), is(platformID));
		assertThat(result.getIdentification(), is(identification));
		assertThat(result.isCalling(), is(false));
	}

	@Test
	public void readInspectITHeaderNull() {

		RemoteCallData result = extractor.getRemoteCallData(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(nullValue()));
	}
}
