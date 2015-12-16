package info.novatec.inspectit.agent.sensor.method.webrequest.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.sensor.method.webrequest.extractor.http.WebrequestHttpParameterExtractor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WebrequestHttpParameterExtractorTest extends AbstractLogSupport {

	private WebrequestHttpParameterExtractor extractor;

	@Mock
	private HttpServletRequest httpServletRequest;

	@BeforeMethod(dependsOnMethods = { "initMocks" })
	public void initTestClass() {
		extractor = new WebrequestHttpParameterExtractor();
	}

	@Test
	public void readInspectITHeader() {
		final String param1 = "InspectITHeader";
		final long param1VReal = 123456789l;
		final String param1V = "platformId;registeredSensorTypeId;registeredMethodId;123456789";
		final Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(param1, param1V);

		when(httpServletRequest.getHeader("InspectITHeader")).thenReturn(parameterMap.get("InspectITHeader"));

		long result = extractor.getIdentification(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(param1VReal));
	}

	@Test
	public void readInspectITHeaderNull() {

		long result = extractor.getIdentification(httpServletRequest.getClass(), httpServletRequest);

		assertThat(result, is(0l));
	}

}
