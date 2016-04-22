package rocks.inspectit.ui.rcp.repository.service.cmr.proxy;

import java.net.ConnectException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.core.runtime.IStatus;
import org.springframework.remoting.RemoteConnectFailureException;

import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition.OnlineStatus;

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
	@Override
	public Object invoke(MethodInvocation paramMethodInvocation) throws Throwable {
		try {
			Object rval = paramMethodInvocation.proceed();
			CmrRepositoryDefinition cmrRepositoryDefinition = InterceptorUtils.getRepositoryDefinition(paramMethodInvocation);
			if ((null != cmrRepositoryDefinition) && InterceptorUtils.isServiceMethod(paramMethodInvocation)) {
				if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE) {
					InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
				}
			} else if (null == cmrRepositoryDefinition) {
				throw new RuntimeException("Service proxy not bounded to the CMR repository definition");
			}
			return rval;
		} catch (RemoteConnectFailureException | ConnectException e) {
			handleConnectionFailure(paramMethodInvocation, e);
			if (InterceptorUtils.isReturnDefaultReturnValue(paramMethodInvocation)) {
				return InterceptorUtils.getDefaultReturnValue(paramMethodInvocation);
			} else {
				throw e;
			}
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
				InspectIT.getDefault().log(IStatus.WARNING, "The server: '" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + "' is currently unavailable.");
			}
		} else {
			throw new RuntimeException("Service proxy not bounded to the CMR repository definition", t);
		}
	}

}
