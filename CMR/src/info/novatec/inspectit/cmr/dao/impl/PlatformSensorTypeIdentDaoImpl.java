package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.PlatformSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * The default implementation of the {@link PlatformSensorTypeIdentDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Patrice Bouillet
 * 
 */
@Repository
public class PlatformSensorTypeIdentDaoImpl extends HibernateDaoSupport implements PlatformSensorTypeIdentDao {

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
	public PlatformSensorTypeIdentDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(PlatformSensorTypeIdent platformSensorTypeIdent) {
		getHibernateTemplate().delete(platformSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<PlatformSensorTypeIdent> platformSensorTypeIdents) {
		getHibernateTemplate().deleteAll(platformSensorTypeIdents);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PlatformSensorTypeIdent> findAll() {
		return getHibernateTemplate().loadAll(PlatformSensorTypeIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public PlatformSensorTypeIdent load(Long id) {
		return (PlatformSensorTypeIdent) getHibernateTemplate().get(PlatformSensorTypeIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(PlatformSensorTypeIdent platformSensorTypeIdent) {
		getHibernateTemplate().saveOrUpdate(platformSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<PlatformSensorTypeIdent> findByExample(long platformId, PlatformSensorTypeIdent platformSensorTypeIdent) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(platformSensorTypeIdent.getClass());
		detachedCriteria.add(Example.create(platformSensorTypeIdent));
		detachedCriteria.add(Restrictions.eq("platformIdent.id", platformId));
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

}
