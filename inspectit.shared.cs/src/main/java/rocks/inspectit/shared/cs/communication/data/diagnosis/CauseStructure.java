package rocks.inspectit.shared.cs.communication.data.diagnosis;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * This class represents a CauseStructure. A CauseStructure consists of a CauseType defining the
 * problem type and of the depth size that defines the number of recursive or iterative calls.
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
	 * The depth defines the depths of recursive or iterative calls.
	 */
	@JsonProperty(value = "depth")
	private int depth;

	/**
	 * @param causeType
	 *            CauseType of enum {@link #CauseType}
	 * @param depth
	 *            depth defines the depths of recursive or iterative calls.
	 */
	public CauseStructure(CauseType causeType, int depth) {
		this.causeType = causeType;
		this.depth = depth;
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
	 * Gets {@link #depth}.
	 *
	 * @return {@link #depth}
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Sets {@link #depth}.
	 *
	 * @param depth
	 *            New value for {@link #depth}
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.causeType == null) ? 0 : this.causeType.hashCode());
		result = (prime * result) + this.depth;
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
		if (this.depth != other.depth) {
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
		RECURSIVE,
		/**
		 * The Cause Type is mainly one method to database.
		 */
		SINGLE_DATABASE,
		/**
		 * The Cause Type is due to iterative calls to database..
		 */
		ITERATIVE_DATABASE,
		/**
		 * The Cause Type is due to recursive calls to database..
		 */
		RECURSIVE_DATABASE
	}

}

