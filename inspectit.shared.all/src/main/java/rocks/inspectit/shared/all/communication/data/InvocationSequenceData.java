package rocks.inspectit.shared.all.communication.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.MethodSensorData;

/**
 * The invocation sequence data object which is used to store the path of method invocations from
 * instrumented methods.
 *
 * Notice that the <code>InvocationSequenceDataHelper</code> class provides utility methods to query
 * <code>InvocationSequenceData</code> instances.
 *
 * @author Patrice Bouillet
 * @see InvocationSequenceDataHelper
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class InvocationSequenceData extends MethodSensorData {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1388734093735447105L;

	/**
	 * The nested invocation traces are stored in this list.
	 */
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
	private List<InvocationSequenceData> nestedSequences = new ArrayList<InvocationSequenceData>(0);

	/**
	 * The parent sequence of this sequence if there is any.
	 */
	@JsonIgnore
	private InvocationSequenceData parentSequence;

	/**
	 * The associated timer data object. Can be <code>null</code>.
	 */
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	private TimerData timerData;

	/**
	 * The associated sql statement data object. Can be <code>null</code>.
	 */
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	private SqlStatementData sqlStatementData;

	/**
	 * The associated remote call data object. Can be <code>null</code>.
	 */
	private RemoteCallData remoteCallData;

	/**
	 * The associated exception sensor data object. Can be <code>null</code>.
	 */
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
	private List<ExceptionSensorData> exceptionSensorDataObjects;

	/**
	 * The associated logging data. Can be <code>null</code>.
	 */
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	private LoggingData loggingData;

	/**
	 * The position if parent sequence is not <code>null</code>.
	 */
	@JsonIgnore
	private long position;

	/**
	 * The duration of this invocation sequence.
	 */
	private double duration;

	/**
	 * The start time of this invocation sequence.
	 */
	@JsonIgnore
	private double start;

	/**
	 * The end time of this invocation sequence.
	 */
	@JsonIgnore
	private double end;

	/**
	 * The count of the nested sequences (all levels).
	 */
	private long childCount = 0;

	/**
	 * If the {@link SqlStatementData} is available in this or one of the nested invocations.
	 */
	@JsonIgnore
	private Boolean nestedSqlStatements;

	/**
	 * If the {@link ExceptionSensorData} is available in this or one of the nested invocations.
	 */
	@JsonIgnore
	private Boolean nestedExceptions;

	/**
	 * Identifier of the application this invocation sequence belongs to.
	 */
	private int applicationId = 0;

	/**
	 * Identifier of the business transaction this invocation sequence belongs to.
	 */
	private int businessTransactionId = 0;

	/**
	 * If the {@link RemoteCallData} is available and is outgoing in this or one of the nested
	 * invocations.
	 */
	private Boolean nestedOutgoingRemoteCalls;

	/**
	 * If the {@link RemoteCallData} is available and is outgoing in this or one of the nested
	 * invocations.
	 */
	private Boolean nestedIncomingRemoteCalls;

	/**
	 * Default no-args constructor.
	 */
	public InvocationSequenceData() {
	}

	/**
	 * Creates a new instance.
	 *
	 * @param timeStamp
	 *            the timestamp.
	 * @param platformIdent
	 *            the platform identifier.
	 * @param sensorTypeIdent
	 *            the sensor type identifier.
	 * @param methodIdent
	 *            the method identifier.
	 */
	public InvocationSequenceData(Timestamp timeStamp, long platformIdent, long sensorTypeIdent, long methodIdent) {
		super(timeStamp, platformIdent, sensorTypeIdent, methodIdent);
	}

	/**
	 * Gets {@link #nestedSequences}.
	 *
	 * @return {@link #nestedSequences}
	 */
	public List<InvocationSequenceData> getNestedSequences() {
		return nestedSequences;
	}

	/**
	 * Sets {@link #nestedSequences}.
	 *
	 * @param nestedSequences
	 *            New value for {@link #nestedSequences}
	 */
	public void setNestedSequences(List<InvocationSequenceData> nestedSequences) {
		this.nestedSequences = nestedSequences;
	}

	/**
	 * Gets {@link #parentSequence}.
	 *
	 * @return {@link #parentSequence}
	 */
	public InvocationSequenceData getParentSequence() {
		return parentSequence;
	}

	/**
	 * Sets {@link #parentSequence}.
	 *
	 * @param parentSequence
	 *            New value for {@link #parentSequence}
	 */
	public void setParentSequence(InvocationSequenceData parentSequence) {
		this.parentSequence = parentSequence;
	}

	/**
	 * Gets {@link #timerData}.
	 *
	 * @return {@link #timerData}
	 */
	public TimerData getTimerData() {
		return timerData;
	}

	/**
	 * Sets {@link #timerData}.
	 *
	 * @param timerData
	 *            New value for {@link #timerData}
	 */
	public void setTimerData(TimerData timerData) {
		this.timerData = timerData;
	}

	/**
	 * Gets {@link #sqlStatementData}.
	 *
	 * @return {@link #sqlStatementData}
	 */
	public SqlStatementData getSqlStatementData() {
		return sqlStatementData;
	}

	/**
	 * Sets {@link #sqlStatementData}.
	 *
	 * @param sqlStatementData
	 *            New value for {@link #sqlStatementData}
	 */
	public void setSqlStatementData(SqlStatementData sqlStatementData) {
		this.sqlStatementData = sqlStatementData;
	}

	/**
	 * Sets {@link #remoteCallData}.
	 *
	 * @param remoteCallData
	 *            New value for {@link #remoteCallData}
	 */
	public void setRemoteCallData(RemoteCallData remoteCallData) {
		this.remoteCallData = remoteCallData;
	}

	/**
	 * Gets {@link #remoteCallData}.
	 *
	 * @return {@link #remoteCallData}
	 */
	public RemoteCallData getRemoteCallData() {
		return remoteCallData;
	}

	/**
	 * Gets {@link #loggingData}.
	 *
	 * @return {@link #loggingData}
	 */
	public LoggingData getLoggingData() {
		return loggingData;
	}

	/**
	 * Sets {@link #loggingData}.
	 *
	 * @param loggingData
	 *            New value for {@link #loggingData}
	 */
	public void setLoggingData(LoggingData loggingData) {
		this.loggingData = loggingData;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(long position) {
		this.position = position;
	}

	/**
	 * @return the position
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * @return the duration
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * @return the start time of the invocation sequence
	 */
	public double getStart() {
		return start;
	}

	/**
	 *
	 * @param start
	 *            the start time of the invocation sequence
	 */
	public void setStart(double start) {
		this.start = start;
	}

	/**
	 *
	 * @return the end time of the invocation sequence
	 */
	public double getEnd() {
		return end;
	}

	/**
	 * Gets {@link #exceptionSensorDataObjects}.
	 *
	 * @return {@link #exceptionSensorDataObjects}
	 */
	public List<ExceptionSensorData> getExceptionSensorDataObjects() {
		return exceptionSensorDataObjects;
	}

	/**
	 * Sets {@link #exceptionSensorDataObjects}.
	 *
	 * @param exceptionSensorDataObjects
	 *            New value for {@link #exceptionSensorDataObjects}
	 */
	public void setExceptionSensorDataObjects(List<ExceptionSensorData> exceptionSensorDataObjects) {
		this.exceptionSensorDataObjects = exceptionSensorDataObjects;
	}

	/**
	 * Adds the given exception data to this invocation sequence.
	 *
	 * @param data
	 *            the exception data to add.
	 */
	public void addExceptionSensorData(ExceptionSensorData data) {
		if (null == exceptionSensorDataObjects) {
			exceptionSensorDataObjects = new ArrayList<ExceptionSensorData>();
		}
		exceptionSensorDataObjects.add(data);
	}

	/**
	 *
	 * @param end
	 *            the end time of the invocation sequence
	 */
	public void setEnd(double end) {
		this.end = end;
	}

	/**
	 * @param childCount
	 *            the childCount to set
	 */
	public void setChildCount(long childCount) {
		this.childCount = childCount;
	}

	/**
	 * @return the childCount
	 */
	public long getChildCount() {
		return childCount;
	}

	/**
	 * Gets {@link #nestedSqlStatements}.
	 *
	 * @return {@link #nestedSqlStatements}
	 */
	public Boolean isNestedSqlStatements() {
		return nestedSqlStatements;
	}

	/**
	 * Sets {@link #nestedSqlStatements}.
	 *
	 * @param nestedSqlStatements
	 *            New value for {@link #nestedSqlStatements}
	 */
	public void setNestedSqlStatements(Boolean nestedSqlStatements) {
		this.nestedSqlStatements = nestedSqlStatements;
	}

	/**
	 * Gets {@link #nestedExceptions}.
	 *
	 * @return {@link #nestedExceptions}
	 */
	public Boolean isNestedExceptions() {
		return nestedExceptions;
	}

	/**
	 * Sets {@link #nestedExceptions}.
	 *
	 * @param nestedExceptions
	 *            New value for {@link #nestedExceptions}
	 */
	public void setNestedExceptions(Boolean nestedExceptions) {
		this.nestedExceptions = nestedExceptions;
	}

	/**
	 * Gets {@link #applicationId}.
	 *
	 * @return {@link #applicationId}
	 */
	public int getApplicationId() {
		return applicationId;
	}

	/**
	 * Sets {@link #applicationId}.
	 *
	 * @param applicationId
	 *            New value for {@link #applicationId}
	 */
	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	/**
	 * Gets {@link #businessTransactionId}.
	 *
	 * @return {@link #businessTransactionId}
	 */
	public int getBusinessTransactionId() {
		return businessTransactionId;
	}

	/**
	 * Sets {@link #businessTransactionId}.
	 *
	 * @param businessTransactionId
	 *            New value for {@link #businessTransactionId}
	 */
	public void setBusinessTransactionId(int businessTransactionId) {
		this.businessTransactionId = businessTransactionId;
	}
	
	/**
	 * Gets {@link #nestedOutgoingRemoteCalls}.
	 *
	 * @return {@link #nestedOutgoingRemoteCalls}
	 */
	public Boolean isNestedOutgoingRemoteCalls() {
		return nestedOutgoingRemoteCalls;
	}

	/**
	 * Sets {@link #nestedOutgoingRemoteCalls}.
	 *
	 * @param nestedOutgoingRemoteCalls
	 *            New value for {@link #nestedOutgoingRemoteCalls}
	 */
	public void setNestedOutgoingRemoteCalls(Boolean nestedOutgoingRemoteCalls) {
		this.nestedOutgoingRemoteCalls = nestedOutgoingRemoteCalls;
	}

	/**
	 * Gets {@link #nestedIncomingRemoteCalls}.
	 *
	 * @return {@link #nestedIncomingRemoteCalls}
	 */
	public Boolean isNestedIncommingRemoteCalls() {
		return nestedIncomingRemoteCalls;
	}

	/**
	 * Sets {@link #nestedIncomingRemoteCalls}.
	 *
	 * @param nestedIncommingRemoteCalls
	 *            New value for {@link #nestedIncomingRemoteCalls}
	 */
	public void setNestedIncommingRemoteCalls(Boolean nestedIncommingRemoteCalls) {
		this.nestedIncomingRemoteCalls = nestedIncommingRemoteCalls;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((exceptionSensorDataObjects == null) ? 0 : exceptionSensorDataObjects.hashCode());
		result = (prime * result) + ((sqlStatementData == null) ? 0 : sqlStatementData.hashCode());
		result = (prime * result) + ((timerData == null) ? 0 : timerData.hashCode());
		result = (prime * result) + ((loggingData == null) ? 0 : loggingData.hashCode());
		result = prime * result + ((remoteCallData == null) ? 0 : remoteCallData.hashCode());
		result = (prime * result) + applicationId;
		result = (prime * result) + businessTransactionId;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InvocationSequenceData other = (InvocationSequenceData) obj;
		if (exceptionSensorDataObjects == null) {
			if (other.exceptionSensorDataObjects != null) {
				return false;
			}
		} else if (!exceptionSensorDataObjects.equals(other.exceptionSensorDataObjects)) {
			return false;
		}
		if (sqlStatementData == null) {
			if (other.sqlStatementData != null) {
				return false;
			}
		} else if (!sqlStatementData.equals(other.sqlStatementData)) {
			return false;
		}
		if (timerData == null) {
			if (other.timerData != null) {
				return false;
			}
		} else if (!timerData.equals(other.timerData)) {
			return false;
		}
		if (loggingData == null) {
			if (other.loggingData != null) {
				return false;
			}
		} else if (!loggingData.equals(other.loggingData)) {
			return false;
		}
		if (applicationId != other.applicationId) {
			return false;
		}
		if (businessTransactionId != other.businessTransactionId) {
			return false;
		}
		if (remoteCallData == null) {
			if (other.remoteCallData != null) {
				return false;
			}
		} else if (!remoteCallData.equals(other.remoteCallData)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
		long size = super.getObjectSize(objectSizes, doAlign);
		size += objectSizes.getPrimitiveTypesSize(11, 0, 2, 0, 2, 3);
		size += objectSizes.getSizeOf(timerData);
		size += objectSizes.getSizeOf(loggingData);
		size += objectSizes.getSizeOf(sqlStatementData);
		size += objectSizes.getSizeOf(remoteCallData);
		if (nestedSequences instanceof ArrayList) {
			size += objectSizes.getSizeOf(nestedSequences, 0);
			for (InvocationSequenceData invocationSequenceData : nestedSequences) {
				size += objectSizes.getSizeOf(invocationSequenceData);
			}
		}
		if (null != exceptionSensorDataObjects) {
			size += objectSizes.getSizeOf(exceptionSensorDataObjects);
			for (ExceptionSensorData exceptionSensorData : exceptionSensorDataObjects) {
				size += objectSizes.getSizeOf(exceptionSensorData);
			}
		}
		if (null != nestedSqlStatements) {
			size += objectSizes.getSizeOfBooleanObject();
		}
		if (null != nestedExceptions) {
			size += objectSizes.getSizeOfBooleanObject();
		}
		if (null != nestedOutgoingRemoteCalls) {
			size += objectSizes.getSizeOfBooleanObject();
		}
		if (null != nestedIncomingRemoteCalls) {
			size += objectSizes.getSizeOfBooleanObject();
		}
		if (doAlign) {
			return objectSizes.alignTo8Bytes(size);
		} else {
			return size;
		}
	}

	/**
	 * Clones invocation sequence. This method returns new object exactly same as the original
	 * object, but with out nested sequences set.
	 *
	 * @return Cloned invocation sequence.
	 */
	public InvocationSequenceData getClonedInvocationSequence() {
		InvocationSequenceData clone = new InvocationSequenceData(this.getTimeStamp(), this.getPlatformIdent(), this.getSensorTypeIdent(), this.getMethodIdent());
		clone.setId(this.getId());
		clone.setChildCount(this.getChildCount());
		clone.setDuration(this.getDuration());
		clone.setEnd(this.getEnd());
		clone.setNestedSequences(Collections.<InvocationSequenceData> emptyList());
		clone.setParameterContentData(this.getParameterContentData());
		clone.setParentSequence(this.getParentSequence());
		clone.setPosition(this.getPosition());
		clone.setSqlStatementData(this.getSqlStatementData());
		clone.setTimerData(this.getTimerData());
		clone.setExceptionSensorDataObjects(this.getExceptionSensorDataObjects());
		clone.setStart(this.getStart());
		clone.setNestedSqlStatements(this.isNestedSqlStatements());
		clone.setNestedExceptions(this.isNestedExceptions());
		clone.setLoggingData(this.getLoggingData());
		clone.setRemoteCallData(this.getRemoteCallData());
		clone.setNestedIncommingRemoteCalls(this.isNestedIncommingRemoteCalls());
		clone.setApplicationId(this.getApplicationId());
		clone.setBusinessTransactionId(this.getBusinessTransactionId());
		clone.setNestedOutgoingRemoteCalls(this.isNestedOutgoingRemoteCalls());
		return clone;
	}

}
