package rocks.inspectit.server.spring.aop;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.RemoteException;

/**
 * Tests for the {@link ExceptionInterceptor}.
 *
 * @author Ivan Senic
 *
 */
public class ExceptionInterceptorTest {

	private ExceptionInterceptor interceptor;

	@Mock
	private ProceedingJoinPoint jp;

	@Mock
	private Signature signature;

	@Mock
	private Logger log;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(jp.getSignature()).thenReturn(signature);

		interceptor = new ExceptionInterceptor();
		interceptor.log = log;
	}

	@Test
	public void noException() throws Throwable {
		interceptor.logServiceException(jp);

		verify(jp, times(1)).proceed();
		verifyZeroInteractions(log);
	}

	@Test(expectedExceptions = { BusinessException.class })
	public void businessException() throws Throwable {
		String methodSignature = "methodSignature";
		BusinessException businessException = mock(BusinessException.class);
		when(jp.proceed()).thenThrow(businessException);
		when(signature.toString()).thenReturn(methodSignature);

		try {
			interceptor.logServiceException(jp);
		} finally {
			verify(jp, times(1)).proceed();
			verify(businessException, times(1)).setServiceMethodSignature(methodSignature);
		}
	}

	@Test(expectedExceptions = { RemoteException.class })
	public void remoteException() throws Throwable {
		String methodSignature = "methodSignature";
		RemoteException remoteException = mock(RemoteException.class);
		when(jp.proceed()).thenThrow(remoteException);
		when(signature.toString()).thenReturn(methodSignature);

		try {
			interceptor.logServiceException(jp);
		} finally {
			verify(jp, times(1)).proceed();
			verify(remoteException, times(1)).setServiceMethodSignature(methodSignature);
			verify(log, times(1)).warn(anyString(), eq(remoteException));
		}
	}

	@Test(expectedExceptions = { RemoteException.class })
	public void unexpectedException() throws Throwable {
		String methodSignature = "methodSignature";

		Throwable cause = new Throwable("cause");
		Throwable throwable = new Throwable("throwable", cause);
		when(jp.proceed()).thenThrow(throwable);
		when(signature.toString()).thenReturn(methodSignature);

		try {
			interceptor.logServiceException(jp);
		} catch (Throwable t) {
			assertThat(t, is(instanceOf(RemoteException.class)));
			assertThat(t.getMessage(), is("throwable"));
			assertThat(t.getStackTrace(), is(equalTo(throwable.getStackTrace())));
			assertThat(((RemoteException) t).getOriginalExceptionClass(), is(throwable.getClass().getName()));
			assertThat(((RemoteException) t).getServiceMethodSignature(), is(methodSignature));

			Throwable c = t.getCause();
			assertThat(c, is(instanceOf(RemoteException.class)));
			assertThat(c.getMessage(), is("cause"));
			assertThat(c.getStackTrace(), is(equalTo(cause.getStackTrace())));
			assertThat(((RemoteException) c).getOriginalExceptionClass(), is(cause.getClass().getName()));
			assertThat(((RemoteException) c).getServiceMethodSignature(), is(methodSignature));

			throw t;
		} finally {
			verify(jp, times(1)).proceed();
			verify(log, times(1)).warn(anyString(), eq(throwable));
		}
	}
}
