package info.novatec.inspectit.rcp.handlers;

import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.RemoteCallData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.rcp.util.OccurrenceFinderFactory;

import org.eclipse.core.commands.AbstractHandler;

/**
 * Handler that know how the template objects are created. All handler that need to create template
 * objects should extend this handler.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractTemplateHandler extends AbstractHandler {

	/**
	 * Returns template for {@link SqlStatementData}.
	 * 
	 * @param sqlStatementData
	 *            Source object.
	 * @param id
	 *            Should id be inserted into template.
	 * @param sql
	 *            Should SQL query be inserted into template.
	 * @param parameters
	 *            Should parameters be inserted into template.
	 * @return Template object.
	 */
	protected SqlStatementData getTemplate(SqlStatementData sqlStatementData, boolean id, boolean sql, boolean parameters) {
		SqlStatementData template = OccurrenceFinderFactory.getEmptyTemplate(sqlStatementData);
		if (id && 0 != sqlStatementData.getId()) {
			template.setId(sqlStatementData.getId());
		}
		if (sql && null != sqlStatementData.getSql()) {
			template.setSql(sqlStatementData.getSql());
		}
		if (parameters && null != sqlStatementData.getParameterValues()) {
			template.setParameterValues(sqlStatementData.getParameterValues());
		}
		return template;
	}

	/**
	 * Returns template for {@link RemoteCallData}.
	 * 
	 * @param remoteCallData
	 *            Source object.
	 * @param id
	 *            Should id be inserted into template.
	 * @param identification
	 *            Should identification be inserted into template.
	 * @param remotePlatformIdent
	 *            Should remotePlatformIdent be inserted into template.
	 * @param remoteCall
	 *            Should remoteCall be inserted into template.
	 * @return Template object.
	 */
	protected RemoteCallData getTemplate(RemoteCallData remoteCallData, boolean id, boolean identification, boolean remotePlatformIdent, boolean remoteCall) {
		RemoteCallData template = OccurrenceFinderFactory.getEmptyTemplate(remoteCallData);
		if (id && 0 != remoteCallData.getId()) {
			template.setId(remoteCallData.getId());
		}
		if (identification && 0 != remoteCallData.getIdentification()) {
			template.setIdentification(remoteCallData.getIdentification());
		}
		if (remotePlatformIdent && 0 != remoteCallData.getRemotePlatformIdent()) {
			template.setRemotePlatformIdent(remoteCallData.getRemotePlatformIdent());
		}
		if (remoteCall) {
			template.setCalling(!remoteCallData.isCalling());
		}
		return template;
	}

	/**
	 * Returns template for {@link ExceptionSensorData}.
	 * 
	 * @param exceptionSensorData
	 *            Source object.
	 * @param id
	 *            Should id be inserted into template.
	 * @param throwableType
	 *            Should throwable type be inserted into template.
	 * @param exceptionEvent
	 *            Should exception event be inserted into template.
	 * @param errorMessage
	 *            Should error message be inserted into template.
	 * @param stackTrace
	 *            Should stack trace be inserted into template.
	 * @return Template object.
	 */
	protected ExceptionSensorData getTemplate(ExceptionSensorData exceptionSensorData, boolean id, boolean throwableType, boolean exceptionEvent, boolean errorMessage, boolean stackTrace) {
		ExceptionSensorData template = OccurrenceFinderFactory.getEmptyTemplate(exceptionSensorData);
		if (id && 0 != exceptionSensorData.getId()) {
			template.setId(exceptionSensorData.getId());
		}
		if (throwableType && null != exceptionSensorData.getThrowableType()) {
			template.setThrowableType(exceptionSensorData.getThrowableType());
		}
		if (exceptionEvent && null != exceptionSensorData.getExceptionEvent()) {
			template.setExceptionEvent(exceptionSensorData.getExceptionEvent());
		}
		if (errorMessage && null != exceptionSensorData.getErrorMessage()) {
			template.setErrorMessage(exceptionSensorData.getErrorMessage());
		}
		if (stackTrace && null != exceptionSensorData.getStackTrace()) {
			template.setStackTrace(exceptionSensorData.getStackTrace());
		}
		return template;
	}

	/**
	 * Returns template for {@link TimerData}.
	 * 
	 * @param timerData
	 *            Source object.
	 * @param id
	 *            Should id be inserted into template.
	 * @param methodIdent
	 *            Should methodIdent be inserted into template.
	 * @return Template object.
	 */
	protected TimerData getTemplate(TimerData timerData, boolean id, boolean methodIdent) {
		TimerData template = OccurrenceFinderFactory.getEmptyTemplate(timerData);
		if (id && 0 != timerData.getId()) {
			template.setId(timerData.getId());
		}
		if (methodIdent && 0 != timerData.getMethodIdent()) {
			template.setMethodIdent(timerData.getMethodIdent());
		}
		return template;
	}

	/**
	 * Returns template for {@link InvocationSequenceData}.
	 * 
	 * @param invocationSequenceData
	 *            Source object.
	 * @param id
	 *            Should id be inserted into template.
	 * @param methodIdent
	 *            Should methodIdent be inserted into template.
	 * @return Template object.
	 */
	protected InvocationSequenceData getTemplate(InvocationSequenceData invocationSequenceData, boolean id, boolean methodIdent) {
		InvocationSequenceData template = OccurrenceFinderFactory.getEmptyTemplate(invocationSequenceData);
		if (id && 0 != invocationSequenceData.getId()) {
			template.setId(invocationSequenceData.getId());
		}
		if (methodIdent && 0 != invocationSequenceData.getMethodIdent()) {
			template.setMethodIdent(invocationSequenceData.getMethodIdent());
		}
		return template;
	}
}
