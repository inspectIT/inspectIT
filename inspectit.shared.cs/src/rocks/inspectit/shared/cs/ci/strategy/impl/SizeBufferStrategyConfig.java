package info.novatec.inspectit.ci.strategy.impl;

import info.novatec.inspectit.ci.strategy.IStrategyConfig;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration for the size buffer strategy.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "size-buffer-strategy-config")
public class SizeBufferStrategyConfig implements IStrategyConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.buffer.impl.SizeBufferStrategy";

	/**
	 * Buffer size.
	 * <p>
	 * Default size is {@value #size}.
	 */
	@XmlAttribute(name = "size", required = true)
	private int size = 12;

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
		return Collections.singletonMap("size", String.valueOf(size));
	}

	/**
	 * Gets {@link #size}.
	 * 
	 * @return {@link #size}
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Sets {@link #size}.
	 * 
	 * @param size
	 *            New value for {@link #size}
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + size;
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
		SizeBufferStrategyConfig other = (SizeBufferStrategyConfig) obj;
		if (size != other.size) {
			return false;
		}
		return true;
	}

}
