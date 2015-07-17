package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.storage.serializer.SerializationException;

import java.net.ConnectException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
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
		} catch (SerializationException e) {
			CmrRepositoryDefinition cmrRepositoryDefinition = InterceptorUtils.getRepositoryDefinition(paramMethodInvocation);
			InspectIT.getDefault().createErrorDialog(
					"CMR repository version (" + cmrRepositoryDefinition.getVersion() + ") is not compatible with the version of the inspectIT UI. Communication between two is failing.", e, -1);
			return InterceptorUtils.getDefaultReturnValue(paramMethodInvocation);
		} catch (Exception e) {
			InspectIT.getDefault().createErrorDialog(e.getMessage(), e.getCause() != null ? e.getCause() : e, -1);
			return InterceptorUtils.getDefaultReturnValue(paramMethodInvocation);
		}
	}

	/**
	 * Handles the connection failure.
	 * 
	 * @param paramMethodInvocation
	 *            {@link MethodInvocation}.
	 * @param e
	 *            {@link Throwable}.
	 */
	private void handleConnectionFailure(MethodInvocation paramMethodInvocation, Throwable e) {
		CmrRepositoryDefinition cmrRepositoryDefinition = InterceptorUtils.getRepositoryDefinition(paramMethodInvocation);
		if (null != cmrRepositoryDefinition) {
			if (cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE) {
				InspectIT.getDefault().getCmrRepositoryManager().forceCmrRepositoryOnlineStatusUpdate(cmrRepositoryDefinition);
			}
			InspectIT.getDefault().createErrorDialog("The server: '" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort() + "' is currenlty unavailable.", e, -1);
		} else {
			throw new RuntimeException("Service proxy not bounded to the CMR repository definition");
		}
	}

}
