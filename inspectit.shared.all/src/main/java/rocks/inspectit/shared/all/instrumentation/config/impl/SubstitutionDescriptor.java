package rocks.inspectit.shared.all.instrumentation.config.impl;

/**
 * Descriptor that is used in special sensors to denote if the return value and/or argument value
 * should be replaced if the special sensor returns result.
 *
 * @author Ivan Senic
 *
 */
public class SubstitutionDescriptor {

	/**
	 * If return value substitution should be performed.
	 */
	private final boolean returnValueSubstitution;

	/**
	 * If return value substitution should be performed.
	 */
	private final boolean parameterValueSubstitution;

	/**
	 * No-arg constructor for serialization.
	 */
	public SubstitutionDescriptor() {
		this(false, false);
	}

	/**
	 * Default constructor.
	 *
	 * @param returnValueSubstitution
	 *            If return value substitution should be performed.
	 * @param parameterValueSubstitution
	 *            If parameter value substitution should be performed.
	 */
	public SubstitutionDescriptor(boolean returnValueSubstitution, boolean parameterValueSubstitution) {
		this.returnValueSubstitution = returnValueSubstitution;
		this.parameterValueSubstitution = parameterValueSubstitution;
	}

	/**
	 * Gets {@link #returnValueSubstitution}.
	 *
	 * @return {@link #returnValueSubstitution}
	 */
	public boolean isReturnValueSubstitution() {
		return this.returnValueSubstitution;
	}

	/**
	 * Gets {@link #parameterValueSubstitution}.
	 *
	 * @return {@link #parameterValueSubstitution}
	 */
	public boolean isParameterValueSubstitution() {
		return this.parameterValueSubstitution;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (this.parameterValueSubstitution ? 1231 : 1237);
		result = (prime * result) + (this.returnValueSubstitution ? 1231 : 1237);
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
		SubstitutionDescriptor other = (SubstitutionDescriptor) obj;
		if (this.parameterValueSubstitution != other.parameterValueSubstitution) {
			return false;
		}
		if (this.returnValueSubstitution != other.returnValueSubstitution) {
			return false;
		}
		return true;
	}

}
