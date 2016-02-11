package info.novatec.inspectit.rcp.util;

/**
 * Simple POJO for occurrence counting in the invocation sequence data.
 * 
 * @author Ivan Senic
 * 
 */
public class ElementOccurrenceCount {

	/**
	 * Number of visible occurrences.
	 */
	private int visibleOccurrences;

	/**
	 * Number of filtered occurrences.
	 */
	private int filteredOccurrences;

	/**
	 * Reference to the empty element that can used for returning in exceptional cases.
	 */
	private static ElementOccurrenceCount empty = new ElementOccurrenceCount();

	/**
	 * @return the visibleOccurrences
	 */
	public int getVisibleOccurrences() {
		return visibleOccurrences;
	}

	/**
	 * @param visibleOccurrences
	 *            the visibleOccurrences to set
	 */
	public void setVisibleOccurrences(int visibleOccurrences) {
		if (this != empty) {
			this.visibleOccurrences = visibleOccurrences;
		}
	}

	/**
	 * Increases the visible occurrences count.
	 */
	public void increaseVisibleOccurrences() {
		if (this != empty) {
			visibleOccurrences++;
		}
	}

	/**
	 * @return the filteredOccurrences
	 */
	public int getFilteredOccurrences() {
		return filteredOccurrences;
	}

	/**
	 * @param filteredOccurrences
	 *            the filteredOccurrences to set
	 */
	public void setFilteredOccurrences(int filteredOccurrences) {
		if (this != empty) {
			this.filteredOccurrences = filteredOccurrences;
		}
	}

	/**
	 * Increases the filtered occurrences count.
	 */
	public void increaseFilteredOccurrences() {
		if (this != empty) {
			filteredOccurrences++;
		}
	}

	/**
	 * Total amount of occurrences.
	 * 
	 * @return Total amount of occurrences.
	 */
	public int getTotalOccurrences() {
		return visibleOccurrences + filteredOccurrences;
	}

	/**
	 * Returns the empty element. The returned element content can not be changed.
	 * 
	 * @return Returns the empty element.
	 */
	public static ElementOccurrenceCount emptyElement() {
		return empty;
	}

}
