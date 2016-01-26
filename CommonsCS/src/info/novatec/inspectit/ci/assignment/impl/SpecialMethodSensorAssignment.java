package info.novatec.inspectit.ci.assignment.impl;

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.cmr.service.IRegistrationService;
import info.novatec.inspectit.instrumentation.config.SpecialInstrumentationType;
import info.novatec.inspectit.instrumentation.config.applier.IInstrumentationApplier;
import info.novatec.inspectit.instrumentation.config.applier.SpecialInstrumentationApplier;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Special {@link MethodSensorAssignment} that server for defining special Instrumentation points
 * with the already existing assignment approach.
 * <p>
 * Defining the {@link #instrumentationType} will apply such instrumentation on the defined
 * method(s).
 *
 * @author Ivan Senic
 *
 */
@XmlTransient
public class SpecialMethodSensorAssignment extends MethodSensorAssignment {

	/**
	 * Instrumentation type to apply on the assignment.
	 */
	private SpecialInstrumentationType instrumentationType;

	/**
	 * No-arg constructor.
	 */
	protected SpecialMethodSensorAssignment() {
	}

	/**
	 * Default constructor.
	 *
	 * @param instrumentationType
	 *            Special instrumentation type.
	 * @throws IllegalArgumentException
	 *             If passed instrumentation type is <code>null</code> or of type
	 *             {@link SpecialInstrumentationType#HOOK}.
	 */
	public SpecialMethodSensorAssignment(SpecialInstrumentationType instrumentationType) throws IllegalArgumentException {
		if (null == instrumentationType) {
			throw new IllegalArgumentException("Functional method sensor assignment must define instrumentation type.");
		}

		this.instrumentationType = instrumentationType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IInstrumentationApplier getInstrumentationApplier(Environment environment, IRegistrationService registrationService) {
		return new SpecialInstrumentationApplier(this, environment);
	}

	/**
	 * Gets {@link #instrumentationType}.
	 *
	 * @return {@link #instrumentationType}
	 */
	public SpecialInstrumentationType getInstrumentationType() {
		return instrumentationType;
	}

	/**
	 * Sets {@link #instrumentationType}.
	 *
	 * @param instrumentationType
	 *            New value for {@link #instrumentationType}
	 */
	public void setInstrumentationType(SpecialInstrumentationType instrumentationType) {
		this.instrumentationType = instrumentationType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SpecialMethodSensorAssignment other = (SpecialMethodSensorAssignment) obj;
		if (instrumentationType != other.instrumentationType) {
			return false;
		}
		return true;
	}

}
