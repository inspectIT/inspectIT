package rocks.inspectit.shared.all.communication.data.eum;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import rocks.inspectit.shared.all.tracing.constants.ExtraTags;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * Represents the execution of a javascript function.
 *
 * @author Jonas Kunz
 *
 */
public abstract class JSFunctionExecution extends AbstractEUMSpanDetails {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 2033672104732276882L;

	/**
	 * The name of the function being executed, if available. If the function is anonymous, the name
	 * is "<anonymous>". If the name could not be queried by platform limitations, the value is "".
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
	public void collectTags(Map<String, String> tags) {
		if (!functionName.isEmpty()) {
			tags.put(ExtraTags.OPERATION_NAME, functionName);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isExternalCall() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropagationType getPropagationType() {
		return null;
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
