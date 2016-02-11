package info.novatec.inspectit.storage.processor;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.storage.IWriter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Abstract class for all storage data processors.
 * 
 * @author Ivan Senic
 * 
 */
public abstract class AbstractDataProcessor implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 3557026153566448866L;

	/**
	 * {@link IWriter} to write data to.
	 */
	private transient IWriter storageWriter;

	/**
	 * Processes one {@link DefaultData} object. This method will check is
	 * {@link #canBeProcessed(DefaultData)} is true, and then delegate the processing to the
	 * {@link #processData(DefaultData)} method.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @return Returns list of {@link Future}s if the data processing submitted one or more writing
	 *         tasks. Empty collection means no writing tasks were submitted.
	 */
	public Collection<Future<Void>> process(DefaultData defaultData) {
		if (canBeProcessed(defaultData)) {
			return processData(defaultData);
		}
		return Collections.emptyList();
	}

	/**
	 * Concrete method for processing. IMplemented by sub-classeS.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @return Returns list of {@link Future}s if the data processing submitted one or more writing
	 *         tasks. Empty collection means no writing tasks were submitted.
	 */
	protected abstract Collection<Future<Void>> processData(DefaultData defaultData);

	/**
	 * Returns if the {@link DefaultData} object can be processed by this
	 * {@link AbstractDataProcessor}.
	 * 
	 * @param defaultData
	 *            Default data object.
	 * @return True if data can be processed, false otherwise.
	 */
	public abstract boolean canBeProcessed(DefaultData defaultData);

	/**
	 * Flushes any data that the {@link AbstractDataProcessor} still has. This method will be called
	 * by the storage writer before the storage is closed, so it is ensured that no data remains
	 * unwritten.
	 * <p>
	 * Default implementation of this method does not do anything, subclasses should override it if
	 * any specific action is needed.
	 * 
	 * @return Returns list of {@link Future}s if the data flushing submitted one or more writing
	 *         tasks. Empty collection means no writing tasks were submitted.
	 */
	public Collection<Future<Void>> flush() {
		return Collections.emptyList();
	}

	/**
	 * 
	 * @return the storageWriter
	 */
	protected IWriter getStorageWriter() {
		return storageWriter;
	}

	/**
	 * 
	 * @param storageWriter
	 *            the storage writer
	 */
	public void setStorageWriter(IWriter storageWriter) {
		this.storageWriter = storageWriter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).toString();
	}
}
