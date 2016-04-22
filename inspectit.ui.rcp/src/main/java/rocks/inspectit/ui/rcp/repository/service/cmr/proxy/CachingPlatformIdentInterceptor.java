package rocks.inspectit.ui.rcp.repository.service.cmr.proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * An interceptor that populates the CachedDataService cache with the loaded {@link PlatformIdent}
 * from the service.
 *
 * @author Ivan Senic
 *
 */
public class CachingPlatformIdentInterceptor implements MethodInterceptor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Object result = methodInvocation.proceed();
		if ((result instanceof PlatformIdent) && InterceptorUtils.isServiceMethod(methodInvocation)) {
			CmrRepositoryDefinition cmrRepositoryDefinition = InterceptorUtils.getRepositoryDefinition(methodInvocation);
			if (null != cmrRepositoryDefinition) {
				PlatformIdent platformIdent = (PlatformIdent) result;
				cmrRepositoryDefinition.getCachedDataService().refreshData(platformIdent);
			}
		}
		return result;
	}

}
