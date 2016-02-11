package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.service.IJmxDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.indexing.query.provider.impl.StorageIndexQueryProvider;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * {@link IJmxDataAccessService} for storage purposes. This class indirectly uses the
 * {@link AbstractCachedJmxDataAccessService} to cache the data.
 * 
 * @author Alfred Krauss
 * @author Marius Oehler
 * 
 */
public class StorageJmxDataAccessService extends AbstractStorageService<JmxSensorValueData> implements IJmxDataAccessService {

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<JmxSensorValueData> indexingTree;

	/**
	 * {@link StorageIndexQueryProvider}.
	 */
	private StorageIndexQueryProvider storageIndexQueryProvider;

	/**
	 * Returns the {@link JmxSensorValueData} that match the given template and the time span.
	 * 
	 * @param template
	 *            the template
	 * @param fromDate
	 *            get only element after this date
	 * @param toDate
	 *            only elements before this date
	 * @param onlyLatest
	 *            return only the latest element of each sensor
	 * @return a list of {@link JmxSensorValueData} objects
	 */
	private List<JmxSensorValueData> queryJmxData(JmxSensorValueData template, Date fromDate, Date toDate, boolean onlyLatest) {
		StorageIndexQuery query = storageIndexQueryProvider.createNewStorageIndexQuery();

		query.setObjectClasses(Arrays.asList(JmxSensorValueData.class));

		if (template.getPlatformIdent() > 0) {
			query.setPlatformIdent(template.getPlatformIdent());
		}
		if (template.getSensorTypeIdent() > 0) {
			query.setSensorTypeIdent(template.getSensorTypeIdent());
		}
		if (fromDate != null) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (toDate != null) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}

		if (template.getJmxSensorDefinitionDataIdentId() > 0) {
			query.addIndexingRestriction(IndexQueryRestrictionFactory.equal("jmxSensorDefinitionDataIdentId", template.getJmxSensorDefinitionDataIdentId()));
		}

		List<JmxSensorValueData> resultList = executeQuery(query);

		if (onlyLatest) {
			HashMap<Long, JmxSensorValueData> map = new HashMap<Long, JmxSensorValueData>();

			for (DefaultData data : resultList) {
				JmxSensorValueData jmxData = (JmxSensorValueData) data;
				if (map.containsKey(jmxData.getJmxSensorDefinitionDataIdentId())) {
					if (map.get(jmxData.getJmxSensorDefinitionDataIdentId()).getTimeStamp().getTime() < data.getTimeStamp().getTime()) {
						map.put(jmxData.getJmxSensorDefinitionDataIdentId(), (JmxSensorValueData) data);
					}
				} else {
					map.put(jmxData.getJmxSensorDefinitionDataIdentId(), (JmxSensorValueData) data);
				}
			}

			return new ArrayList<JmxSensorValueData>(map.values());
		} else {
			return resultList;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<JmxSensorValueData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData template) {
		return queryJmxData(template, new Date(0), new Date(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData template, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		return queryJmxData(template, fromDate, toDate, true);
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<JmxSensorValueData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param storageIndexQueryProvider
	 *            the storageIndexQueryProvider to set
	 */
	public void setStorageIndexQueryProvider(StorageIndexQueryProvider storageIndexQueryProvider) {
		this.storageIndexQueryProvider = storageIndexQueryProvider;
	}
}
