package rocks.inspectit.agent.java.sensor.method.special;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
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

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.config.IConfigurationStorage;
import rocks.inspectit.agent.java.config.StorageException;
import rocks.inspectit.agent.java.config.impl.SpecialSensorConfig;
import rocks.inspectit.agent.java.eum.data.IDataHandler;
import rocks.inspectit.agent.java.eum.instrumentation.JSAgentBuilder;
import rocks.inspectit.agent.java.eum.instrumentation.TagInjectionResponseWrapper;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentEndUserMonitoringConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class EUMInstrumentationHookTest extends TestBase {

	AgentEndUserMonitoringConfig eumConfig = new AgentEndUserMonitoringConfig();

	static final long METHOD_ID = 11L;

	@Mock
	SpecialSensorConfig ssc;

	@Mock
	javax.servlet.Filter dummyFilter;

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

	EUMInstrumentationHook hook;

	@BeforeMethod
	public void initMocks() throws IOException, StorageException {
		eumConfig.setActiveModules("a12");
		eumConfig.setEnabled(true);
		eumConfig.setScriptBaseUrl("/baseUrl/");
		when(dummyRequest.getRequestURI()).thenReturn("/mycool/url/here");
		when(config.getEndUserMonitoringConfig()).thenReturn(eumConfig);
		when(dummyResponse.getWriter()).thenReturn(Mockito.mock(PrintWriter.class));
		when(dummyResponse.getOutputStream()).thenReturn(Mockito.mock(ServletOutputStream.class));

	}

	TagInjectionResponseWrapper respWrapper;

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

	public static class BeforeBody extends EUMInstrumentationHookTest {

		private static final String FAKE_BEACON = "Json beacon usually goes here..";

		@Test
		public void testBeaconInterception() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			String beaconUrl = eumConfig.getScriptBaseUrl() + JSAgentModule.BEACON_SUB_PATH;
			when(dummyRequest.getRequestURI()).thenReturn(beaconUrl);
			when(dummyRequest.getReader()).thenReturn(new BufferedReader(new StringReader(FAKE_BEACON)));

			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, new Object[] { dummyRequest, dummyResponse }, ssc);

			assertThat(intercepted, equalTo(true));
			verify(this.dataHandler).insertBeacon(FAKE_BEACON);
		}

		@Test
		public void testScriptInterception() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			String scriptUrl = eumConfig.getScriptBaseUrl() + JSAgentModule.JAVASCRIPT_URL_PREFIX + "32488_" + eumConfig.getActiveModules() + ".JS";
			when(dummyRequest.getRequestURI()).thenReturn(scriptUrl);

			StringWriter response = new StringWriter();
			PrintWriter pw = new PrintWriter(response);

			when(dummyResponse.getWriter()).thenReturn(pw);

			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, new Object[] { dummyRequest, dummyResponse }, ssc);
			assertThat(intercepted, equalTo(true));
			assertThat(response.toString(), equalTo(JSAgentBuilder.buildJsFile(eumConfig.getActiveModules())));

		}

		@Test
		public void testOtherRequestsInterception() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			String requestUrl = eumConfig.getScriptBaseUrl() + "dontinterceptme";
			when(dummyRequest.getRequestURI()).thenReturn(requestUrl);

			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, new Object[] { dummyRequest, dummyResponse }, ssc);

			assertThat(intercepted, equalTo(false));
			verify(dummyResponse, never()).getWriter();
			verify(dummyResponse, never()).getOutputStream();

		}

		@Test
		public void testFilterChainHandling() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			String requestUrl = eumConfig.getScriptBaseUrl() + "dontinterceptme";
			when(dummyRequest.getRequestURI()).thenReturn(requestUrl);

			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyFilter, new Object[] { dummyRequest, dummyResponse }, ssc);
			assertThat(intercepted, equalTo(false));

			intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, new Object[] { dummyRequest, dummyResponse }, ssc);
			assertThat(intercepted, equalTo(false));

			verify(dummyResponse, never()).getWriter();
			verify(dummyResponse, never()).getOutputStream();

		}

		@SuppressWarnings("unchecked")
		@Test
		public void testInvalidConfigInterception() throws IOException, StorageException {
			when(config.getEndUserMonitoringConfig()).thenThrow(StorageException.class);
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			Object[] params = new Object[] { dummyRequest, dummyResponse };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			Object response = params[1];
			assertThat(intercepted, equalTo(false));
			assertThat(response, equalTo((Object) dummyResponse));

		}

		@SuppressWarnings("unchecked")
		@Test
		public void testInvalidConfigHandling() throws IOException, StorageException {
			when(config.getEndUserMonitoringConfig()).thenThrow(StorageException.class);
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			Object[] params = new Object[] { dummyRequest, dummyResponse };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			Object response = params[1];
			assertThat(intercepted, equalTo(false));
			assertThat(response, equalTo((Object) dummyResponse));

		}

		@Test
		public void testInvalidParamTypesHandling() throws IOException, StorageException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			Object invalidResponseObject = new Object();
			Object[] params = new Object[] { invalidResponseObject, invalidResponseObject };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			Object response = params[1];
			assertThat(intercepted, equalTo(false));
			assertThat(response, equalTo(invalidResponseObject));

		}

		@Test
		public void testInstrumentation() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			Object[] params = new Object[] { dummyRequest, dummyResponse };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			Object response = params[1];
			assertThat(intercepted, equalTo(false));
			assertThat(response, instanceOf(FakeWrapper.class));
		}

		@SuppressWarnings("unchecked")
		@Test
		public void testPreventDoubleInstrumentation() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			Object[] params = new Object[] { dummyRequest, new FakeWrapper(dummyResponse) };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			assertThat(intercepted, equalTo(false));
			verify(linker, never()).createProxy(any(Class.class), any(IProxySubject.class), any(ClassLoader.class));
		}

		@Test
		public void testCookieGeneration() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			Object[] params = new Object[] { dummyRequest, dummyResponse };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			assertThat(intercepted, equalTo(false));
			respWrapper.getWriter();
			verify(dummyResponse, times(1)).addCookie(any(Cookie.class));
		}

		@Test
		public void testPreventCookieOverwriting() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			String sessionId = "234587";
			when(dummyRequest.getCookies()).thenReturn(new Cookie[] { new Cookie(JSAgentBuilder.SESSION_ID_COOKIE_NAME, sessionId) });

			Object[] params = new Object[] { dummyRequest, dummyResponse };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			assertThat(intercepted, equalTo(false));
			respWrapper.getWriter();

			verify(dummyResponse, never()).addCookie(any(Cookie.class));
		}

		@SuppressWarnings("unchecked")
		@Test
		public void testLinkerErrorHandling() throws IOException {
			when(linker.createProxy(any(Class.class), any(TagInjectionResponseWrapper.class), any(ClassLoader.class))).then(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					return null;
				}
			});
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			Object[] params = new Object[] { dummyRequest, dummyResponse };
			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, params, ssc);
			Object response = params[1];

			assertThat(intercepted, equalTo(false));
			verify(linker, times(1)).createProxy(any(Class.class), any(IProxySubject.class), any(ClassLoader.class));
			assertThat(response, equalTo((Object) dummyResponse));
			assertThat(intercepted, equalTo(false));

		}

	}

	public static class AfterBody extends EUMInstrumentationHookTest {

		@Test
		public void testNoOperationInAfterBody() throws IOException {
			hook = new EUMInstrumentationHook(linker, dataHandler, config);

			String requestUrl = eumConfig.getScriptBaseUrl() + "dontinterceptme";
			when(dummyRequest.getRequestURI()).thenReturn(requestUrl);

			boolean intercepted = null != hook.beforeBody(METHOD_ID, dummyFilter, new Object[] { dummyRequest, dummyResponse }, ssc);
			assertThat(intercepted, equalTo(false));
			intercepted = null != hook.beforeBody(METHOD_ID, dummyServlet, new Object[] { dummyRequest, dummyResponse }, ssc);
			assertThat(intercepted, equalTo(false));

			hook.afterBody(METHOD_ID, dummyServlet, new Object[] { dummyRequest, dummyResponse }, null, ssc);
			hook.afterBody(METHOD_ID, dummyFilter, new Object[] { dummyRequest, dummyResponse }, null, ssc);

			verify(dummyResponse, never()).getWriter();
			verify(dummyResponse, never()).getOutputStream();

		}
	}

	private static class FakeWrapper extends HttpServletResponseWrapper {

		public FakeWrapper(HttpServletResponse response) {
			super(response);
		}
	}
}
