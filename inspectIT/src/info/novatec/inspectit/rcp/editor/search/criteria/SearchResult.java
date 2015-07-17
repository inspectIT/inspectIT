package info.novatec.inspectit.rcp.editor.search.criteria;

/**
 * Class holding the search result.
 * 
 * @author Ivan Senic
 * 
 */
public class SearchResult {

	/**
	 * Total occurrences found.
	 */
	private int totalOccurrences;

	/**
	 * Current occurrence displayed.
	 */
	private int currentOccurence;

	/**
	 * Can next element be shown.
	 */
	private boolean canShowNext;

	/**
	 * Can previous element be shown.
	 */
	private boolean canShowPrevious;

	/**
	 * Default constructor.
	 * 
	 * @param currentOccurence
	 *            Current occurrence displayed.
	 * @param totalOccurrences
	 *            Total occurrences found.
	 * @param canShowNext
	 *            Can next element be shown.
	 * @param canShowPrevious
	 *            Can previous element be shown.
	 */
	public SearchResult(int currentOccurence, int totalOccurrences, boolean canShowNext, boolean canShowPrevious) {
		super();
		this.currentOccurence = currentOccurence;
		this.totalOccurrences = totalOccurrences;
		this.canShowNext = canShowNext;
		this.canShowPrevious = canShowPrevious;
	}

	/**
	 * Gets {@link #totalOccurrences}.
	 * 
	 * @return {@link #totalOccurrences}
	 */
	public int getTotalOccurrences() {
		return totalOccurrences;
	}

	/**
	 * Sets {@link #totalOccurrences}.
	 * 
	 * @param totalOccurrences
	 *            New value for {@link #totalOccurrences}
	 */
	public void setTotalOccurrences(int totalOccurrences) {
		this.totalOccurrences = totalOccurrences;
	}

	/**
	 * Gets {@link #currentOccurence}.
	 * 
	 * @return {@link #currentOccurence}
	 */
	public int getCurrentOccurence() {
		return currentOccurence;
	}

	/**
	 * Sets {@link #currentOccurence}.
	 * 
	 * @param currentOccurence
	 *            New value for {@link #currentOccurence}
	 */
	public void setCurrentOccurence(int currentOccurence) {
		this.currentOccurence = currentOccurence;
	}

	/**
	 * Gets {@link #canShowNext}.
	 * 
	 * @return {@link #canShowNext}
	 */
	public boolean isCanShowNext() {
		return canShowNext;
	}

	/**
	 * Sets {@link #canShowNext}.
	 * 
	 * @param canShowNext
	 *            New value for {@link #canShowNext}
	 */
	public void setCanShowNext(boolean canShowNext) {
		this.canShowNext = canShowNext;
	}

	/**
	 * Gets {@link #canShowPrevious}.
	 * 
	 * @return {@link #canShowPrevious}
	 */
	public boolean isCanShowPrevious() {
		return canShowPrevious;
	}

	/**
	 * Sets {@link #canShowPrevious}.
	 * 
	 * @param canShowPrevious
	 *            New value for {@link #canShowPrevious}
	 */
	public void setCanShowPrevious(boolean canShowPrevious) {
		this.canShowPrevious = canShowPrevious;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (canShowNext ? 1231 : 1237);
		result = prime * result + (canShowPrevious ? 1231 : 1237);
		result = prime * result + currentOccurence;
		result = prime * result + totalOccurrences;
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
		SearchResult other = (SearchResult) obj;
		if (canShowNext != other.canShowNext) {
			return false;
		}
		if (canShowPrevious != other.canShowPrevious) {
			return false;
		}
		if (currentOccurence != other.currentOccurence) {
			return false;
		}
		if (totalOccurrences != other.totalOccurrences) {
			return false;
		}
		return true;
	}

}
