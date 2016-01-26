package info.novatec.inspectit.instrumentation.config.impl;

import info.novatec.inspectit.instrumentation.asm.ClassLoaderDelegationMethodInstrumenter;
import info.novatec.inspectit.instrumentation.config.FunctionalInstrumentationType;
import info.novatec.inspectit.instrumentation.config.IMethodInstrumentationPoint;

import org.objectweb.asm.MethodVisitor;

/**
 * Functional instrumentation point. These points are not directly defined by user, but are specific
 * points that we add based on the specific settings or our implementation.
 *
 * @author Ivan Senic
 *
 */
public class FunctionalInstrumentationPoint implements IMethodInstrumentationPoint {

	/**
	 * Type of instrumentation.
	 */
	private FunctionalInstrumentationType instrumentationType;

	/**
	 * Needed for serialization.
	 */
	protected FunctionalInstrumentationPoint() {
	}

	/**
	 * Default constructor. Defines type of functional instrumentation.
	 *
	 * @param instrumentationType
	 *            Type of instrumentation.
	 */
	public FunctionalInstrumentationPoint(FunctionalInstrumentationType instrumentationType) {
		this.instrumentationType = instrumentationType;
	}

	/**
	 * {@inheritDoc}
	 */
	public FunctionalInstrumentationType getInstrumentationType() {
		return instrumentationType;
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodVisitor getMethodVisitor(MethodVisitor superMethodVisitor, int access, String name, String desc) {
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
		FunctionalInstrumentationPoint other = (FunctionalInstrumentationPoint) obj;
		if (instrumentationType != other.instrumentationType) {
			return false;
		}
		return true;
	}

}
