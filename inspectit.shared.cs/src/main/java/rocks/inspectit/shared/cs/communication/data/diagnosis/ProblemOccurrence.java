package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * This class represents a problem that was detected by the rules applied to the DiagnosisEngine.
 *
 * @author Alexander Wert, Christian Voegele
 *
 */
public class ProblemOccurrence {

	/**
	 * InvocationIdentifier to the request root.
	 */
	@JsonProperty(value = "requestRoot")
	private InvocationIdentifier requestRoot;

	/**
	 * The id to the business transaction name of requestRoot.
	 */
	@JsonProperty(value = "businessTransactionNameIdent")
	private int businessTransactionNameIdent;

	/**
	 * The id to the application name of requestRoot.
	 */
	@JsonProperty(value = "applicationNameIdent")
	private int applicationNameIdent;

	/**
	 * The duration of the requestRoot.
	 */
	@JsonProperty(value = "requestRootDuration")
	private double requestRootDuration;

	/**
	 * InvocationIdentifier to the globalContext.
	 */
	@JsonProperty(value = "globalContext")
	private InvocationIdentifier globalContext;

	/**
	 * InvocationIdentifier to the problemContext.
	 */
	@JsonProperty(value = "problemContext")
	private InvocationIdentifier problemContext;

	/**
	 * The rootCause of the problem.
	 */
	@JsonProperty(value = "rootCause")
	private RootCause rootCause;

	/**
	 * The causeStructure of the problem.
	 */
	@JsonProperty(value = "causeStructure")
	private CauseStructure causeStructure;

	/**
	 * Constructor that creates new ProblemOccurence based on InvocationSequenceData.
	 *
	 * @param requestRoot
	 *            root InvocationSequenceData
	 * @param requestRootDuration
	 *            duration of requestRoot
	 * @param globalContext
	 *            identified globalContext
	 * @param problemContext
	 *            identified problemContext
	 * @param rootCause
	 *            identified rootCauses
	 * @param causeStructure
	 *            The causeStructure
	 */
	public ProblemOccurrence(final InvocationSequenceData requestRoot, final double requestRootDuration, final InvocationSequenceData globalContext, final InvocationSequenceData problemContext,
			final RootCause rootCause, final CauseStructure causeStructure) {
		this.businessTransactionNameIdent = requestRoot.getBusinessTransactionId();
		this.applicationNameIdent = requestRoot.getApplicationId();
		this.requestRoot = new InvocationIdentifier(requestRoot);
		this.requestRootDuration = requestRootDuration;
		this.globalContext = new InvocationIdentifier(globalContext);
		this.problemContext = new InvocationIdentifier(problemContext);
		this.rootCause = rootCause;
		this.causeStructure = causeStructure;
	}

	/**
	 * Gets {@link #requestRoot}.
	 *
	 * @return {@link #requestRoot}
	 */
	public final InvocationIdentifier getRequestRoot() {
		return this.requestRoot;
	}

	/**
	 * Sets {@link #requestRoot}.
	 *
	 * @param requestRoot
	 *            New value for {@link #requestRoot}
	 */
	public final void setRequestRoot(InvocationIdentifier requestRoot) {
		this.requestRoot = requestRoot;
	}

	/**
	 * Gets {@link #businessTransactionNameIdent}.
	 *
	 * @return {@link #businessTransactionNameIdent}
	 */
	public final int getBusinessTransactionNameIdent() {
		return this.businessTransactionNameIdent;
	}

	/**
	 * Sets {@link #businessTransactionNameIdent}.
	 *
	 * @param businessTransactionNameIdent
	 *            New value for {@link #businessTransactionNameIdent}
	 */
	public final void setBusinessTransactionNameIdent(int businessTransactionNameIdent) {
		this.businessTransactionNameIdent = businessTransactionNameIdent;
	}

	/**
	 * Gets {@link #applicationNameIdent}.
	 *
	 * @return {@link #applicationNameIdent}
	 */
	public final int getApplicationNameIdent() {
		return this.applicationNameIdent;
	}

	/**
	 * Sets {@link #applicationNameIdent}.
	 *
	 * @param applicationNameIdent
	 *            New value for {@link #applicationNameIdent}
	 */
	public final void setApplicationNameIdent(int applicationNameIdent) {
		this.applicationNameIdent = applicationNameIdent;
	}

