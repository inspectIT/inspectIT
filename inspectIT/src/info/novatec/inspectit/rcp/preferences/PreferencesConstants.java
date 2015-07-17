package info.novatec.inspectit.rcp.preferences;

/**
 * Interface that just holds the all inspectIT preferences keys.
 * 
 * @author Ivan Senic
 * 
 */
public interface PreferencesConstants {

	/**
	 * Token used to separate the objects when list of properties of objects that are saved.
	 */
	String PREF_OBJECT_SEPARATION_TOKEN = "|";

	/**
	 * Split regex for creating preference string.
	 */
	String PREF_SPLIT_REGEX = "#";

	/**
	 * Preference key for storing CMR repository definitions.
	 */
	String CMR_REPOSITORY_DEFINITIONS = "CMR_REPOSITORY_DEFINITIONS";

	/**
	 * Preference key for columns size of our tables.
	 */
	String TABLE_COLUMN_SIZE_CACHE = "TABLE_COLUMN_SIZE_CACHE";

	/**
	 * Preference key for hidden columns of our tables.
	 */
	String HIDDEN_TABLE_COLUMN_CACHE = "HIDDEN_TABLE_COLUMN_CACHE";

	/**
	 * Preference key for columns order of our tables.
	 */
	String TABLE_COLUMN_ORDER_CACHE = "TABLE_COLUMN_ORDER_CACHE";

	/**
	 * Preference key for refresh rate in the editors.
	 */
	String REFRESH_RATE = "REFRESH_RATE";

	/**
	 * Preference key for decimal places displayed in the editors.
	 */
	String DECIMAL_PLACES = "DECIMAL_PLACES";

	/**
	 * Items to show in editors tables.
	 */
	String ITEMS_COUNT_TO_SHOW = "ITEMS_COUNT_TO_SHOW";

	/**
	 * Invocation filter exclusive time preference.
	 */
	String INVOCATION_FILTER_EXCLUSIVE_TIME = "INVOCATION_FILTER_EXCLUSIVE_TIME";

	/**
	 * Invocation filter total time preference.
	 */
	String INVOCATION_FILTER_TOTAL_TIME = "INVOCATION_FILTER_TOTAL_TIME";

	/**
	 * Invocation filter data types preference.
	 */
	String INVOCATION_FILTER_DATA_TYPES = "INVOCATION_FILTER_DATA_TYPES";

	/**
	 * Last selected repository in data explorer view.
	 */
	String LAST_SELECTED_REPOSITORY = "LAST_SELECTED_REPOSITORY";

	/**
	 * Last selected agent in data explorer view.
	 */
	String LAST_SELECTED_AGENT = "LAST_SELECTED_AGENT";

}
