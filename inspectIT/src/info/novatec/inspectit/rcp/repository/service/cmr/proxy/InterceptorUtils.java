package info.novatec.inspectit.rcp.repository.service.cmr.proxy;

import info.novatec.inspectit.cmr.service.ReturnDefaultValue;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.service.cmr.ICmrService;

import java.lang.reflect.Method;
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
	 * Checks if the method being executed has the {@link ReturnDefaultValue} annotation or it's
	 * declared in the type that is annotated with same annotation.
	 * 
	 * @param methodInvocation
	 *            Method invocation.
	 * @return <code>true</code> if {@link ReturnDefaultValue} is present on method or method
	 *         declaring class
	 */
	public static boolean isReturnDefaultReturnValue(MethodInvocation methodInvocation) {
		Method method = methodInvocation.getMethod();
		Class<?> declaringClass = method.getDeclaringClass();
		return declaringClass.isAnnotationPresent(ReturnDefaultValue.class) || method.isAnnotationPresent(ReturnDefaultValue.class);
	}

	/**
	 * Tries to get the {@link CmrRepositoryDefinition} from the proxied {@link ICmrService} object.
	 * 
	 * @param paramMethodInvocation
	 *            {@link MethodInvocation}.
	 * @return CMR invoked or null.
	 */
	public static CmrRepositoryDefinition getRepositoryDefinition(MethodInvocation paramMethodInvocation) {
		if (paramMethodInvocation instanceof ReflectiveMethodInvocation) {
			ReflectiveMethodInvocation reflectiveMethodInvocation = (ReflectiveMethodInvocation) paramMethodInvocation;
			Object service = reflectiveMethodInvocation.getThis();
			if (service instanceof ICmrService) {
				ICmrService cmrService = (ICmrService) service;
				CmrRepositoryDefinition cmrRepositoryDefinition = cmrService.getCmrRepositoryDefinition();
				return cmrRepositoryDefinition;
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
