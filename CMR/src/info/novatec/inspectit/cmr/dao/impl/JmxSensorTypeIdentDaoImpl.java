package info.novatec.inspectit.cmr.dao.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import info.novatec.inspectit.cmr.dao.JmxSensorTypeIdentDao;
import info.novatec.inspectit.cmr.model.JmxSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.util.PlatformIdentCache;

/**
 * The default implementation of the {@link JmxSensorTypeIdentDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Alfred Krauss
 * 
 */
@Repository
public class JmxSensorTypeIdentDaoImpl extends HibernateDaoSupport implements JmxSensorTypeIdentDao {

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
	public JmxSensorTypeIdentDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@link PlatformIdent} cache.
	 */
	@Autowired
	private PlatformIdentCache platformIdentCache;

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<JmxSensorTypeIdent> findByExample(JmxSensorTypeIdent jmxSensorTypeIdent) {
		return getHibernateTemplate().findByExample(jmxSensorTypeIdent);
	}

	/**
	 * {@inheritDoc}
	 */
	public JmxSensorTypeIdent load(Long id) {
		return (JmxSensorTypeIdent) getHibernateTemplate().get(JmxSensorTypeIdent.class, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveOrUpdate(JmxSensorTypeIdent jmxSensorTypeIdent) {
		getHibernateTemplate().saveOrUpdate(jmxSensorTypeIdent);
		platformIdentCache.markDirty(jmxSensorTypeIdent.getPlatformIdent());
	}

}
