package rocks.inspectit.agent.java.eum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.eum.data.IDataHandler;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentEndUserMonitoringConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class ServletInstrumenterTest extends TestBase {

	AgentEndUserMonitoringConfig eumConfig = new AgentEndUserMonitoringConfig();

	@Mock
	javax.servlet.Servlet dummyServlet;

	@Mock
	javax.servlet.http.HttpServletResponse dummyResponse;

	@Mock
	javax.servlet.http.HttpServletRequest dummyRequest;

	@Mock
	IConfigurationStorage config;

	@Mock
	IRuntimeLinker linker;

	@Mock
	IDataHandler dataHandler;

	@Mock
	Logger log;

	@InjectMocks
	ServletInstrumenter instrumenter;

	@BeforeMethod
	public void initMocks() throws IOException, StorageException {

		eumConfig.setActiveModules("a12");
		eumConfig.setEnabled(true);
		eumConfig.setScriptBaseUrl("/baseUrl/");
		when(config.getEndUserMonitoringConfig()).thenReturn(eumConfig);

		instrumenter.setConfigurationStorage(config);
		when(dummyResponse.getWriter()).thenReturn(Mockito.mock(PrintWriter.class));
		when(dummyResponse.getOutputStream()).thenReturn(Mockito.mock(ServletOutputStream.class));
	}

	public static class InterceptRequest extends ServletInstrumenterTest {

		private static final String FAKE_BEACON = "Json beacon usually goes here..";

		@Test
		public void testBeaconInterception() throws IOException {
			String beaconUrl = eumConfig.getScriptBaseUrl() + JSAgentModule.BEACON_SUB_PATH;
			when(dummyRequest.getRequestURI()).thenReturn(beaconUrl);
			when(dummyRequest.getReader()).thenReturn(new BufferedReader(new StringReader(FAKE_BEACON)));

			boolean intercepted = instrumenter.interceptRequest(dummyServlet, dummyRequest, dummyResponse);

			assertThat(intercepted, equalTo(true));
			verify(this.dataHandler).insertBeacon(FAKE_BEACON);
		}

		@Test
		public void testScriptInterception() throws IOException {

			String scriptUrl = eumConfig.getScriptBaseUrl() + JSAgentModule.JAVASCRIPT_URL_PREFIX + "32488_" + eumConfig.getActiveModules() + ".JS";
			when(dummyRequest.getRequestURI()).thenReturn(scriptUrl);

			StringWriter response = new StringWriter();
			PrintWriter pw = new PrintWriter(response);

			when(dummyResponse.getWriter()).thenReturn(pw);

			boolean intercepted = instrumenter.interceptRequest(dummyServlet, dummyRequest, dummyResponse);
			assertThat(intercepted, equalTo(true));
			assertThat(response.toString(), equalTo(JSAgentBuilder.buildJsFile(eumConfig.getActiveModules())));

		}

		@Test
		public void testOtherRequestsInterception() throws IOException {
			String requestUrl = eumConfig.getScriptBaseUrl() + "dontinterceptme";
			when(dummyRequest.getRequestURI()).thenReturn(requestUrl);

			boolean intercepted = instrumenter.interceptRequest(dummyServlet, dummyRequest, dummyResponse);

			assertThat(intercepted, equalTo(false));
			verify(dummyResponse, never()).getWriter();
			verify(dummyResponse, never()).getOutputStream();

		}
	}

	public static class InstrumentResponse extends ServletInstrumenterTest {

		private TagInjectionResponseWrapper respWrapper;

		private class FakeWrapper extends HttpServletResponseWrapper {

			public FakeWrapper(HttpServletResponse response) {
				super(response);
			}
		}

		@SuppressWarnings("unchecked")
		@BeforeMethod
		public void initLinker() {
			when(linker.isProxyInstance(any(TagInjectionResponseWrapper.class), any(Class.class))).thenAnswer(new Answer<Boolean>() {
				@Override
				public Boolean answer(InvocationOnMock invocation) throws Throwable {
					return invocation.getArguments()[0] instanceof FakeWrapper;
				}
			});
			when(linker.createProxy(any(Class.class), any(TagInjectionResponseWrapper.class), any(ClassLoader.class))).then(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					respWrapper = (TagInjectionResponseWrapper) invocation.getArguments()[1];
					return new FakeWrapper(dummyResponse);
				}
			});
		}

		@Test
		public void testInstrumentation() throws IOException {
			Object response = instrumenter.instrumentResponse(dummyServlet, dummyRequest, dummyResponse);
			assertThat(response, instanceOf(FakeWrapper.class));
		}

		@SuppressWarnings("unchecked")
		@Test
		public void testPreventDoubleInstrumentation() throws IOException {
			instrumenter.instrumentResponse(dummyServlet, dummyRequest, new FakeWrapper(dummyResponse));
			verify(linker, never()).createProxy(any(Class.class), any(IProxySubject.class), any(ClassLoader.class));
		}

		@Test
		public void testCookieGeneration() throws IOException {
			instrumenter.instrumentResponse(dummyServlet, dummyRequest, dummyResponse);
			respWrapper.getWriter();
			verify(dummyResponse, times(1)).addCookie(any(Cookie.class));
		}

		@Test
		public void testCorrectCookieOverwriting() throws IOException {
			String sessionId = "234587";
			when(dummyRequest.getCookies()).thenReturn(new Cookie[] { new Cookie(JSAgentBuilder.SESSION_ID_COOKIE_NAME, sessionId) });

			ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
			Mockito.doNothing().when(dummyResponse).addCookie(captor.capture());

			instrumenter.instrumentResponse(dummyServlet, dummyRequest, dummyResponse);
			respWrapper.getWriter();

			verify(dummyResponse, times(1)).addCookie(any(Cookie.class));
			Cookie added = captor.getValue();
			assertThat(added.getName(), is(JSAgentBuilder.SESSION_ID_COOKIE_NAME));
			assertThat(added.getValue(), is(sessionId));
		}



	}

}
