package rocks.inspectit.shared.all.instrumentation.config.impl;

/**
 * Special {@link MethodSensorTypeConfig} for the exception sensor type.
 * 
 * @author Ivan Senic
 * 
 */
public class ExceptionSensorTypeConfig extends MethodSensorTypeConfig {

	/**
	 * If enhanced exception sensor is used.
	 */
	private boolean enhanced;

	/**
	 * Gets {@link #enhanced}.
	 * 
	 * @return {@link #enhanced}
	 */
	public boolean isEnhanced() {
		return enhanced;
	}

	/**
	 * Sets {@link #enhanced}.
	 * 
	 * @param enhanced
	 *            New value for {@link #enhanced}
	 */
	public void setEnhanced(boolean enhanced) {
		this.enhanced = enhanced;
	}

}
