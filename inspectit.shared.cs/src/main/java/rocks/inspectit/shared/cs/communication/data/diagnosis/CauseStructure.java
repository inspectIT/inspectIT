package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This class represents a CauseStructure. A CauseStructure consists of a CauseType defining the
 * problem type and of the SourceType that defines the type of the calls.
 *
 * @author Alexander Wert
 *
 */
public class CauseStructure {

	/**
	 * CauseType of enum {@link #CauseType}.
	 */
	@JsonProperty(value = "causeType")
	private CauseType causeType;

	/**
	 * SourceType of enum {@link #SourceType}.
	 */
	@JsonProperty(value = "sourceType")
	private SourceType sourceType;

	/**
	 * @param causeType
	 *            CauseType of enum {@link #CauseType}
	 * @param sourceType
	 *            SourceType of enum {@link #SourceType}
	 */
	public CauseStructure(CauseType causeType, SourceType sourceType) {
		this.causeType = causeType;
		this.sourceType = sourceType;
	}

	/**
	 * Gets {@link #causeType}.
	 *
	 * @return {@link #causeType}
	 */
	public CauseType getCauseType() {
		return causeType;
	}

	/**
	 * Sets {@link #causeType}.
	 *
	 * @param causeType
	 *            New value for {@link #causeType}
	 */
	public void setCauseType(CauseType causeType) {
		this.causeType = causeType;
	}

	/**
	 * Gets {@link #sourceType}.
	 *
	 * @return {@link #sourceType}
	 */
	public final SourceType getSourceType() {
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
		result = (prime * result) + ((this.causeType == null) ? 0 : this.causeType.hashCode());
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
		CauseStructure other = (CauseStructure) obj;
		if (this.causeType != other.causeType) {
			return false;
		}
		if (this.sourceType != other.sourceType) {
			return false;
		}
		return true;
	}


	/**
	 * @author Alexander Wert
	 *
	 */
	public enum CauseType {
		/**
		 * The Cause Type is mainly one method.
		 */
		SINGLE,
		/**
		 * The Cause Type is due to iterative calls.
		 */
		ITERATIVE,
		/**
		 * The Cause Type is due to recursive calls.
		 */
		RECURSIVE
	}

	/**
	 * @author Christian Voegele
	 *
	 */
	public enum SourceType {
		/**
		 * The source type are database calls.
		 */
		DATABASE,
		/**
		 * The source type are HTTP calls.
		 */
		HTTP,
		/**
		 * The source type are non database / HTTP calls.
		 */
		TIMERDATA
	}

}

