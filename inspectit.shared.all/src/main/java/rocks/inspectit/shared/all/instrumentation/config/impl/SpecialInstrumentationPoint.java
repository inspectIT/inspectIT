package rocks.inspectit.shared.all.instrumentation.config.impl;

import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;

import rocks.inspectit.shared.all.instrumentation.asm.ClassLoaderDelegationMethodInstrumenter;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.SpecialInstrumentationType;

/**
 * Special instrumentation point. These points are not directly defined by user, but are specific
 * points that we add based on the specific settings or our implementation.
 *
 * @author Ivan Senic
 *
 */
public class SpecialInstrumentationPoint implements IMethodInstrumentationPoint {

	/**
	 * Type of instrumentation.
	 */
	private SpecialInstrumentationType instrumentationType;

	/**
	 * Needed for serialization.
	 */
	protected SpecialInstrumentationPoint() {
	}

	/**
	 * Default constructor. Defines type of special instrumentation.
	 *
	 * @param instrumentationType
	 *            Type of instrumentation.
	 */
	public SpecialInstrumentationPoint(SpecialInstrumentationType instrumentationType) {
		this.instrumentationType = instrumentationType;
	}

	/**
	 * {@inheritDoc}
	 */
	public SpecialInstrumentationType getInstrumentationType() {
		return instrumentationType;
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodVisitor getMethodVisitor(MethodVisitor superMethodVisitor, int access, String name, String desc, boolean enhancedExceptionSensor) {
		switch (instrumentationType) {
		case CLASS_LOADING_DELEGATION:
			return new ClassLoaderDelegationMethodInstrumenter(superMethodVisitor, access, name, desc);
		default:
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instrumentationType == null) ? 0 : instrumentationType.hashCode());
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
		SpecialInstrumentationPoint other = (SpecialInstrumentationPoint) obj;
		if (instrumentationType != other.instrumentationType) {
			return false;
		}
		return true;
	}

}
