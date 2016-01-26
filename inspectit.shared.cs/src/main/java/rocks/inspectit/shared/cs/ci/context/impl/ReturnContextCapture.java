package rocks.inspectit.shared.cs.ci.context.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.communication.data.ParameterContentType;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;
import rocks.inspectit.shared.cs.ci.context.AbstractContextCapture;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyPathStart getPropertyPathStart() {
		PropertyPathStart propertyPathStart = new PropertyPathStart();
		propertyPathStart.setName(getDisplayName());
		propertyPathStart.setContentType(ParameterContentType.RETURN);
		addPaths(propertyPathStart);
		return propertyPathStart;
	}

}
