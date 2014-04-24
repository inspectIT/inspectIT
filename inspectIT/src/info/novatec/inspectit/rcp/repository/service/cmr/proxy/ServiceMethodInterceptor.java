package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

import java.net.ConnectException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.core.runtime.IStatus;
import org.springframework.remoting.RemoteConnectFailureException;

/**
 * Our service method interceptor that will catch {@link InspectITCommunicationException} and if the
 * problem was {@link RemoteConnectFailureException}, it will update the online status of the CMR.
 * This interceptor will also show a error message.
 * 
 * @author Ivan Senic
 * 
 */
public class ServiceMethodInterceptor implements MethodInterceptor {

	/**
	 * {@inheritDoc}
	 */
	public Object invoke(MethodInvocation paramMethodInvocation) throws Throwable {
		try {
			Object rval = paramMethodInvocation.proceed();
			CmrRepositoryDefinition cmrRepositoryDefinition = InterceptorUtils.getRepositoryDefinition(paramMethodInvocation);
			if (null != cmrRepositoryDefinition && InterceptorUtils.isServiceMethod(paramMethodInvocation)) {
				if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
					InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
				}
			} else if (null == cmrRepositoryDefinition) {
				throw new RuntimeException("Service proxy not bounded to the CMR repository definition");
			}
			return rval;
		} catch (RemoteConnectFailureException e) {
			handleConnectionFailure(paramMethodInvocation, e);
			return InterceptorUtils.getDefaultReturnValue(paramMethodInvocation);
		} catch (ConnectException e) {
			handleConnectionFailure(paramMethodInvocation, e);
			return InterceptorUtils.getDefaultReturnValue(paramMethodInvocation);
		} catch (BusinessException e) { // NOPMD
			// if it's business exception we must throw it to correctly have it in the service calls
			throw e;
		} catch (Exception e) {
			// TODO possibly remove this one completely and let it be caught in the UI execution
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e.getCause() != null ? e.getCause() : e, -1);
			return InterceptorUtils.getDefaultReturnValue(paramMethodInvocation);
		}
	}

	/**
	 * Handles the connection failure.
	 * 
	 * @param paramMethodInvocation
	 *            {@link MethodInvocation}.
	 * @param t
	 *            {@link Throwable}.
	 */
	private void handleConnectionFailure(MethodInvocation paramMethodInvocation, Throwable t) {
		CmrRepositoryDefinition cmrRepositoryDefinition = InterceptorUtils.getRepositoryDefinition(paramMethodInvocation);
		if (null != cmrRepositoryDefinition) {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
			}
			InspectIT.getDefault().log(IStatus.WARNING, "The server: '" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + "' is currenlty unavailable.");
		} else {
			throw new RuntimeException("Service proxy not bounded to the CMR repository definition", t);
		}
	}

}
