package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.service.cmr.ICmrService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

import com.google.common.base.Defaults;

/**
 * Utilities that will be used in interceptors.
 * 
 * @author Ivan Senic
 * 
 */
public final class InterceptorUtils {

	/**
	 * Private constructor.
	 */
	private InterceptorUtils() {
	}

	/**
	 * Is service method.
	 * 
	 * @param methodInvocation
	 *            Method invocation.
	 * @return Return if it is service method.
	 */
	public static boolean isServiceMethod(MethodInvocation methodInvocation) {
		return !methodInvocation.getMethod().getDeclaringClass().equals(ICmrService.class);
	}

	/**
	 * Checks if the method being executed is executed on the proxy containing {@link ICmrService}
	 * and service defines the default value on error value.
	 * 
	 * @param methodInvocation
	 *            Method invocation.
	 * @return <code>true</code> if {@link ICmrService} objects defines return default on error
	 */
	public static boolean isReturnDefaultReturnValue(MethodInvocation methodInvocation) {
		ICmrService cmrService = getCmrService(methodInvocation);
		return null != cmrService && cmrService.isDefaultValueOnError();
	}

	/**
	 * Tries to get the {@link CmrRepositoryDefinition} from the proxied {@link ICmrService} object.
	 * 
	 * @param methodInvocation
	 *            {@link MethodInvocation}.
	 * @return CMR invoked or null.
	 */
	public static CmrRepositoryDefinition getRepositoryDefinition(MethodInvocation methodInvocation) {
		ICmrService cmrService = getCmrService(methodInvocation);
		if (null != cmrService) {
			CmrRepositoryDefinition cmrRepositoryDefinition = cmrService.getCmrRepositoryDefinition();
			return cmrRepositoryDefinition;
		}
		return null;
	}

	/**
	 * Returns {@link ICmrService} object if one is bounded to the proxy being invoked in the given
	 * {@link MethodInvocation} or <code>null</code> if one can not be obtained.
	 * 
	 * @param methodInvocation
	 *            {@link MethodInvocation}.
	 * @return {@link ICmrService} bounded on proxy or <code>null</code>
	 */
	private static ICmrService getCmrService(MethodInvocation methodInvocation) {
		if (methodInvocation instanceof ReflectiveMethodInvocation) {
			ReflectiveMethodInvocation reflectiveMethodInvocation = (ReflectiveMethodInvocation) methodInvocation;
			Object service = reflectiveMethodInvocation.getThis();
			if (service instanceof ICmrService) {
				return (ICmrService) service;
			}
		}
		return null;
	}

	/**
	 * Checks if the return type of the {@link java.lang.reflect.Method} invoked by
	 * {@link MethodInvocation} is one of tree major collection types (List, Map, Set) and if it is
	 * returns the empty collection of correct type. Otherwise it returns null.
	 * 
	 * @param paramMethodInvocation
	 *            {@link MethodInvocation}
	 * @return If the method invoked by {@link MethodInvocation} is one of tree major collection
	 *         types (List, Map, Set) method returns the empty collection of correct type. Otherwise
	 *         it returns null.
	 */
	public static Object getDefaultReturnValue(MethodInvocation paramMethodInvocation) {
		Class<?> returnType = paramMethodInvocation.getMethod().getReturnType();
		if (returnType.isAssignableFrom(List.class)) {
			return Collections.emptyList();
		} else if (returnType.isAssignableFrom(Map.class)) {
			return Collections.emptyMap();
		} else if (returnType.isAssignableFrom(Set.class)) {
			return Collections.emptySet();
		} else if (returnType.isPrimitive()) {
			try {
				return Defaults.defaultValue(returnType);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}

	}
}
