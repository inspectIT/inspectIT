package info.novatec.inspectit.cmr.spring.aop;

import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.RemoteException;
import info.novatec.inspectit.spring.logger.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.stereotype.Component;

/**
 * Aspect that defines the around advice that is bounded to all methods of all classes in the
 * info.novatec.inspectit.cmr.service package. The advice prints the exception if one is raised and
 * provides additional information like what method was executed, what parameters where passed, etc.
 * In addition all exceptions except our BusinessException are transformed to a
 * {@link RemoteException}.
 * 
 * @author Ivan Senic
 * 
 */
@Aspect
@Component
public class ExceptionInterceptor {

	/**
	 * Logger for logging purposes.
	 */
	@Log
	Logger log;

	/**
	 * Field that identifies the cause field in a {@link Throwable} used for modifying it via
	 * reflection.
	 */
	private static Field throwableCauseField;

	// get the #throwableCauseField via reflection
	static {
		try {
			throwableCauseField = Throwable.class.getDeclaredField("cause");
			throwableCauseField.setAccessible(true);
		} catch (Exception e) {
			throw new BeanInitializationException("Count not initialize " + ExceptionInterceptor.class.getName() + ". Check nested Exception.", e);
		}
	}

	/**
	 * The advice.
	 * 
	 * @param jp
	 *            {@link ProceedingJoinPoint} holding all information about pointcut.
	 * @return Result of method invocation.
	 * @throws Exception
	 *             If exception occurs we re-throw the {@link Exception} of some kind
	 */
	@Around("execution(* info.novatec.inspectit.cmr.service.*.*(..))")
	public Object logServiceException(ProceedingJoinPoint jp) throws Exception {
		try {
			return jp.proceed();
		} catch (BusinessException e) {
			if (log.isDebugEnabled()) {
				log.debug("BusinessException thrown in the service method " + jp.getSignature() + " executed with following parameters: " + Arrays.toString(jp.getArgs()) + ".", e);
			}
			// if it's our BusinessException set service signature and just re-throw it
			e.setServiceMethodSignature(jp.getSignature().toString());

			throw e;
		} catch (RemoteException e) {
			log.warn("Exception thrown in the service method " + jp.getSignature() + " executed with following parameters: " + Arrays.toString(jp.getArgs()) + ". Original exception class is: "
					+ e.getOriginalExceptionClass(), e);

			// if we already have remote one service signature and just re-throw it
			e.setServiceMethodSignature(jp.getSignature().toString());

			throw e;
		} catch (Throwable t) { // NOPMD
			log.warn("Exception thrown in the service method " + jp.getSignature() + " executed with following parameters: " + Arrays.toString(jp.getArgs()) + ".", t);

			RemoteException transformException = transformException(t, new ArrayList<Throwable>());
			transformException.setServiceMethodSignature(jp.getSignature().toString());

			throw transformException;
		}
	}

	/**
	 * Transforms the given throwable to the {@link RemoteException}.
	 * 
	 * @param throwable
	 *            {@link Throwable} to transform.
	 * @return {@link RemoteException}
	 */
	public static RemoteException transformException(Throwable throwable) {
		try {
			return transformException(throwable, new ArrayList<Throwable>());
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to transform exception to the RemoteException. Check nested Exception.", e);
		}
	}

	/**
	 * Transforms the given throwable to the {@link RemoteException}.
	 * 
	 * @param throwable
	 *            {@link Throwable} to transform.
	 * @param visitedException
	 *            List of already processed exceptions.
	 * @return {@link RemoteException}
	 * @throws IllegalAccessException
	 *             If set can not be executed on the
	 *             {@link ExceptionInterceptor#throwableCauseField}.
	 */
	private static RemoteException transformException(Throwable throwable, List<Throwable> visitedException) throws IllegalAccessException {
		if (throwable == null || !visitedException.add(throwable) || throwable instanceof RemoteException) {
			return null;
		}

		RemoteException transformedException = new RemoteException(throwable);
		throwableCauseField.set(transformedException, transformException(throwable.getCause(), visitedException));
		return transformedException;
	}
}