	/**
	 * Gets {@link #requestRootDuration}.
	 *
	 * @return {@link #requestRootDuration}
	 */
	public final double getRequestRootDuration() {
		return this.requestRootDuration;
	}

	/**
	 * Sets {@link #requestRootDuration}.
	 *
	 * @param requestRootDuration
	 *            New value for {@link #requestRootDuration}
	 */
	public final void setRequestRootDuration(double requestRootDuration) {
		this.requestRootDuration = requestRootDuration;
	}

	/**
	 * Gets {@link #globalContext}.
	 *
	 * @return {@link #globalContext}
	 */
	public final InvocationIdentifier getGlobalContext() {
		return this.globalContext;
	}

	/**
	 * Sets {@link #globalContext}.
	 *
	 * @param globalContext
	 *            New value for {@link #globalContext}
	 */
	public final void setGlobalContext(InvocationIdentifier globalContext) {
		this.globalContext = globalContext;
	}

	/**
	 * Gets {@link #problemContext}.
	 *
	 * @return {@link #problemContext}
	 */
	public final InvocationIdentifier getProblemContext() {
		return this.problemContext;
	}

	/**
	 * Sets {@link #problemContext}.
	 *
	 * @param problemContext
	 *            New value for {@link #problemContext}
	 */
	public final void setProblemContext(InvocationIdentifier problemContext) {
		this.problemContext = problemContext;
	}

	/**
	 * Gets {@link #rootCause}.
	 *
	 * @return {@link #rootCause}
	 */
	public final RootCause getRootCause() {
		return this.rootCause;
	}

	/**
	 * Sets {@link #rootCause}.
	 *
	 * @param rootCause
	 *            New value for {@link #rootCause}
	 */
	public final void setRootCause(RootCause rootCause) {
		this.rootCause = rootCause;
	}

	/**
	 * Gets {@link #causeStructure}.
	 *
	 * @return {@link #causeStructure}
	 */
	public final CauseStructure getCauseStructure() {
		return this.causeStructure;
	}

	/**
	 * Sets {@link #causeStructure}.
	 *
	 * @param causeStructure
	 *            New value for {@link #causeStructure}
	 */
	public final void setCauseStructure(CauseStructure causeStructure) {
		this.causeStructure = causeStructure;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + this.applicationNameIdent;
		result = (prime * result) + this.businessTransactionNameIdent;
		result = (prime * result) + ((this.causeStructure == null) ? 0 : this.causeStructure.hashCode());
		result = (prime * result) + ((this.globalContext == null) ? 0 : this.globalContext.hashCode());
		result = (prime * result) + ((this.problemContext == null) ? 0 : this.problemContext.hashCode());
		result = (prime * result) + ((this.requestRoot == null) ? 0 : this.requestRoot.hashCode());
		long temp;
		temp = Double.doubleToLongBits(this.requestRootDuration);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((this.rootCause == null) ? 0 : this.rootCause.hashCode());
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
		ProblemOccurrence other = (ProblemOccurrence) obj;
		if (this.applicationNameIdent != other.applicationNameIdent) {
			return false;
		}
		if (this.businessTransactionNameIdent != other.businessTransactionNameIdent) {
			return false;
		}
		if (this.causeStructure == null) {
			if (other.causeStructure != null) {
				return false;
			}
		} else if (!this.causeStructure.equals(other.causeStructure)) {
			return false;
		}
		if (this.globalContext == null) {
			if (other.globalContext != null) {
				return false;
			}
		} else if (!this.globalContext.equals(other.globalContext)) {
			return false;
		}
		if (this.problemContext == null) {
			if (other.problemContext != null) {
				return false;
			}
		} else if (!this.problemContext.equals(other.problemContext)) {
			return false;
		}
		if (this.requestRoot == null) {
			if (other.requestRoot != null) {
				return false;
			}
		} else if (!this.requestRoot.equals(other.requestRoot)) {
			return false;
		}
		if (Double.doubleToLongBits(this.requestRootDuration) != Double.doubleToLongBits(other.requestRootDuration)) {
			return false;
		}
		if (this.rootCause == null) {
			if (other.rootCause != null) {
				return false;
			}
		} else if (!this.rootCause.equals(other.rootCause)) {
			return false;
		}
		return true;
	}

}