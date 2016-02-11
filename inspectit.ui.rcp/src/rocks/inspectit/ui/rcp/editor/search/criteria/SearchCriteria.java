package info.novatec.inspectit.rcp.editor.search.criteria;

import org.eclipse.core.runtime.Assert;

/**
 * POJO that holds the information about a search.
 * 
 * @author Ivan Senic
 * 
 */
public class SearchCriteria {

	/**
	 * String to be search for.
	 */
	private String searchString = "";

	/**
	 * Upper case of searched string.
	 */
	private String searchStringUpperCase = "";

	/**
	 * If search is case sensitive.
	 */
	private boolean caseSensitive;

	/**
	 * Default constructor.
	 * 
	 * @param searchString
	 *            String to search.
	 */
	public SearchCriteria(String searchString) {
		this(searchString, false);
	}

	/**
	 * Secondary constructor.
	 * 
	 * @param searchString
	 *            String to search.
	 * @param caseSensitive
	 *            Should search be case sensitive.
	 */
	public SearchCriteria(String searchString, boolean caseSensitive) {
		Assert.isNotNull(searchString);
		this.caseSensitive = caseSensitive;
		this.searchString = searchString;
		this.searchStringUpperCase = searchString.toUpperCase();
	}

	/**
	 * @return the searchString
	 */
	public String getSearchString() {
		return searchString;
	}

	/**
	 * @param searchString
	 *            the searchString to set
	 */
	public void setSearchString(String searchString) {
		this.searchString = searchString;
		if (null != searchString) {
			searchStringUpperCase = searchString.toUpperCase();
		} else {
			searchStringUpperCase = null; // NOPMD
		}
	}

	/**
	 * @return the searcgStringUpperCase
	 */
	public String getSearchStringUpperCase() {
		return searchStringUpperCase;
	}

	/**
	 * @return the caseSensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * @param caseSensitive
	 *            the caseSensitive to set
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (caseSensitive ? 1231 : 1237);
		result = prime * result + ((searchString == null) ? 0 : searchString.hashCode());
		result = prime * result + ((searchStringUpperCase == null) ? 0 : searchStringUpperCase.hashCode());
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
		SearchCriteria other = (SearchCriteria) obj;
		if (caseSensitive != other.caseSensitive) {
			return false;
		}
		if (searchString == null) {
			if (other.searchString != null) {
				return false;
			}
		} else if (!searchString.equals(other.searchString)) {
			return false;
		}
		if (searchStringUpperCase == null) {
			if (other.searchStringUpperCase != null) {
				return false;
			}
		} else if (!searchStringUpperCase.equals(other.searchStringUpperCase)) {
			return false;
		}
		return true;
	}

}
