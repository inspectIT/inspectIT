package rocks.inspectit.agent.java.instrumentation;

import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;

import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.instrumentation.asm.ConstructorInstrumenter;
import rocks.inspectit.agent.java.instrumentation.asm.MethodInstrumenter;
import rocks.inspectit.agent.java.instrumentation.asm.SpecialMethodInstrumenter;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;

/**
 * Factory that provides
 * {@link #getMethodVisitor(IMethodInstrumentationPoint, MethodVisitor, int, String, String, boolean)}
 * method to return the correct {@link MethodVisitor} based on the
 * {@link IMethodInstrumentationPoint}.
 *
 * @author Ivan Senic
 *
 */
@Component
public class InstrumenterFactory {

	/**
	 * Returns correct {@link MethodVisitor} based on the given instrumentation point.
	 *
	 * @param instrumentationPoint
	 *            {@link IMethodInstrumentationPoint}
	 * @param superMethodVisitor
	 *            the method visitor to which created adapter delegates calls
	 * @param access
	 *            the method's access flags
	 * @param name
	 *            the method's name
	 * @param desc
	 *            the method's descriptor
	 * @param enhancedExceptionSensor
	 *            If the visitor should consider enhanced exception sensor being active.
	 *
	 * @return {@link MethodVisitor} to use in the instrumenter
	 * @throws IllegalArgumentException
	 *             If passed instrumentation point is not known by this factory
	 */
	public MethodVisitor getMethodVisitor(IMethodInstrumentationPoint instrumentationPoint, MethodVisitor superMethodVisitor, int access, String name, String desc, boolean enhancedExceptionSensor)
			throws IllegalArgumentException {
		if (null == instrumentationPoint) {
			throw new IllegalArgumentException("Intrumentation point must not be null.");
		}

		if (instrumentationPoint instanceof SensorInstrumentationPoint) {
			SensorInstrumentationPoint sensorInstrumentationPoint = (SensorInstrumentationPoint) instrumentationPoint;
			if (sensorInstrumentationPoint.isConstructor()) {
				return new ConstructorInstrumenter(superMethodVisitor, access, name, desc, sensorInstrumentationPoint.getId(), enhancedExceptionSensor);
			} else {
				return new MethodInstrumenter(superMethodVisitor, access, name, desc, sensorInstrumentationPoint.getId(), enhancedExceptionSensor);
			}
		} else if (instrumentationPoint instanceof SpecialInstrumentationPoint) {
			SpecialInstrumentationPoint specialInstrumentationPoint = (SpecialInstrumentationPoint) instrumentationPoint;
			return new SpecialMethodInstrumenter(superMethodVisitor, access, name, desc, specialInstrumentationPoint.getId());
		}
		throw new IllegalArgumentException("The instrumentation point " + instrumentationPoint + " is not known to the InstrumenterFactory.");
	}

}
