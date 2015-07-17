package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.MethodSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;

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
 * The default implementation of the {@link MethodSensorTypeIdentDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Patrice Bouillet
 * 
 */
@Repository
public class MethodSensorTypeIdentDaoImpl extends HibernateDaoSupport implements MethodSensorTypeIdentDao {

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
	public MethodSensorTypeIdentDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(MethodSensorTypeIdent methodSensorTypeIdent) {
		getHibernateTemplate().delete(methodSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(List<MethodSensorTypeIdent> methodSensorTypeIdents) {
		getHibernateTemplate().deleteAll(methodSensorTypeIdents);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MethodSensorTypeIdent> findAll() {
		return getHibernateTemplate().loadAll(MethodSensorTypeIdent.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodSensorTypeIdent load(Long id) {
		return (MethodSensorTypeIdent) getHibernateTemplate().get(MethodSensorTypeIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(MethodSensorTypeIdent methodSensorTypeIdent) {
		getHibernateTemplate().saveOrUpdate(methodSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<MethodSensorTypeIdent> findByExample(long platformId, MethodSensorTypeIdent methodSensorTypeIdent) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(methodSensorTypeIdent.getClass());
		detachedCriteria.add(Example.create(methodSensorTypeIdent));
		detachedCriteria.add(Restrictions.eq("platformIdent.id", platformId));
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

}
