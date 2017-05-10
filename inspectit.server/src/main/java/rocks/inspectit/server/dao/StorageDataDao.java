package rocks.inspectit.server.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.AbstractStorageLabelType;

/**
 * Storage data dao interface.
 *
 * @author Ivan Senic
 *
 */
public interface StorageDataDao {

	/**
	 * Saves a label to the database if the same label does not exists.
	 *
	 * @param label
	 *            Label to save.
	 * @return True if label was saved, false otherwise.
	 */
	boolean saveLabel(AbstractStorageLabel<?> label);

	/**
	 * Removes a label.
	 *
	 * @param label
	 *            Label to remove.
	 */
	void removeLabel(AbstractStorageLabel<?> label);

	/**
	 * Removes a collection of labels.
	 *
	 * @param labels
	 *            Labels.
	 */
	void removeLabels(Collection<AbstractStorageLabel<?>> labels);

	/**
	 * Returns all labels registered on the CMR.
	 *
	 * @return Returns all labels registered on the CMR.
	 */
	List<AbstractStorageLabel<?>> getAllLabels();

	/**
	 * Returns all labels of specified type registered on the CMR.
	 *
	 * @param <E>
	 *            Type of label.
	 * @param labelType
	 *            Label type.
	 * @return Returns all labels of specified type registered on the CMR.
	 */
	<E> List<AbstractStorageLabel<E>> getAllLabelsForType(AbstractStorageLabelType<E> labelType);

	/**
	 * Saves the {@link AbstractStorageLabelType} to the database. The label will be saved only if
	 * the {@link AbstractStorageLabelType#isMultiType()} is true or no instances of the label type
	 * are already saved.
	 *
	 * @param labelType
	 *            Label type to save.
	 */
	void saveLabelType(AbstractStorageLabelType<?> labelType);

	/**
	 * Removes label type from database.
	 *
	 * @param labelType
	 *            Label type to remove.
	 * @throws BusinessException
	 *             If there are still labels of this type existing in the database.
	 */
	void removeLabelType(AbstractStorageLabelType<?> labelType) throws BusinessException;

	/**
	 * Returns all instances of desired label type.
	 *
	 * @param <E>
	 *            Label value type.
	 * @param labelTypeClass
	 *            Label type class.
	 * @return List of all instances.
	 */
	<E extends AbstractStorageLabelType<?>> List<E> getLabelTypes(Class<E> labelTypeClass);

	/**
	 * Returns all label types.
	 *
	 * @return Returns all label types.
	 */
	List<AbstractStorageLabelType<?>> getAllLabelTypes();

	/**
	 * Returns all the data that is indexed in the indexing tree for a specific platform ident. Not
	 * that is possible that some data is contained two times in the return list, ones as a object
	 * in the list, ones as a part of invocation that is in the list.
	 *
	 * @param platformId
	 *            Id of agent.
	 * @param fromDate
	 *            Date to search data from. Can be <code>null</code> for no restriction.
	 * @param toDate
	 *            Date to search data to. Can be <code>null</code> for no restriction.
	 * @return List of {@link DefaultData} objects.
	 */
	List<DefaultData> getAllDefaultDataForAgent(long platformId, Date fromDate, Date toDate);

	/**
	 * Returns the fresh data from the buffer which IDs correspond to the given IDs.
	 *
	 * @param elementIds
	 *            Id to search for.
	 * @param platformIdent
	 *            PLatform ident that elements belong to. Value 0 will ignore the platform ident and
	 *            search the complete buffer.
	 * @return Data to be store in storage.
	 */
	List<DefaultData> getDataFromIdList(Collection<Long> elementIds, long platformIdent);

	/**
	 * Returns the fresh data from the buffer which is involved with the given trace id.
	 *
	 * @param traceIds
	 *            Trace id to search for.
	 * @return Data to be store in storage.
	 */
	List<DefaultData> getDataForTraceIdList(Collection<Long> traceIds);

	/**
	 * Returns the last {@link SystemInformationData} for every agent provided in the list.
	 *
	 * @param agentIds
	 *            Collection of agent IDs.
	 * @return List of {@link SystemInformationData}.
	 */
	List<SystemInformationData> getSystemInformationData(Collection<Long> agentIds);

}