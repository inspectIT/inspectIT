package rocks.inspectit.shared.all.communication.data.eum;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents the execution of a javascript function.
 *
 * @author Jonas Kunz
 *
 */
public abstract class JSFunctionExecution extends AbstractEUMTraceElement {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2033672104732276882L;

	/**
	 * The name of the function being executed, if available. If the function is anonymous, the name
	 * is "<anonymous>". If the name could not be queried by platform limitations, the value is
	 * zero.
	 */
	@JsonProperty
	private String functionName;

	/**
	 * Gets {@link #functionName}.
	 *
	 * @return {@link #functionName}
	 */
	public String getFunctionName() {
		return this.functionName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.functionName == null) ? 0 : this.functionName.hashCode());
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
		JSFunctionExecution other = (JSFunctionExecution) obj;
		if (this.functionName == null) {
			if (other.functionName != null) {
				return false;
			}
		} else if (!this.functionName.equals(other.functionName)) {
			return false;
		}
		return true;
	}

}
