package rocks.inspectit.shared.cs.ci.profile.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ChartingMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.TimerMethodSensorAssignment;

/**
 * Profile data for the sensor assignments.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sensor-assignment-profile-data")
public class SensorAssignmentProfileData extends AbstractProfileData<List<? extends AbstractClassSensorAssignment<?>>> {

	/**
	 * Name.
	 */
	private static final String NAME = "Sensor Assignment";

	/**
	 * {@link MethodSensorAssignment}s.
	 */
	@XmlElementRefs({ @XmlElementRef(type = MethodSensorAssignment.class), @XmlElementRef(type = TimerMethodSensorAssignment.class), @XmlElementRef(type = ChartingMethodSensorAssignment.class) })
	private List<MethodSensorAssignment> methodSensorAssignments;

	/**
	 * {@link ExceptionSensorAssignment}s.
	 */
	@XmlElementRefs({ @XmlElementRef(type = ExceptionSensorAssignment.class) })
	private List<ExceptionSensorAssignment> exceptionSensorAssignments;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<? extends AbstractClassSensorAssignment<?>> getData() {
		List<AbstractClassSensorAssignment<?>> results = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(methodSensorAssignments)) {
			results.addAll(methodSensorAssignments);
		}
		if (CollectionUtils.isNotEmpty(exceptionSensorAssignments)) {
			results.addAll(exceptionSensorAssignments);
		}
		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Gets {@link #methodSensorAssignments}.
	 *
	 * @return {@link #methodSensorAssignments}
	 */
	public List<MethodSensorAssignment> getMethodSensorAssignments() {
		return methodSensorAssignments;
	}

	/**
	 * Sets {@link #methodSensorAssignments}.
	 *
	 * @param methodSensorAssignments
	 *            New value for {@link #methodSensorAssignments}
	 */
	public void setMethodSensorAssignments(List<MethodSensorAssignment> methodSensorAssignments) {
		this.methodSensorAssignments = methodSensorAssignments;
	}

	/**
	 * Gets {@link #exceptionSensorAssignments}.
	 *
	 * @return {@link #exceptionSensorAssignments}
	 */
	public List<ExceptionSensorAssignment> getExceptionSensorAssignments() {
		return exceptionSensorAssignments;
	}

	/**
	 * Sets {@link #exceptionSensorAssignments}.
	 *
	 * @param exceptionSensorAssignments
	 *            New value for {@link #exceptionSensorAssignments}
	 */
	public void setExceptionSensorAssignments(List<ExceptionSensorAssignment> exceptionSensorAssignments) {
		this.exceptionSensorAssignments = exceptionSensorAssignments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((exceptionSensorAssignments == null) ? 0 : exceptionSensorAssignments.hashCode());
		result = (prime * result) + ((methodSensorAssignments == null) ? 0 : methodSensorAssignments.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SensorAssignmentProfileData other = (SensorAssignmentProfileData) obj;
		if (exceptionSensorAssignments == null) {
			if (other.exceptionSensorAssignments != null) {
				return false;
			}
		} else if (!exceptionSensorAssignments.equals(other.exceptionSensorAssignments)) {
			return false;
		}
		if (methodSensorAssignments == null) {
			if (other.methodSensorAssignments != null) {
				return false;
			}
		} else if (!methodSensorAssignments.equals(other.methodSensorAssignments)) {
			return false;
		}
		return true;
	}

}
