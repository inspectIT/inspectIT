package info.novatec.inspectit.cmr.spring.aop;

import info.novatec.inspectit.spring.logger.Log;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Aspect that defines the after throwing advice that is bounded to all methods of all classes in
 * the info.novatec.inspectit.cmr.service package. The advice prints the exception if one is raised
 * and provides additional information like what method was executed, what parameters where passed,
 * etc.
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
	 * The advice.
	 * 
	 * @param jp
	 *            {@link JoinPoint} holding all information about pointcut.
	 * @param exception
	 *            Exception being thrown.
	 */
	@AfterThrowing(pointcut = "execution(* info.novatec.inspectit.cmr.service.*.*(..))", throwing = "exception")
	public void logServiceException(JoinPoint jp, Exception exception) {
		log.warn("Exception thrown in the service method " + jp.getSignature() + " executed with following parameters: " + Arrays.toString(jp.getArgs()) + ".", exception);
	}
}
