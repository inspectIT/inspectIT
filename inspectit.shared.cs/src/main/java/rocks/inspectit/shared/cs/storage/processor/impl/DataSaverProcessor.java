package rocks.inspectit.shared.cs.storage.processor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.storage.serializer.util.KryoSerializationPreferences;
import rocks.inspectit.shared.cs.storage.processor.AbstractDataProcessor;

/**
 * {@link DataSaverProcessor} enables definition of classes which objects need to be saved to the
 * storage.
 *
 * @author Ivan Senic
 *
 */
public class DataSaverProcessor extends AbstractDataProcessor {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -5795459378970312428L;

	/**
	 * List of classes that should be saved by this simple saver.
	 */
	private List<Class<? extends DefaultData>> classes;

	/**
	 * If invocation affiliation should be written along side {@link InvocationAwareData}.
	 */
	private boolean writeInvocationAffiliation;

	/**
	 * No-arg constructor.
	 */
	public DataSaverProcessor() {
	}

	/**
	 * Default constructor.
	 *
	 * @param classes
	 *            List of classes to be saved to storage by this {@link AbstractDataProcessor}.
	 * @param writeInvocationAffiliation
	 *            If invocation affiliation should be written along side {@link InvocationAwareData}
	 *            .
	 */
	public DataSaverProcessor(List<Class<? extends DefaultData>> classes, boolean writeInvocationAffiliation) {
		this.classes = classes;
		if (null == this.classes) {
			this.classes = new ArrayList<>();
		}
		this.writeInvocationAffiliation = writeInvocationAffiliation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<Future<Void>> processData(DefaultData defaultData) {
		// if I am writing the InvocationAwareData and invocations are not saved
		// make sure we don't save the invocation affiliation
		if ((defaultData instanceof InvocationAwareData) && !writeInvocationAffiliation) {
			Map<String, Boolean> kryoPreferences = new HashMap<>(1);
			kryoPreferences.put(KryoSerializationPreferences.WRITE_INVOCATION_AFFILIATION_DATA, Boolean.FALSE);
			Future<Void> future = getStorageWriter().write(defaultData, kryoPreferences);
			if (null != future) {
				return Collections.singleton(future);
			}
		} else {
			Future<Void> future = getStorageWriter().write(defaultData);
			if (null != future) {
				return Collections.singleton(future);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		if (null != defaultData) {
			return classes.contains(defaultData.getClass());
		}
		return false;
	}

}
