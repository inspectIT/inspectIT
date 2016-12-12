package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.shared.cs.communication.data.InvocationSequenceDataHelper;

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
		this.parameterIndex = parameterIndex;
		this.methodSignature = methodSignature;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		MethodIdent mIdent = cachedDataService.getMethodIdentForId(invocSequence.getMethodIdent());
		if ((null != mIdent) && Objects.equals(methodSignature, mIdent.getFullyQualifiedMethodSignature()) && InvocationSequenceDataHelper.hasCapturedParameters(invocSequence)) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.methodSignature == null) ? 0 : this.methodSignature.hashCode());
		result = (prime * result) + this.parameterIndex;
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
		MethodParameterValueSource other = (MethodParameterValueSource) obj;
		if (this.methodSignature == null) {
			if (other.methodSignature != null) {
				return false;
			}
		} else if (!this.methodSignature.equals(other.methodSignature)) {
			return false;
		}
		if (this.parameterIndex != other.parameterIndex) {
			return false;
		}
		return true;
	}
}
