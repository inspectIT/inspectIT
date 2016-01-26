package rocks.inspectit.shared.all.instrumentation.config.impl;

import rocks.inspectit.shared.all.communication.data.ParameterContentType;

/**
 * The start definition of a property accessor.
 *
 * @author Patrice Bouillet
 *
 */
public class PropertyPathStart extends PropertyPath {

	/**
	 * Defines what type of property we are capturing.
	 */
	private ParameterContentType contentType;

	/**
	 * The position of the parameter in the signature if the <code>classOfExecutedMethod</code>
	 * value is set to <code>false</code>. Only set if <code>contentType</code> is set to
	 * <code>Parameter</code>
	 */
	private int signaturePosition = -1;

	/**
	 * Gets {@link #contentType}.
	 *
	 * @return {@link #contentType}
	 */
	public ParameterContentType getContentType() {
		return contentType;
	}

	/**
	 * Sets {@link #contentType}.
	 *
	 * @param contentType
	 *            New value for {@link #platformIdent}
	 */
	public void setContentType(ParameterContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * sets the position of the parameter in the signature to read.
	 *
	 * @param signaturePosition
	 *            the position of the parameter in the signature to read.
	 */
	public void setSignaturePosition(int signaturePosition) {
		this.signaturePosition = signaturePosition;
	}

	/**
	 * returns the position of the parameter in the signature to read.
	 *
	 * @return position of the parameter in the signature to read.
	 */
	public int getSignaturePosition() {
		return signaturePosition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (null != getPathToContinue()) {
			return "[" + getName() + "] " + getPathToContinue().toString();
		} else {
			return "[" + getName() + "]";
		}
	}

}