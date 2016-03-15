package info.novatec.inspectit.cmr.spring.exporter;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationFactory;
import org.apache.shiro.subject.ExecutionException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.stereotype.Component;

/**
 * This class is taken and modified from <a href="https://shiro.apache.org/static/1.2.4/shiro-spring/cobertura/org.apache.shiro.spring.remoting.SecureRemoteInvocationExecutor.html" />
 * to handle the session id manually.
 * The originally class is under the Apache 2 licence.
 * 
 * The authors who modified it are
 * @author Ivan Senic
 * @author Clemens Geibel
 * 
 * An implementation of the Spring {@link org.springframework.remoting.support.RemoteInvocationExecutor}	
 * that binds a {@code sessionId} to the incoming thread to make it available to the {@code SecurityManager}
 * implementation during the thread execution.  The {@code SecurityManager} implementation can use this sessionId	
 * to reconstitute the {@code Subject} instance based on persistent state in the corresponding {@code Session}.	
 *	 	
 * @since 0.1
 *
 */
@Component
public class SessionAwareSecureRemoteInvocationExecutor extends	DefaultRemoteInvocationExecutor {
	
	/**
	 * The security manager.
	 */
	@Autowired
	private SecurityManager securityManager;

	/**
	 * Thread local session id.
	 */
	private ThreadLocal<Object> sessionIdThreadLocal = new ThreadLocal<Object>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(final RemoteInvocation invocation, final Object targetObject) 
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		try {
			Subject.Builder builder = new Subject.Builder(securityManager);
			String host = (String) invocation.getAttribute(SecureRemoteInvocationFactory.HOST_KEY);
			if (host != null) {
				builder.host(host);
			}
			Serializable sessionId = invocation.getAttribute(SecureRemoteInvocationFactory.SESSION_ID_KEY);
			if (sessionId != null) {
				builder.sessionId(sessionId);
			}

			Subject subject = builder.buildSubject();
		
			return subject.execute(new Callable<Object>() {
				public Object call() throws Exception {
					// This is the part which is significantly modified to set the session id thread local manually.
					Object result = SessionAwareSecureRemoteInvocationExecutor.super.invoke(invocation, targetObject);
				
					Object sessionId = null;
					Session session = SecurityUtils.getSubject().getSession(false);
					if (null != session) {
						sessionId = session.getId();
					}
					sessionIdThreadLocal.set(sessionId);
				
					return result;
				}
			});
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if ((cause instanceof NoSuchMethodException)) {
				throw ((NoSuchMethodException) cause);
			}
			if ((cause instanceof IllegalAccessException)) {
				throw ((IllegalAccessException) cause);
			}
			if ((cause instanceof InvocationTargetException)) {
				throw ((InvocationTargetException) cause);
			}
			throw new InvocationTargetException(cause);
		} catch (Throwable t) {
			throw new InvocationTargetException(t);
		}
	}

	public Object getSessionId() {
		return sessionIdThreadLocal.get();
	}

}
