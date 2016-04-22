package rocks.inspectit.server.dao.impl;

import java.util.Iterator;
import java.util.List;

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
	public List<MethodIdent> findForPlatformIdAndExample(long platformId, MethodIdent methodIdentExample) {
		TypedQuery<MethodIdent> query = getEntityManager().createNamedQuery(MethodIdent.FIND_BY_PLATFORM_AND_EXAMPLE, MethodIdent.class);
		query.setParameter("platformIdent", platformId);
		query.setParameter("className", methodIdentExample.getClassName());
		query.setParameter("methodName", methodIdentExample.getMethodName());
		query.setParameter("returnType", methodIdentExample.getReturnType());
		if (null != methodIdentExample.getPackageName()) {
			query.setParameter("packageName", methodIdentExample.getPackageName());
		} else {
			query.setParameter("packageName", "null");
		}

		List<MethodIdent> results = query.getResultList();

		// manually filter the parameters
		for (Iterator<MethodIdent> it = results.iterator(); it.hasNext();) {
			MethodIdent methodIdent = it.next();
			if (!CollectionUtils.isEqualCollection(methodIdent.getParameters(), methodIdentExample.getParameters())) {
				it.remove();
			}
		}

		return results;
	}
}
