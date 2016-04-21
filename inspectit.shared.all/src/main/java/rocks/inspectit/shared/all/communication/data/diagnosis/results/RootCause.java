/**
 *
 */
package rocks.inspectit.shared.all.communication.data.diagnosis.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.AggregatedInvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * This class represents a rootCause of a problem. The rootCause consists of a methodName, the timer
 * of the methodName and the underlying MethodCalls and TimerData.
 *
 * @author Alexander Wert, Christian Voegele
 *
 */
public class RootCause implements Serializable {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 5657239884020810922L;

	/**
	 * The ident of the root cause method.
	 */
	@JsonProperty(value = "methodIdent")
	private long methodIdent;

	/**
	 * Relevant timerData of RootCause.
	 */
	@JsonProperty(value = "timerDataProblemOccurence")
	private TimerDataProblemOccurence timerDataProblemOccurence;

	/**
	 * Map of underlying methodsNames mapped to the {@link #timerData} of the rootCause.
	 */
	@JsonProperty(value = "TimerDataPerMethod")
	private Map<Long, ArrayList<TimerDataProblemOccurence>> timerDataPerMethod = new HashMap<Long, ArrayList<TimerDataProblemOccurence>>();

	/**
	 * This constructor creates new RootCause.
	 *
	 * @param rootCauseInvocations
	 *            sequence of InvocationSequenceData that represents the root cause
	 */
	public RootCause(AggregatedInvocationSequenceData rootCauseInvocations) {
		this.methodIdent = rootCauseInvocations.getMethodIdent();
		this.timerDataProblemOccurence = new TimerDataProblemOccurence(rootCauseInvocations);

		ArrayList<TimerDataProblemOccurence> timerList;
		for (InvocationSequenceData invocation : rootCauseInvocations.getRawInvocationsSequenceElements()) {
			long methodIdent = invocation.getMethodIdent();
			if (this.timerDataPerMethod.containsKey(methodIdent)) {
				timerList = this.timerDataPerMethod.get(methodIdent);
				timerList.add(new TimerDataProblemOccurence(invocation));
			} else {
				timerList = new ArrayList<TimerDataProblemOccurence>();
				timerList.add(new TimerDataProblemOccurence(invocation));
				this.timerDataPerMethod.put(methodIdent, timerList);
			}
		}
	}

	/**
	 * Gets {@link #methodIdent}.
	 *
	 * @return {@link #methodIdent}
	 */
	public final long getMethodIdent() {
		return this.methodIdent;
	}

	/**
	 * Sets {@link #methodIdent}.
	 *
	 * @param methodIdent
	 *            New value for {@link #methodIdent}
	 */
	public final void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * Gets {@link #timerDataProblemOccurence}.
	 *
	 * @return {@link #timerDataProblemOccurence}
	 */
	public final TimerDataProblemOccurence getTimerDataProblemOccurence() {
		return this.timerDataProblemOccurence;
	}

	/**
	 * Sets {@link #timerDataProblemOccurence}.
	 *
	 * @param timerDataProblemOccurence
	 *            New value for {@link #timerDataProblemOccurence}
	 */
	public final void setTimerDataProblemOccurence(TimerDataProblemOccurence timerDataProblemOccurence) {
		this.timerDataProblemOccurence = timerDataProblemOccurence;
	}

	/**
	 * Gets {@link #timerDataPerMethod}.
	 *
	 * @return {@link #timerDataPerMethod}
	 */
	public final Map<Long, ArrayList<TimerDataProblemOccurence>> getTimerDataPerMethod() {
		return this.timerDataPerMethod;
	}

	/**
	 * Sets {@link #timerDataPerMethod}.
	 *
	 * @param timerDataPerMethod
	 *            New value for {@link #timerDataPerMethod}
	 */
	public final void setTimerDataPerMethod(Map<Long, ArrayList<TimerDataProblemOccurence>> timerDataPerMethod) {
		this.timerDataPerMethod = timerDataPerMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (this.methodIdent ^ (this.methodIdent >>> 32));
		result = (prime * result) + ((this.timerDataPerMethod == null) ? 0 : this.timerDataPerMethod.hashCode());
		result = (prime * result) + ((this.timerDataProblemOccurence == null) ? 0 : this.timerDataProblemOccurence.hashCode());
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
		RootCause other = (RootCause) obj;
		if (this.methodIdent != other.methodIdent) {
			return false;
		}
		if (this.timerDataPerMethod == null) {
			if (other.timerDataPerMethod != null) {
				return false;
			}
		} else if (!this.timerDataPerMethod.equals(other.timerDataPerMethod)) {
			return false;
		}
		if (this.timerDataProblemOccurence == null) {
			if (other.timerDataProblemOccurence != null) {
				return false;
			}
		} else if (!this.timerDataProblemOccurence.equals(other.timerDataProblemOccurence)) {
			return false;
		}
		return true;
	}


}