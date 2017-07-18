package rocks.inspectit.shared.cs.ci.strategy.impl;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.cs.ci.strategy.IStrategyConfig;

/**
 * Configuration for the disruptor strategy.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "disruptor-strategy-config")
public class DisruptorStrategyConfig implements IStrategyConfig {

	/**
	 * Default buffer size.
	 */
	static final int DEFAULT_BUFFER_SIZE = 1024;

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "rocks.inspectit.agent.java.core.disruptor.impl.DefaultDisruptorStrategy";

	/**
	 * Size of the disruptor buffer.
	 * <p>
	 * Default size is {@value #DEFAULT_BUFFER_SIZE} .
	 */
	@XmlAttribute(name = "buffer-size", required = true)
	private int bufferSize = DEFAULT_BUFFER_SIZE;

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
		return Collections.singletonMap("bufferSize", String.valueOf(bufferSize));
	}

	/**
	 * Checks if the given number is power of two and if not returns the next power of two number
	 * that is bigger than given number.
	 *
	 * @param number
	 *            Number to check.
	 * @param nonPositiveDefault
	 *            Number to return in case given number is negative.
	 * @return Negative default if num is zero or less, num if it's power of two number, otherwise
	 *         closest higher power of two.
	 */
	private int checkPowerOfTwo(int number, int nonPositiveDefault) {
		if (number <= 0) {
			return nonPositiveDefault;
		} else if ((number & (number - 1)) == 0) {
			return number;
		} else {
			return (int) Math.pow(2, Math.ceil(Math.log(number) / Math.log(2)));
		}
	}

	/**
	 * Gets {@link #bufferSize}.
	 *
	 * @return {@link #bufferSize}
	 */
	public int getBufferSize() {
		return this.bufferSize;
	}

	/**
	 * Sets {@link #bufferSize}.
	 *
	 * @param bufferSize
	 *            New value for {@link #bufferSize}
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = checkPowerOfTwo(bufferSize, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.bufferSize;
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
		DisruptorStrategyConfig other = (DisruptorStrategyConfig) obj;
		if (this.bufferSize != other.bufferSize) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DisruptorStrategyConfig [bufferSize=" + this.bufferSize + "]";
	}

}
