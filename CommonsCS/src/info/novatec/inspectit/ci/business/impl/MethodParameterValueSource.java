/**
 *
 */
package info.novatec.inspectit.ci.business.impl;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.ParameterContentData;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "method-parameter")
public class MethodParameterValueSource extends StringValueSource {

	/**
	 * Index of the method parameter.
	 */
	@XmlAttribute(name = "parameter-index", required = true)
	private int parameterIndex;

	/**
	 * Method signature.
	 */
	@XmlAttribute(name = "methodSignature", required = true)
	private String methodSignature;

	/**
	 * Default Constructor.
	 */
	public MethodParameterValueSource() {
	}

	/**
	 * Constructor.
	 *
	 * @param parameterIndex
	 *            index of the method parameter.
	 * @param methodSignature
	 *            method signature
	 */
	public MethodParameterValueSource(int parameterIndex, String methodSignature) {
		super();
		this.parameterIndex = parameterIndex;
		this.methodSignature = methodSignature;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		MethodIdent mIdent = cachedDataService.getMethodIdentForId(invocSequence.getMethodIdent());
		if (null != mIdent && mIdent.getFullyQualifiedMethodSignature().equals(methodSignature) && null != invocSequence.getParameterContentData()) {
			for (ParameterContentData parameterContentData : invocSequence.getParameterContentData()) {
				if (parameterIndex == parameterContentData.getSignaturePosition()) {
					return new String[] { parameterContentData.getContent() };
				}
			}
		}
		return new String[0];
	}

	/**
	 * Gets {@link #parameterIndex}.
	 *
	 * @return {@link #parameterIndex}
	 */
	public int getParameterIndex() {
		return parameterIndex;
	}

	/**
	 * Sets {@link #parameterIndex}.
	 *
	 * @param parameterIndex
	 *            New value for {@link #parameterIndex}
	 */
	public void setParameterIndex(int parameterIndex) {
		this.parameterIndex = parameterIndex;
	}

	/**
	 * Gets {@link #methodSignature}.
	 *
	 * @return {@link #methodSignature}
	 */
	public String getMethodSignature() {
		return methodSignature;
	}

	/**
	 * Sets {@link #methodSignature}.
	 *
	 * @param methodSignature
	 *            New value for {@link #methodSignature}
	 */
	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

}
