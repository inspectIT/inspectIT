package info.novatec.inspectit.ci.strategy.impl;

import info.novatec.inspectit.ci.strategy.IStrategyConfig;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration for the time sending strategy.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "time-sending-strategy-config")
public class TimeSendingStrategyConfig implements IStrategyConfig {
	
	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sending.impl.TimeStrategy";

	/**
	 * Sending time in milliseconds.
	 * <p>
	 * Default value is {@value #time}
	 */
	@XmlAttribute(name = "time", required = true)
	private long time = 5000;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getSettings() {
		return Collections.singletonMap("time", String.valueOf(time));
	}

	/**
	 * Gets {@link #time}.
	 * 
	 * @return {@link #time}
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets {@link #time}.
	 * 
	 * @param time
	 *            New value for {@link #time}
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (time ^ (time >>> 32));
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
		TimeSendingStrategyConfig other = (TimeSendingStrategyConfig) obj;
		if (time != other.time) {
			return false;
		}
		return true;
	}

}
