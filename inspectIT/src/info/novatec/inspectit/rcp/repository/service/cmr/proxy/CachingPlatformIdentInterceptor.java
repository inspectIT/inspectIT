package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

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
		if (result instanceof PlatformIdent && InterceptorUtils.isServiceMethod(methodInvocation)) {
			CmrRepositoryDefinition cmrRepositoryDefinition = InterceptorUtils.getRepositoryDefinition(methodInvocation);
			if (null != cmrRepositoryDefinition) {
				PlatformIdent platformIdent = (PlatformIdent) result;
				cmrRepositoryDefinition.getCachedDataService().refreshData(platformIdent);
			}
		}
		return result;
	}

}
