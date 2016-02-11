package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.util.PlatformIdentCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * The default implementation of the {@link PlatformIdentDao} interface by using the Entity manager.
 * 
 * @author Patrice Bouillet
 * 
 */
@Repository
public class PlatformIdentDaoImpl extends AbstractJpaDao<PlatformIdent> implements PlatformIdentDao {

	/**
	 * {@link PlatformIdent} cache.
	 */
	@Autowired
	private PlatformIdentCache platformIdentCache;

	/**
	 * Default constructor.
	 */
	public PlatformIdentDaoImpl() {
		super(PlatformIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(PlatformIdent platformIdent) {
		super.delete(platformIdent);
		platformIdentCache.remove(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteAll(List<PlatformIdent> platformIdents) {
		for (PlatformIdent platformIdent : platformIdents) {
			delete(platformIdent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PlatformIdent> findAll() {
		return getEntityManager().createNamedQuery(PlatformIdent.FIND_ALL, PlatformIdent.class).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PlatformIdent> findByName(String agentName) {
		return findByNameAndIps(agentName, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<PlatformIdent> findByNameAndIps(String agentName, List<String> definedIps) {
		TypedQuery<PlatformIdent> query = getEntityManager().createNamedQuery(PlatformIdent.FIND_BY_AGENT_NAME, PlatformIdent.class);
		query.setParameter("agentName", agentName);
		List<PlatformIdent> results = query.getResultList();

		// manually filter the defined IPs
		if (null != definedIps) {
			for (Iterator<PlatformIdent> it = results.iterator(); it.hasNext();) {
				PlatformIdent platformIdent = it.next();
				if (!Objects.equals(definedIps, platformIdent.getDefinedIPs())) {
					it.remove();
				}
			}
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(PlatformIdent platformIdent) {
		final int maxDefIPsSize = 1024;

		if (null != platformIdent.getDefinedIPs()) {
			int charNum = 0;
			List<String> newDefinedIPs = new ArrayList<String>();
			for (String item : platformIdent.getDefinedIPs()) {
				// if it is too long, we stop adding
				if (charNum + item.length() <= maxDefIPsSize) {
					newDefinedIPs.add(item);
					// we add 1 also for the white space
					charNum += item.length() + 1;
				} else {
					break;
				}
			}

			// change only if we really cut the list
			if (newDefinedIPs.size() != platformIdent.getDefinedIPs().size()) {
				platformIdent.setDefinedIPs(newDefinedIPs);
			}
		}

		if (null == platformIdent.getId()) {
			super.create(platformIdent);
		} else {
			super.update(platformIdent);
		}

		platformIdentCache.markDirty(platformIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public PlatformIdent findInitialized(long id) {
		for (PlatformIdent platformIdent : platformIdentCache.getCleanPlatformIdents()) {
			if (platformIdent.getId().longValue() == id) {
				return platformIdent;
			}
		}

		List<PlatformIdent> cleanPlatformIdents = loadIdentsFromDB(Collections.<Long> emptyList(), Collections.singleton(Long.valueOf(id)));
		if (CollectionUtils.isNotEmpty(cleanPlatformIdents)) {
			if (1 == cleanPlatformIdents.size()) {
				return cleanPlatformIdents.get(0);
			} else {
				throw new RuntimeException("More than one agent retrieved for one ID.");
			}
		}

		return null;
	}

	/**
	 * Find all initialized agents that have a id in a given set.
	 * 
	 * @param wantedAgentsIds
	 *            Agents Ids.
	 * @return List of {@link PlatformIdent}.
	 */
	public List<PlatformIdent> findAllInitialized(Set<Long> wantedAgentsIds) {
		if (null == wantedAgentsIds) {
			return Collections.emptyList();
		}

		List<PlatformIdent> initializedPlatformIdents = new ArrayList<PlatformIdent>();
		List<Long> cleanIdents = new ArrayList<Long>();
		for (PlatformIdent platformIdent : platformIdentCache.getCleanPlatformIdents()) {
			cleanIdents.add(platformIdent.getId());
			if (wantedAgentsIds.contains(platformIdent.getId())) {
				initializedPlatformIdents.add(platformIdent);
			}
		}

		wantedAgentsIds.removeAll(cleanIdents);
		if (cleanIdents.size() != platformIdentCache.getSize()) {
			List<PlatformIdent> cleanPlatformIdents = loadIdentsFromDB(cleanIdents, wantedAgentsIds);
			for (PlatformIdent platformIdent : cleanPlatformIdents) {
				if (wantedAgentsIds.contains(platformIdent.getId())) {
					initializedPlatformIdents.add(platformIdent);
				}
			}
		}

		Collections.sort(initializedPlatformIdents, new Comparator<PlatformIdent>() {
			@Override
			public int compare(PlatformIdent o1, PlatformIdent o2) {
				return (int) (o1.getId().longValue() - o2.getId().longValue());
			}
		});
		return initializedPlatformIdents;
	}

	/**
	 * Initialize all platform idents from the database.
	 */
	@PostConstruct
	public void postConstruct() {
		loadIdentsFromDB(Collections.<Long> emptyList(), Collections.<Long> emptyList());
	}

	/**
	 * Loads agents from database, excluding the agents which IDs is supplied in the exclude
	 * collection.
	 * 
	 * @param excludeIdents
	 *            IDs of the agents that should not be loaded. If empty or <code>null</code> it
	 *            won't be taken into consideration.
	 * @param includeIdents
	 *            IDs of the agents that should be loaded. If empty or <code>null</code> it won't be
	 *            taken into consideration.
	 * 
	 * @return List of {@link PlatformIdent}.
	 */
	@SuppressWarnings("unchecked")
	private List<PlatformIdent> loadIdentsFromDB(Collection<Long> excludeIdents, Collection<Long> includeIdents) {
		StringBuilder gl = new StringBuilder(
				"select distinct platformIdent from PlatformIdent as platformIdent left join fetch platformIdent.methodIdents methodIdent left join fetch platformIdent.jmxDefinitionDataIdents jmxDefinitionDataIdents left join fetch platformIdent.sensorTypeIdents left join fetch methodIdent.methodIdentToSensorTypes");
		if (CollectionUtils.isNotEmpty(includeIdents) && CollectionUtils.isNotEmpty(excludeIdents)) {
			gl.append(" where platformIdent.id in :includeIdents and platformIdent.id not in :excludeIdents");
		} else if (CollectionUtils.isNotEmpty(includeIdents)) {
			gl.append(" where platformIdent.id in :includeIdents");
		} else if (CollectionUtils.isNotEmpty(excludeIdents)) {
			gl.append(" where platformIdent.id not in :excludeIdents");
		}

		Query query = getEntityManager().createQuery(gl.toString());
		if (CollectionUtils.isNotEmpty(includeIdents)) {
			query.setParameter("includeIdents", includeIdents);
		}
		if (CollectionUtils.isNotEmpty(excludeIdents)) {
			query.setParameter("excludeIdents", excludeIdents);
		}

		List<PlatformIdent> platformIdents = query.getResultList();
		for (PlatformIdent platformIdent : platformIdents) {
			platformIdentCache.markClean(platformIdent);
		}
		return platformIdents;
	}
}
