package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.rcp.repository.service.cmr.ICmrService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * {@link MethodInterceptor} that delegates the call to the concrete service of a
 * {@link ICmrService} class.
 * 
 * @author Ivan Senic
 * 
 */
public class ServiceInterfaceDelegateInterceptor implements MethodInterceptor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Object thisObject = methodInvocation.getThis();
		if (thisObject instanceof ICmrService) {
			ICmrService cmrService = (ICmrService) thisObject;
			if (InterceptorUtils.isServiceMethod(methodInvocation)) {
				Object concreteService = cmrService.getService();
				Object returnVal = invokeUsingReflection(concreteService, methodInvocation.getMethod(), methodInvocation.getArguments());
				return returnVal;
			} else {
				return methodInvocation.proceed();
			}
		} else {
			throw new Exception("ServiceInterfaceIntroductionInterceptor not bounded to the ICmrService class.");
		}
	}

	/**
	 * Invokes the concrete object using reflection.
	 * 
	 * @param concreteService
	 *            Service to invoke.
	 * @param method
	 *            Method to invoke.
	 * @param arguments
	 *            Arguments.
	 * @throws Throwable
	 *             If any other exception occurs.
	 * @return Return value.
	 */
	private Object invokeUsingReflection(Object concreteService, Method method, Object[] arguments) throws Throwable {
		try {
			return method.invoke(concreteService, arguments);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

}
