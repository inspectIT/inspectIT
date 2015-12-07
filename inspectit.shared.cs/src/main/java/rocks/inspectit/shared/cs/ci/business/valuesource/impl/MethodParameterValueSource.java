/**
 *
 */
package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;

/**
 * This configuration element indicates that the method parameter value is to be used as the string
 * value for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "method-parameter")
public class MethodParameterValueSource extends StringValueSource {

	/**
	 * Index of the method parameter.
	 */
	@XmlAttribute(name = "parameter-index", required = true)
	private int parameterIndex = 0;

	/**
	 * Method signature.
	 */
	@XmlAttribute(name = "methodSignature", required = true)
	private String methodSignature = "";

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
		this.parameterIndex = parameterIndex;
		this.methodSignature = methodSignature;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		MethodIdent mIdent = cachedDataService.getMethodIdentForId(invocSequence.getMethodIdent());
		if (null != mIdent && Objects.equals(methodSignature, mIdent.getFullyQualifiedMethodSignature()) && InvocationSequenceDataHelper.hasCapturedParameters(invocSequence)) {
			for (ParameterContentData parameterContentData : InvocationSequenceDataHelper.getCapturedParameters(invocSequence, false)) {
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
