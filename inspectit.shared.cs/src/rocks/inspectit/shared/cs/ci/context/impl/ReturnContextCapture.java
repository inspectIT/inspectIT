package info.novatec.inspectit.ci.context.impl;

import info.novatec.inspectit.ci.context.AbstractContextCapture;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;

/**
 * {@link AbstractContextCapture} for return values.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "return-capture")
public class ReturnContextCapture extends AbstractContextCapture {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAgentStringNotation() {
		StringBuffer stringBuffer = new StringBuffer("r=");
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

}
