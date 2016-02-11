package info.novatec.inspectit.cmr.storage;

/**
 * Class for providing instances of {@link CmrStorageWriter} when needed.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class CmrStorageWriterProvider {

	/**
	 * @return Returns properly initialized {@link CmrStorageWriter}.
	 */
	protected abstract CmrStorageWriter getCmrStorageWriter();

}
