package info.novatec.inspectit.rcp.editor.testers;

import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;

import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Tester checks if there is at least one object in the selection that could be added to the
 * steppable objects list. NOte that empty
 * {@link info.novatec.inspectit.communication.data.InvocationSequenceData} and
 * {@link HttpTimerData} can not be added.
 * 
 * @author Ivan Senic
 * 
 */
public class AddToSteppingObjectsTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean correctSelection = false;
		if (receiver instanceof StructuredSelection) {
			Iterator<Object> it = ((StructuredSelection) receiver).iterator();
			while (it.hasNext()) {
				Object nextObject = it.next();
				if (nextObject instanceof MethodSensorData) {
					if (nextObject instanceof HttpTimerData) {
						continue;
					} else {
						correctSelection = true;
						break;
					}
				}
			}
		}

		return correctSelection == ((Boolean) expectedValue).booleanValue();
	}

}
