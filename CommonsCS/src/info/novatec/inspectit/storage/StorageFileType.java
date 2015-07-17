package info.novatec.inspectit.storage;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of all storage files that exist.
 * 
 * @author Ivan Senic
 * 
 */
public enum StorageFileType {

	/**
	 * Storage index files.
	 */
	INDEX_FILE(".index"),

	/**
	 * Storage data files.
	 */
	DATA_FILE(".itdata"),

	/**
	 * Storage agent files.
	 */
	AGENT_FILE(".agent"),

	/**
	 * Storage info file.
	 */
	STORAGE_FILE(".storage"),

	/**
	 * Local storage info file.
	 */
	LOCAL_STORAGE_FILE(".local"),

	/**
	 * Files that are zipped for export/import.
	 */
	ZIP_STORAGE_FILE(".itds"),

	/**
	 * Cached data files.
	 */
	CACHED_DATA_FILE(".cached");

	/**
	 * The LOOKUP map which is used to get an element of the enumeration when passing the full
	 * qualified name.
	 */
	private static final Map<String, StorageFileType> LOOKUP = new HashMap<String, StorageFileType>();

	static {
		for (StorageFileType fileType : EnumSet.allOf(StorageFileType.class)) {
			LOOKUP.put(fileType.getExtension(), fileType);
		}
	}

	/**
	 * Extension of file type.
	 */
	private String extension;

	/**
	 * Private constructor.
	 * 
	 * @param extension
	 *            Extension of file type.
	 */
	private StorageFileType(String extension) {
		this.extension = extension;
	}

	/**
	 * Gets {@link #extension}.
	 * 
	 * @return {@link #extension}
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Returns the {@link StorageFileType} from the given extension, if extension is valid.
	 * 
	 * @param extension
	 *            Extension.
	 * @return {@link StorageFileType} for corresponding extension or <code>null</code> if extension
	 *         is not existing.
	 */
	public static StorageFileType fromExtension(String extension) {
		return LOOKUP.get(extension);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return super.toString() + " (" + extension + ")";
	}

}
