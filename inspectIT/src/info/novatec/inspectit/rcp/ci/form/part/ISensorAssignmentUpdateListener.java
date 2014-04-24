package info.novatec.inspectit.rcp.ci.form.part;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.rcp.validation.ValidationControlDecoration;

import java.util.List;

/**
 * Listener for the sensor assignment master block.
 * 
 * @author Ivan Senic
 * 
 */
public interface ISensorAssignmentUpdateListener {

	/**
	 * Informs the listener that the sensor assignment was edited (in the details part).
	 * 
	 * @param sensorAssignment
	 *            Element being edited.
	 * @param dirty
	 *            If element has been made dirty.
	 * @param isValid
	 *            Is current data defined in the assignment valid input data.
	 * @param validationDecorations
	 *            TODO
	 */
	void sensorAssignmentUpdated(AbstractClassSensorAssignment<?> sensorAssignment, boolean dirty, boolean isValid, List<ValidationControlDecoration<?>> validationDecorations);
}
