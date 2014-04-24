package info.novatec.inspectit.ci.strategy.impl;

import info.novatec.inspectit.ci.strategy.IStrategyConfig;

import java.util.Collections;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration for the list size sending strategy.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "list-sending-strategy-config")
public class ListSendingStrategyConfig implements IStrategyConfig {

	/**
	 * Implementing class name.
	 */
	private static final String CLASS_NAME = "info.novatec.inspectit.agent.sending.impl.ListSizeStrategy";

	/**
	 * Size of data in the list before send is executed.
	 * <p>
	 * Default size is {@value #listSize} .
	 */
	@XmlAttribute(name = "list-size", required = true)
	private int listSize = 10;

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
		return Collections.singletonMap("size", String.valueOf(listSize));
	}

	/**
	 * Gets {@link #listSize}.
	 * 
	 * @return {@link #listSize}
	 */
	public int getListSize() {
		return listSize;
	}

	/**
	 * Sets {@link #listSize}.
	 * 
	 * @param listSize
	 *            New value for {@link #listSize}
	 */
	public void setListSize(int listSize) {
		this.listSize = listSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + listSize;
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
		ListSendingStrategyConfig other = (ListSendingStrategyConfig) obj;
		if (listSize != other.listSize) {
			return false;
		}
		return true;
	}

}
