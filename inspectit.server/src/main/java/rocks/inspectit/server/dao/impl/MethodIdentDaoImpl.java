package rocks.inspectit.server.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import rocks.inspectit.server.dao.MethodIdentDao;
import rocks.inspectit.server.util.PlatformIdentCache;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

/**
 * The default implementation of the {@link MethodIdentDao} interface by using the Entity manager.
 *
 * @author Patrice Bouillet
 *
 */
@Repository
public class MethodIdentDaoImpl extends AbstractJpaDao<MethodIdent> implements MethodIdentDao {

	/**
	 * Default constructor.
	 */
	public MethodIdentDaoImpl() {
		super(MethodIdent.class);
	}

	/**
	 * {@link PlatformIdent} cache.
	 */
	@Autowired
	private PlatformIdentCache platformIdentCache;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveOrUpdate(MethodIdent methodIdent) {
		// we save if id is not set, otherwise merge
		if (null == methodIdent.getId()) {
			super.create(methodIdent);
		} else {
			super.update(methodIdent);
		}
		platformIdentCache.markDirty(methodIdent.getPlatformIdent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MethodIdent> findAll() {
		return getEntityManager().createNamedQuery(MethodIdent.FIND_ALL, MethodIdent.class).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Long> findIdForPlatformIdAndExample(long platformId, MethodIdent methodIdentExample, boolean updateTimestamp) {
		TypedQuery<Object[]> query = getEntityManager().createNamedQuery(MethodIdent.FIND_ID_BY_PLATFORM_AND_EXAMPLE, Object[].class);
		query.setParameter("platformIdent", platformId);
		query.setParameter("className", methodIdentExample.getClassName());
		query.setParameter("methodName", methodIdentExample.getMethodName());
		query.setParameter("returnType", methodIdentExample.getReturnType());
		if (null != methodIdentExample.getPackageName()) {
			query.setParameter("packageName", methodIdentExample.getPackageName());
		} else {
			query.setParameter("packageName", "null");
		}

		List<Object[]> queryResults = query.getResultList();
		if (CollectionUtils.isEmpty(queryResults)) {
			return Collections.emptyList();
		}

		List<Long> resultList = new ArrayList<>(0);
		for (Object[] objects : queryResults) {
			if (CollectionUtils.isEqualCollection((Collection<?>) objects[1], methodIdentExample.getParameters())) {
				resultList.add((Long) objects[0]);
			}
		}

		if (updateTimestamp && CollectionUtils.isNotEmpty(resultList)) {
			Query updateQuery = getEntityManager().createNamedQuery(MethodIdent.UPDATE_TIMESTAMP);
			updateQuery.setParameter("ids", resultList);
			updateQuery.executeUpdate();
		}

		return resultList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long findPlatformId(long id) {
		TypedQuery<Long> query = getEntityManager().createNamedQuery(MethodIdent.FIND_PLATFORM_ID_BY_ID, Long.class);
		query.setParameter("id", id);
		return query.getSingleResult();
	}
}
