package rocks.inspectit.agent.java.core.impl;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Simple wrapper class for the {@link DefaultData}.
 *
 * @author Matthias Huber
 *
 */
public class DefaultDataWrapper {

	/**
	 * Reference to {@link DefaultData}.
	 */
	private DefaultData defaultData;

	/**
	 * Gets {@link #defaultData}.
	 *
	 * @return {@link #defaultData}
	 */
	public DefaultData getDefaultData() {
		return defaultData;
	}

	/**
	 * Sets {@link #defaultData}.
	 *
	 * @param defaultData
	 *            New value for {@link #defaultData}
	 */
	public void setDefaultData(DefaultData defaultData) {
		this.defaultData = defaultData;
	}

}
