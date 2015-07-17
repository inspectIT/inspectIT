package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.MethodIdentToSensorTypeDao;
import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * The default implementation of the {@link MethodIdentToSensorTypeDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class MethodIdentToSensorTypeDaoImpl extends HibernateDaoSupport implements MethodIdentToSensorTypeDao {

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 *            the hibernate session factory.
	 */
	@Autowired
	public MethodIdentToSensorTypeDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodIdentToSensorType load(Long id) {
		return getHibernateTemplate().get(MethodIdentToSensorType.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(MethodIdentToSensorType methodIdentToSensorType) {
		getHibernateTemplate().saveOrUpdate(methodIdentToSensorType);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public MethodIdentToSensorType find(long methodIdentId, long methodSensorTypeIdentId) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(MethodIdentToSensorType.class);
		detachedCriteria.add(Restrictions.eq("methodIdent.id", methodIdentId));
		detachedCriteria.add(Restrictions.eq("methodSensorTypeIdent.id", methodSensorTypeIdentId));
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		List<MethodIdentToSensorType> list = getHibernateTemplate().findByCriteria(detachedCriteria, 0, 1);
		if (list.size() == 1) {
			return list.get(0);
		} else {
			return null;
		}
	}
}
