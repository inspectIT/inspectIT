package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.IJmxDataAccessService;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.indexing.query.provider.impl.StorageIndexQueryProvider;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link IJmxDataAccessService} for storage purposes. This class indirectly uses the
 * {@link AbstractCachedJmxDataAccessService} to cache the data.
 * 
 * @author Alfred Krauss
 * @author Marius Oehler
 * 
 */
public class StorageJmxDataAccessService extends AbstractStorageService<DefaultData> implements IJmxDataAccessService {

	/**
	 * List of agents.
	 */
	private List<PlatformIdent> agents;

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<? extends DefaultData> indexingTree;

	/**
	 * {@link StorageIndexQueryProvider}.
	 */
	private StorageIndexQueryProvider storageIndexQueryProvider;

	/**
	 * {@inheritDoc}
	 */
	public Map<PlatformIdent, AgentStatusData> getAgentsOverview() {
		Map<PlatformIdent, AgentStatusData> result = new HashMap<PlatformIdent, AgentStatusData>();
		for (PlatformIdent platformIdent : agents) {
			result.put(platformIdent, null);
		}
		return result;
	}

	/**
	 * @param agents
	 *            the agents to set
	 */
	public void setAgents(List<PlatformIdent> agents) {
		this.agents = agents;
	}

	/**
	 * {@inheritDoc}
	 */
	protected IStorageTreeComponent<? extends DefaultData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<? extends DefaultData> indexingTree) {
		this.indexingTree = indexingTree;
	}

	/**
	 * @param storageIndexQueryProvider
	 *            the storageIndexQueryProvider to set
	 */
	public void setStorageIndexQueryProvider(StorageIndexQueryProvider storageIndexQueryProvider) {
		this.storageIndexQueryProvider = storageIndexQueryProvider;
	}

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

		List<DefaultData> resultList = executeQuery(query);

		// Manually select for JmxSensorDefinitionDataIdentId
		if (template.getJmxSensorDefinitionDataIdentId() > 0) {
			for (Iterator<DefaultData> iter = resultList.iterator(); iter.hasNext();) {
				JmxSensorValueData jmxData = (JmxSensorValueData) iter.next();
				if (jmxData.getJmxSensorDefinitionDataIdentId() != template.getJmxSensorDefinitionDataIdentId()) {
					iter.remove();
				}
			}
		}

		List<JmxSensorValueData> returnList;
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

			returnList = new ArrayList<JmxSensorValueData>(map.values());
		} else {
			returnList = new ArrayList<JmxSensorValueData>(resultList.size());
			for (DefaultData data : resultList) {
				returnList.add((JmxSensorValueData) data);
			}
		}

		// sort list
		Collections.sort(returnList, new Comparator<JmxSensorValueData>() {
			@Override
			public int compare(JmxSensorValueData o1, JmxSensorValueData o2) {
				return (int) (o1.getJmxSensorDefinitionDataIdentId() - o2.getJmxSensorDefinitionDataIdentId());
			}
		});
		
		return returnList;
	}

	@Override
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData template) {
		return queryJmxData(template, new Date(0), new Date(), true);
	}

	@Override
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData template, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		return queryJmxData(template, fromDate, toDate, true);
	}

	@Override
	public List<JmxSensorValueData> getJmxData(JmxSensorValueData template, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		return queryJmxData(template, fromDate, toDate, false);
	}
}
