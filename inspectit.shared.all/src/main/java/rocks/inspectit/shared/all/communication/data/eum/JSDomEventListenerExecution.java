package rocks.inspectit.shared.all.communication.data.eum;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * JSDomEventListenerExecution objects represent the execution of JS listeners attached to DOM
 * elements, e.g. button clicks or other input events.
 *
 * @author Jonas Kunz
 *
 */
public class JSDomEventListenerExecution extends JSEventListenerExecution {

	/**
	 *
	 */
	private static final long serialVersionUID = -3340240065540045237L;

	/**
	 * The type of the HTML eleemnt on which the event listener was executed, e.g. "DIV" or "SPAN".
	 */
	@JsonProperty
	private String elementType;

	/**
	 * The ID of the DOM element on which the event occured. If the element has no ID assigned, the
	 * value of this variable is null.
	 */
	@JsonProperty
	@JsonSerialize(include = Inclusion.NON_NULL)
	private String elementID;

	/**
	 * Gets {@link #elementType}.
	 *
	 * @return {@link #elementType}
	 */
	public String getElementType() {
		return this.elementType;
	}

	/**
	 * Gets {@link #elementID}.
	 *
	 * @return {@link #elementID}
	 */
	public String getElementID() {
		return this.elementID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((this.elementID == null) ? 0 : this.elementID.hashCode());
		result = (prime * result) + ((this.elementType == null) ? 0 : this.elementType.hashCode());
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
		JSDomEventListenerExecution other = (JSDomEventListenerExecution) obj;
		if (this.elementID == null) {
			if (other.elementID != null) {
				return false;
			}
		} else if (!this.elementID.equals(other.elementID)) {
			return false;
		}
		if (this.elementType == null) {
			if (other.elementType != null) {
				return false;
			}
		} else if (!this.elementType.equals(other.elementType)) {
			return false;
		}
		return true;
	}

}
