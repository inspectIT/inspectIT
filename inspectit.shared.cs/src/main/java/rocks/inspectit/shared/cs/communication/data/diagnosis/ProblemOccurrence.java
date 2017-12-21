package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.CauseType;
import rocks.inspectit.shared.cs.communication.data.diagnosis.CauseStructure.SourceType;

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
	 * The causeType of the problem.
	 */
	@JsonProperty(value = "causeType")
	private CauseType causeType;

	/**
	 * The sourceType of the problem.
	 */
	@JsonProperty(value = "sourceType")
	private SourceType sourceType;

	/**
	 * Constructor that creates new ProblemOccurrence based on InvocationSequenceData.
	 *
	 * @param requestRoot
	 *            root InvocationSequenceData
	 * @param globalContext
	 *            identified globalContext
	 * @param problemContext
	 *            identified problemContext
	 * @param rootCause
	 *            identified rootCauses
	 * @param causeType
	 *            The causeType
	 * @param sourceType
	 *            The sourceType
	 */
	public ProblemOccurrence(final InvocationSequenceData requestRoot, final InvocationSequenceData globalContext, final InvocationSequenceData problemContext,
			final RootCause rootCause, final CauseType causeType, final SourceType sourceType) {

		if (requestRoot == null) {
			throw new IllegalArgumentException("requestRoot cannot be null");
		}
		if (globalContext == null) {
			throw new IllegalArgumentException("globalContext cannot be null");
		}
		if (problemContext == null) {
			throw new IllegalArgumentException("problemContext cannot be null");
		}
		if (rootCause == null) {
			throw new IllegalArgumentException("rootCause cannot be null");
		}
		if (causeType == null) {
			throw new IllegalArgumentException("causeType cannot be null");
		}
		if (sourceType == null) {
			throw new IllegalArgumentException("sourceType cannot be null");
		}

		this.businessTransactionNameIdent = requestRoot.getBusinessTransactionId();
		this.applicationNameIdent = requestRoot.getApplicationId();
		this.requestRoot = new InvocationIdentifier(requestRoot);
		this.globalContext = new InvocationIdentifier(globalContext);
		this.problemContext = new InvocationIdentifier(problemContext);
		this.rootCause = rootCause;
		this.causeType = causeType;
		this.sourceType = sourceType;
	}

	/**
	 * Constructor that creates new ProblemOccurrence based on InvocationSequenceData.
	 *
	 * @param requestRoot
	 *            root InvocationSequenceData
	 * @param globalContext
	 *            identified globalContext
	 * @param rootCause
	 *            identified rootCauses
	 */
	public ProblemOccurrence(final InvocationSequenceData requestRoot, final InvocationSequenceData globalContext, final RootCause rootCause) {

		if (requestRoot == null) {
			throw new IllegalArgumentException("requestRoot cannot be null");
		}
		if (globalContext == null) {
			throw new IllegalArgumentException("globalContext cannot be null");
		}
		if (rootCause == null) {
			throw new IllegalArgumentException("rootCause cannot be null");
		}

		this.businessTransactionNameIdent = requestRoot.getBusinessTransactionId();
		this.applicationNameIdent = requestRoot.getApplicationId();
		this.requestRoot = new InvocationIdentifier(requestRoot);
		this.globalContext = new InvocationIdentifier(globalContext);
		this.problemContext = new InvocationIdentifier(globalContext);
		this.rootCause = rootCause;
		this.causeType = CauseType.SINGLE;
		this.sourceType = SourceType.TIMERDATA;
	}


	/**
	 * Gets {@link #requestRoot}.
	 *
	 * @return {@link #requestRoot}
	 */
	public InvocationIdentifier getRequestRoot() {
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
	public int getBusinessTransactionNameIdent() {
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
	public int getApplicationNameIdent() {
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
	 * Gets {@link #globalContext}.
	 *
	 * @return {@link #globalContext}
	 */
	public InvocationIdentifier getGlobalContext() {
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
	public InvocationIdentifier getProblemContext() {
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
	public RootCause getRootCause() {
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
	 * Gets {@link #causeType}.
	 *
	 * @return {@link #causeType}
	 */
	public CauseType getCauseType() {
		return this.causeType;
	}

	/**
	 * Sets {@link #causeType}.
	 *
	 * @param causeType
	 *            New value for {@link #causeType}
	 */
	public final void setCauseType(CauseType causeType) {
		this.causeType = causeType;
	}

	/**
	 * Gets {@link #sourceType}.
	 *
	 * @return {@link #sourceType}
	 */
	public SourceType getSourceType() {
		return this.sourceType;
	}

	/**
	 * Sets {@link #sourceType}.
	 *
	 * @param sourceType
	 *            New value for {@link #sourceType}
	 */
	public final void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
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
		result = (prime * result) + ((this.causeType == null) ? 0 : this.causeType.hashCode());
		result = (prime * result) + ((this.globalContext == null) ? 0 : this.globalContext.hashCode());
		result = (prime * result) + ((this.problemContext == null) ? 0 : this.problemContext.hashCode());
		result = (prime * result) + ((this.requestRoot == null) ? 0 : this.requestRoot.hashCode());
		result = (prime * result) + ((this.rootCause == null) ? 0 : this.rootCause.hashCode());
		result = (prime * result) + ((this.sourceType == null) ? 0 : this.sourceType.hashCode());
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
		if (this.causeType != other.causeType) {
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
		if (this.rootCause == null) {
			if (other.rootCause != null) {
				return false;
			}
		} else if (!this.rootCause.equals(other.rootCause)) {
			return false;
		}
		if (this.sourceType != other.sourceType) {
			return false;
		}
		return true;
	}

}