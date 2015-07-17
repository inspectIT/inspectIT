package info.novatec.inspectit.rcp.repository.service.storage;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.IGlobalDataAccessService;
import info.novatec.inspectit.cmr.service.exception.ServiceException;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.indexing.query.provider.impl.StorageIndexQueryProvider;
import info.novatec.inspectit.indexing.storage.IStorageTreeComponent;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

/**
 * {@link IGlobalDataAccessService} for storage purposes. This class indirectly uses the
 * {@link AbstractCachedGlobalDataAccessService} to cache the data.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageGlobalDataAccessService extends AbstractStorageService<DefaultData> implements IGlobalDataAccessService {

	/**
	 * List of agents.
	 */
	private List<PlatformIdent> agents;

	/**
	 * Indexing tree.
	 */
	private IStorageTreeComponent<DefaultData> indexingTree;

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
	 * {@inheritDoc}
	 */
	public PlatformIdent getCompleteAgent(long id) throws ServiceException {
		for (PlatformIdent platformIdent : agents) {
			if (platformIdent.getId().longValue() == id) {
				return platformIdent;
			}
		}
		throw new ServiceException("Agent with given ID=" + id + " is not existing.");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Agents can not be deleted on the Storage.
	 */
	public void deleteAgent(long platformId) throws ServiceException {
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> getLastDataObjects(DefaultData template, long timeInterval) {
		Timestamp toDate = new Timestamp(new Date().getTime());
		Timestamp fromDate = new Timestamp(toDate.getTime() - timeInterval);
		return this.getDataObjectsInInterval(template, fromDate, toDate);
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData getLastDataObject(DefaultData template) {
		StorageIndexQuery query = storageIndexQueryProvider.createNewStorageIndexQuery();
		query.setMinId(template.getId());
		ArrayList<Class<?>> searchClasses = new ArrayList<Class<?>>();
		searchClasses.add(template.getClass());
		query.setObjectClasses(searchClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		query.setSensorTypeIdent(template.getSensorTypeIdent());
		List<DefaultData> resultList = super.executeQuery(query);

		if (CollectionUtils.isNotEmpty(resultList)) {
			Iterator<DefaultData> it = resultList.iterator();
			DefaultData lastObject = it.next();
			while (it.hasNext()) {
				DefaultData next = it.next();
				if (next.getTimeStamp().after(lastObject.getTimeStamp())) {
					lastObject = next;
				}
			}
			return lastObject;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> getDataObjectsSinceId(DefaultData template) {
		StorageIndexQuery query = storageIndexQueryProvider.createNewStorageIndexQuery();
		query.setMinId(template.getId());
		ArrayList<Class<?>> searchClasses = new ArrayList<Class<?>>();
		searchClasses.add(template.getClass());
		query.setObjectClasses(searchClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		query.setSensorTypeIdent(template.getSensorTypeIdent());
		if (template instanceof MethodSensorData) {
			query.setMethodIdent(((MethodSensorData) template).getMethodIdent());
			if (template instanceof InvocationSequenceData) {
				query.setOnlyInvocationsWithoutChildren(true);
			}
		}

		return super.executeQuery(query);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> getDataObjectsSinceIdIgnoreMethodId(DefaultData template) {
		StorageIndexQuery query = storageIndexQueryProvider.createNewStorageIndexQuery();
		query.setMinId(template.getId());
		ArrayList<Class<?>> searchClasses = new ArrayList<Class<?>>();
		searchClasses.add(template.getClass());
		query.setObjectClasses(searchClasses);
		query.setPlatformIdent(template.getPlatformIdent());

		return super.executeQuery(query);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<? extends DefaultData> getDataObjectsFromToDate(DefaultData template, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		return this.getDataObjectsInInterval(template, new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<? extends DefaultData> getTemplatesDataObjectsFromToDate(Collection<DefaultData> templates, Date fromDate, Date toDate) {
		if (fromDate.after(toDate)) {
			return Collections.emptyList();
		}

		List<DefaultData> result = new ArrayList<>();
		for (DefaultData template : templates) {
			result.addAll(this.getDataObjectsFromToDate(template, fromDate, toDate));
		}
		return result;
	}

	/**
	 * Returns data objects in wanted interval based on the wanted template.
	 * 
	 * @param template
	 *            Template to base search on.
	 * @param fromDate
	 *            From date as Timestamp.
	 * @param toDate
	 *            To date as Timestamp.
	 * @return List of {@link DefaultData} objects.
	 */
	private List<DefaultData> getDataObjectsInInterval(DefaultData template, Timestamp fromDate, Timestamp toDate) {
		StorageIndexQuery query = storageIndexQueryProvider.createNewStorageIndexQuery();
		ArrayList<Class<?>> searchClasses = new ArrayList<Class<?>>();
		searchClasses.add(template.getClass());
		query.setObjectClasses(searchClasses);
		query.setPlatformIdent(template.getPlatformIdent());
		query.setSensorTypeIdent(template.getSensorTypeIdent());
		query.setToDate(toDate);
		query.setFromDate(fromDate);
		if (template instanceof MethodSensorData) {
			query.setMethodIdent(((MethodSensorData) template).getMethodIdent());
			if (template instanceof InvocationSequenceData) {
				query.setOnlyInvocationsWithoutChildren(true);
			}
		}

		List<DefaultData> returnList = super.executeQuery(query);
		Collections.sort(returnList, new Comparator<DefaultData>() {

			@Override
			public int compare(DefaultData o1, DefaultData o2) {
				return o1.getTimeStamp().compareTo(o2.getTimeStamp());
			}
		});

		return returnList;
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
	protected IStorageTreeComponent<DefaultData> getIndexingTree() {
		return indexingTree;
	}

	/**
	 * @param indexingTree
	 *            the indexingTree to set
	 */
	public void setIndexingTree(IStorageTreeComponent<DefaultData> indexingTree) {
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
