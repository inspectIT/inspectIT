package info.novatec.inspectit.ci.context.impl;

import info.novatec.inspectit.ci.context.AbstractContextCapture;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;

/**
 * {@link AbstractContextCapture} for parameters. Saves parameter index to capture.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "parameter-capture")
public class ParameterContextCapture extends AbstractContextCapture {

	/**
	 * Index of the parameter to capture.
	 */
	@XmlAttribute(name = "index")
	private int index;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAgentStringNotation() {
		StringBuffer stringBuffer = new StringBuffer("p=");
		stringBuffer.append(index);
		stringBuffer.append(';');
		stringBuffer.append(getDisplayName());
		stringBuffer.append(';');
		if (CollectionUtils.isNotEmpty(getPaths())) {
			boolean first = true;
			for (String path : getPaths()) {
				if (!first) {
					stringBuffer.append('.');
				} else {
					first = false;
				}
				stringBuffer.append(path);
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * Gets {@link #index}.
	 * 
	 * @return {@link #index}
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Sets {@link #index}.
	 * 
	 * @param index
	 *            New value for {@link #index}
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
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
		ParameterContextCapture other = (ParameterContextCapture) obj;
		if (index != other.index) {
			return false;
		}
		return true;
	}

}
