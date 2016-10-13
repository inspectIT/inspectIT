package rocks.inspectit.shared.cs.ci.assignment.impl;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.cs.ci.sensor.method.special.AbstractSpecialMethodSensorConfig;

/**
 * Special {@link MethodSensorAssignment} that server for defining special instrumentation points
 * with the already existing assignment approach.
 *
 * @see AbstractSpecialMethodSensorConfig
 * @author Ivan Senic
 *
 */
@XmlTransient
public class SpecialMethodSensorAssignment extends MethodSensorAssignment {

	/**
	 * {@link AbstractSpecialMethodSensorConfig}.
	 */
	private AbstractSpecialMethodSensorConfig specialMethodSensorConfig;

	/**
	 * No-arg constructor.
	 */
	protected SpecialMethodSensorAssignment() {
	}

	/**
	 * Default constructor.
	 *
	 * @param specialMethodSensorConfig
	 *            {@link AbstractSpecialMethodSensorConfig}.
	 */
	public SpecialMethodSensorAssignment(AbstractSpecialMethodSensorConfig specialMethodSensorConfig) {
		super(specialMethodSensorConfig.getClass());
		this.specialMethodSensorConfig = specialMethodSensorConfig;
	}

	/**
	 * Gets {@link #specialMethodSensorConfig}.
	 * 
	 * @return {@link #specialMethodSensorConfig}
	 */
	public AbstractSpecialMethodSensorConfig getSpecialMethodSensorConfig() {
		return this.specialMethodSensorConfig;
	}

	/**
	 * Sets {@link #specialMethodSensorConfig}.
	 * 
	 * @param specialMethodSensorConfig
	 *            New value for {@link #specialMethodSensorConfig}
	 */
	public void setSpecialMethodSensorConfig(AbstractSpecialMethodSensorConfig specialMethodSensorConfig) {
		this.specialMethodSensorConfig = specialMethodSensorConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.specialMethodSensorConfig == null) ? 0 : this.specialMethodSensorConfig.hashCode());
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
		if (this.specialMethodSensorConfig == null) {
			if (other.specialMethodSensorConfig != null) {
				return false;
			}
		} else if (!this.specialMethodSensorConfig.equals(other.specialMethodSensorConfig)) {
			return false;
		}
		return true;
	}


}
