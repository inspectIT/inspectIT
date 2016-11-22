package rocks.inspectit.shared.all.communication.data.eum;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Base class for all EUM elements which can participate in a EUM trace.
 *
 * Traces are stored by child elements referencing their parents based on their
 * {@link EUMElementID}.
 *
 * Asynchronous (= non-blocking) calls are marked by a flag and elements
 *
 * @author Jonas Kunz
 *
 */
public abstract class AbstractEUMTraceElement extends AbstractEUMElement {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1814842777577166266L;

	/**
	 * The local ID of the parent of this element within the trace. The sessionID and the tabID are
	 * equal to the sessionID and tabID of this element, as elements in a trace are always local to
	 * a tab. If this element is a root element, emaning that is has no parent, the value of this
	 * variable is zero.
	 */
	@JsonProperty(value = "parentLocalID")
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	private long parentLocalID;

	/**
	 * This index defines the ordering of the individual elements within the trace according to
	 * their order of execution. This is necessary in addition to the begin-timestamp as the
	 * timestamps a) might not have enough precision to correctly order short-runnign operations and
	 * b) that for some types of elements detailed timestamps might not be available.
	 *
	 * Can be zero if we don't know the exact order (e.g., for resource load requests).
	 */
	@JsonProperty(value = "executionOrderIndex")
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	private long executionOrderIndex;

	/**
	 * The timestamp storing the enter time of this operation, if available. If the timestamp is not
	 * available, the value of this variable is zero.
	 */
	@JsonProperty
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	private double enterTimestamp;

	/**
	 * The timestamp storing the exit time of this operation, if available. If the timestamp is not
	 * available, the value of this variable is zero.
	 */
	@JsonProperty
	@JsonSerialize(include = Inclusion.NON_DEFAULT)
	private double exitTimestamp;

	/**
	 * Default Constructor.
	 */
	public AbstractEUMTraceElement() {
		parentLocalID = 0;
		executionOrderIndex = 0;
		enterTimestamp = 0;
		exitTimestamp = 0;
	}

	/**
	 * Gets {@link #parentLocalID}.
	 *
	 * @return {@link #parentLocalID}
	 */
	public long getParentLocalID() {
		return this.parentLocalID;
	}

	/**
	 * Return true, if this trace element was called asynchronously, meaning that it did not block
	 * the caller.
	 *
	 * @return {@link #isAsyncCall}
	 */
	public abstract boolean isAsyncCall();

	/**
	 * Gets {@link #enterTimestamp}.
	 *
	 * @return {@link #enterTimestamp}
	 */
	public double getEnterTimestamp() {
		return this.enterTimestamp;
	}

	/**
	 * Gets {@link #exitTimestamp}.
	 *
	 * @return {@link #exitTimestamp}
	 */
	public double getExitTimestamp() {
		return this.exitTimestamp;
	}

	/**
	 * Gets {@link #executionOrderIndex}.
	 *
	 * @return {@link #executionOrderIndex}
	 */
	public long getExecutionOrderIndex() {
		return this.executionOrderIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(this.enterTimestamp);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + (int) (this.executionOrderIndex ^ (this.executionOrderIndex >>> 32));
		temp = Double.doubleToLongBits(this.exitTimestamp);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + (int) (this.parentLocalID ^ (this.parentLocalID >>> 32));
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
		AbstractEUMTraceElement other = (AbstractEUMTraceElement) obj;
		if (Double.doubleToLongBits(this.enterTimestamp) != Double.doubleToLongBits(other.enterTimestamp)) {
			return false;
		}
		if (this.executionOrderIndex != other.executionOrderIndex) {
			return false;
		}
		if (Double.doubleToLongBits(this.exitTimestamp) != Double.doubleToLongBits(other.exitTimestamp)) {
			return false;
		}
		if (this.parentLocalID != other.parentLocalID) {
			return false;
		}
		return true;
	}

}
