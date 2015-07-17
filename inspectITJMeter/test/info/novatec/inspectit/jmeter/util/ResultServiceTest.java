package info.novatec.inspectit.jmeter.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import org.apache.jmeter.samplers.SampleResult;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

@SuppressWarnings("PMD")
// NOCHKALL
public class ResultServiceTest {

	@Mock
	XStream xStream;

	@BeforeTest
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void factory() {
		ResultService service = ResultService.newInstance();
		assertThat(service, is(not(nullValue())));
	}

	@Test
	public void normalUsage() {
		ResultService service = ResultService.newInstance();

		Object myPassedResult = new String();

		final String xStreamResult = "Res";
		when(xStream.toXML(myPassedResult)).thenReturn(xStreamResult);
		service.xStream = xStream;

		service.start();
		service.success();
		service.setResult(myPassedResult);

		SampleResult result = service.getResult();
		assertThat(result.isSuccessful(), is(true));
		assertThat(result.getResponseDataAsString(), is(equalTo(xStreamResult)));
	}

	@SuppressWarnings("serial")
	@Test
	public void signalError() {
		ResultService service = ResultService.newInstance();

		final String exceptionMessage = "Res";

		service.start();
		service.fail(new Exception() {
			public String getMessage() {
				return exceptionMessage;
			}
		});

		SampleResult result = service.getResult();
		assertThat(result.isSuccessful(), is(false));
		assertThat(result.getResponseMessage(), is(equalTo(exceptionMessage)));
	}
}
