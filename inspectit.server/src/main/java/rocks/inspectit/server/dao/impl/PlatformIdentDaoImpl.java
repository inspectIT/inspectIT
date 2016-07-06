package rocks.inspectit.server.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.PlatformIdentDao;
import rocks.inspectit.server.util.PlatformIdentCache;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

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
	@Override
	public void saveOrUpdate(PlatformIdent platformIdent) {
		final int maxDefIPsSize = 1024;

		if (null != platformIdent.getDefinedIPs()) {
			int charNum = 0;
			List<String> newDefinedIPs = new ArrayList<>();
			for (String item : platformIdent.getDefinedIPs()) {
				// if it is too long, we stop adding
				if ((charNum + item.length()) <= maxDefIPsSize) {
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
	@Override
	public PlatformIdent findInitialized(long id) {
		for (PlatformIdent platformIdent : platformIdentCache.getCleanPlatformIdents()) {
			if (platformIdent.getId().longValue() == id) {
				return platformIdent;
			}
		}

		List<PlatformIdent> cleanPlatformIdents = loadIdentsFromDB(Collections.singleton(Long.valueOf(id)));
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

		List<PlatformIdent> initializedPlatformIdents = new ArrayList<>();
		List<Long> cleanIdents = new ArrayList<>();
		for (PlatformIdent platformIdent : platformIdentCache.getCleanPlatformIdents()) {
			cleanIdents.add(platformIdent.getId());
			if (wantedAgentsIds.contains(platformIdent.getId())) {
				initializedPlatformIdents.add(platformIdent);
			}
		}

		wantedAgentsIds.removeAll(cleanIdents);
		if (cleanIdents.size() != platformIdentCache.getSize()) {
			List<PlatformIdent> cleanPlatformIdents = loadIdentsFromDB(wantedAgentsIds);
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
		loadIdentsFromDB(Collections.<Long> emptyList());
	}

	/**
	 * Loads complete agents from database.
	 *
	 * @param ids
	 *            IDs of the agents that should be loaded. If empty or <code>null</code> all will be
	 *            loaded.
	 *
	 * @return List of {@link PlatformIdent}.
	 */
	private List<PlatformIdent> loadIdentsFromDB(Collection<Long> ids) {
		List<PlatformIdent> platformIdents = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(ids)) {
			// load one by one
			for (Long id : ids) {
				PlatformIdent platformIdent = load(id);
				if (null != platformIdent) {
					platformIdents.add(platformIdent);
				}
			}
		} else {
			List<PlatformIdent> all = findAll();
			platformIdents.addAll(all);
		}

		for (PlatformIdent platformIdent : platformIdents) {
			platformIdentCache.markClean(platformIdent);
		}
		return platformIdents;
	}
}
