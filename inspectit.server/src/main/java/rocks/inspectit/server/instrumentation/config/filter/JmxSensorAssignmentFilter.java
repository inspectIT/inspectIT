package rocks.inspectit.server.instrumentation.config.filter;

import java.util.Objects;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;

/**
 * Filter that filters the {@link JmxAttributeDescriptor} types based on the given assignment and
 * vice versa.
 *
 * @see JmxSensorAssignmentFilter#matches(JmxBeanSensorAssignment, JmxAttributeDescriptor)
 * @author Ivan Senic
 *
 */
public class JmxSensorAssignmentFilter {

	/**
	 * Tests if the given {@link JmxAttributeDescriptor} matches the jmx bean sensor assignment.
	 *
	 * @param assignment
	 *            {@link JmxBeanSensorAssignment}
	 * @param attributeDescriptor
	 *            {@link JmxAttributeDescriptor} to check
	 * @return Returns <code>true</code> if the data in the descriptor fits the monitoring
	 *         definition in the assignment, <code>false</code> otherwise
	 */
	public boolean matches(JmxBeanSensorAssignment assignment, JmxAttributeDescriptor attributeDescriptor) {
		// not matching if the attribute is not readable
		if (!attributeDescriptor.ismBeanAttributeIsReadable()) {
			return false;
		}

		if (!matchesObjectName(assignment, attributeDescriptor.getmBeanObjectName())) {
			return false;
		}

		if (!matchesAttribute(assignment, attributeDescriptor.getAttributeName())) {
			return false;
		}

		return true;
	}


	/**
	 * Tests where the object name specifications in the {@link JmxBeanSensorAssignment} match the
	 * given object name string.
	 *
	 * @see ObjectName#apply(ObjectName)
	 * @param assignment
	 *            {@link JmxBeanSensorAssignment}
	 * @param objectName
	 *            Object name string
	 * @return Returns <code>true</code> if the assignment matches the given object name.
	 */
	private boolean matchesObjectName(JmxBeanSensorAssignment assignment, String objectName) {
		ObjectName assignmentObjectName = assignment.getObjectName();
		if (null == assignmentObjectName) {
			return false;
		}

		try {
			ObjectName testObjectName = new ObjectName(objectName);
			return assignmentObjectName.apply(testObjectName);
		} catch (MalformedObjectNameException e) {
			return false;
		}
	}

	/**
	 * Checks if the given {@link JmxBeanSensorAssignment} matches the given attribute name.
	 *
	 * @param assignment
	 *            Assignment
	 * @param attributeName
	 *            Attribute to check
	 * @return Returns <code>true</code> if the assignment definition macthes the attribute name.
	 */
	private boolean matchesAttribute(JmxBeanSensorAssignment assignment, String attributeName) {
		// if assignment does not define attributes, then we match all
		if (CollectionUtils.isEmpty(assignment.getAttributes())) {
			return true;
		} else {
			// otherwise we need to check if we have the correct attribute name
			for (String attribute : assignment.getAttributes()) {
				if (Objects.equals(attribute, attributeName)) {
					return true;
				}
			}
			return false;
		}
	}
}
