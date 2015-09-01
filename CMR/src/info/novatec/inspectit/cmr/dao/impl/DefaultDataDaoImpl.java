package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.spring.logger.Log;

import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The default implementation of the {@link DefaultDataDao} interface by using the
 * {@link HibernateDaoSupport} from Spring.
 * <p>
 * Delegates many calls to the {@link HibernateTemplate} returned by the {@link HibernateDaoSupport}
 * class.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Ivan Senic
 * @author Stefan Siegl
 */
@Repository
public class DefaultDataDaoImpl extends HibernateDaoSupport implements DefaultDataDao {

	/** The logger of this class. */
	@Log
	Logger log;

	/**
	 * List of processors.
	 */
	@Autowired
	@Resource(name = "cmrDataProcessorList")
	// resource must be specified, otherwise all processor all plugged here
	private List<AbstractCmrDataProcessor> cmrDataProcessors;

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
	public DefaultDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public void save(DefaultData defaultData) {
		getHibernateTemplate().save(defaultData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveAll(List<? extends DefaultData> defaultDataCollection) {
		StatelessSession session = getHibernateTemplate().getSessionFactory().openStatelessSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			for (AbstractCmrDataProcessor processor : cmrDataProcessors) {
				processor.process(defaultDataCollection, session);
			}
			tx.commit();
		} catch (Exception e) {
			if (null != tx) {
				tx.rollback();
			}

			log.error("Error occurred trying to process the CMR data processors on the incoming data. Transaction rolled back.", e);
		}
		session.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<DefaultData> findByExampleWithLastInterval(DefaultData template, long timeInterval) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));
		defaultDataCriteria.add(Restrictions.gt("timeStamp", new Timestamp(System.currentTimeMillis() - timeInterval)));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			defaultDataCriteria.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			defaultDataCriteria.add(Restrictions.eq("jmxSensorDefinitionDataIdentId", jmxSensorValueData.getJmxSensorDefinitionDataIdentId()));
		}

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<DefaultData> findByExampleSinceId(DefaultData template) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.gt("id", template.getId()));
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			defaultDataCriteria.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			defaultDataCriteria.add(Restrictions.eq("jmxSensorDefinitionDataIdentId", jmxSensorValueData.getJmxSensorDefinitionDataIdentId()));
		}

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List findByExampleSinceIdIgnoreMethodId(DefaultData template) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.gt("id", template.getId()));
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<DefaultData> findByExampleFromToDate(DefaultData template, Date fromDate, Date toDate) {
		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		defaultDataCriteria.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		defaultDataCriteria.add(Restrictions.between("timeStamp", new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime())));

		if (!(template instanceof JmxSensorValueData) || template.getSensorTypeIdent() > 0) {
			defaultDataCriteria.add(Restrictions.eq("sensorTypeIdent", template.getSensorTypeIdent()));
		}

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			defaultDataCriteria.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			defaultDataCriteria.add(Restrictions.eq("jmxSensorDefinitionDataIdentId", jmxSensorValueData.getJmxSensorDefinitionDataIdentId()));
		}

		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	public DefaultData findByExampleLastData(DefaultData template) {
		DetachedCriteria subQuery = DetachedCriteria.forClass(template.getClass());
		subQuery.add(Restrictions.eq("platformIdent", template.getPlatformIdent()));
		subQuery.setProjection(Projections.projectionList().add(Projections.max("id")));

		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(template.getClass());
		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			subQuery.add(Restrictions.eq("methodIdent", methodSensorData.getMethodIdent()));
			defaultDataCriteria.setFetchMode("parameterContentData", FetchMode.JOIN);
		}

		if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			subQuery.add(Restrictions.eq("jmxSensorDefinitionDataIdentId", jmxSensorValueData.getJmxSensorDefinitionDataIdentId()));
		}

		defaultDataCriteria.add(Property.forName("id").eq(subQuery));
		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		List<DefaultData> resultList = getHibernateTemplate().findByCriteria(defaultDataCriteria);
		if (CollectionUtils.isNotEmpty(resultList)) {
			return resultList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<HttpTimerData> getChartingHttpTimerDataFromDateToDate(Collection<HttpTimerData> templates, Date fromDate, Date toDate, boolean retrieveByTag) {
		if (CollectionUtils.isNotEmpty(templates)) {
			DetachedCriteria criteria = DetachedCriteria.forClass(HttpTimerData.class);
			criteria.add(Restrictions.eq("platformIdent", templates.iterator().next().getPlatformIdent()));
			criteria.add(Restrictions.between("timeStamp", new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime())));

			if (!retrieveByTag) {
				Set<String> uris = new HashSet<String>();
				for (HttpTimerData httpTimerData : templates) {
					if (!HttpTimerData.UNDEFINED.equals(httpTimerData.getUri())) {
						uris.add(httpTimerData.getUri());
					}
				}
				criteria.add(Restrictions.in("uri", uris));
			} else {
				Set<String> tags = new HashSet<String>();

				for (HttpTimerData httpTimerData : templates) {
					if (httpTimerData.hasInspectItTaggingHeader()) {
						tags.add(httpTimerData.getInspectItTaggingHeaderValue());
					}
				}
				criteria.add(Restrictions.in("inspectItTaggingHeaderValue", tags));
			}

			criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
			return getHibernateTemplate().findByCriteria(criteria);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(Long platformId) {
		// because H2 does not support cascading on delete we need to clear all connected data
		Query query = getSession().createQuery("delete from VmArgumentData where systemInformationId in (select id from SystemInformationData where platformIdent = :platformIdent)");
		query.setLong("platformIdent", platformId);
		query.executeUpdate();

		// the code below is the workaround the Hibernate batch delete problem of all DefaultData
		// instances
		// any batch delete or delete executed on the abstract class will raise problem with not
		// existing temporary tables
		Map<String, ClassMetadata> map = getSessionFactory().getAllClassMetadata();
		for (Entry<String, ClassMetadata> entry : map.entrySet()) {
			// for each mapped class check:
			// * that is not abstract
			// * that it's a default data subclass
			// * that has platfromIdent property
			ClassMetadata classMetadata = entry.getValue();
			String className = entry.getKey();
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				continue;
			}
			boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
			if (!isAbstract && classMetadata instanceof AbstractEntityPersister) {
				isAbstract = ((AbstractEntityPersister) classMetadata).isAbstract();
			}

			if (!isAbstract && DefaultData.class.isAssignableFrom(clazz)) {
				boolean hasPlatformIdent = ArrayUtils.contains(classMetadata.getPropertyNames(), "platformIdent");
				if (hasPlatformIdent) {
					// then delete that default data
					query = getSession().createQuery("delete from " + className + " where platformIdent = :platformIdent");
					query.setLong("platformIdent", platformId);
					query.executeUpdate();
				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData template, Date fromDate, Date toDate) {
		if (template == null) {
			return null;
		}

		StringBuilder hql = new StringBuilder("FROM JmxSensorValueData data WHERE id IN (SELECT MAX(sub.id) FROM JmxSensorValueData sub WHERE sub.platformIdent = :platformId");

		if (template.getSensorTypeIdent() > 0) {
			hql.append(" AND sub.jmxSensorDefinitionDataIdentId = :sensorDefinitionId");
		}

		if (fromDate != null && toDate != null) {
			hql.append(" AND sub.timeStamp BETWEEN :fromTime and :toTime");
		}

		hql.append(" GROUP BY sub.jmxSensorDefinitionDataIdentId)");

		Query query = getSession().createQuery(hql.toString());
		query.setLong("platformId", template.getPlatformIdent());

		if (template.getSensorTypeIdent() > 0) {
			query.setLong("sensorDefinitionId", template.getSensorTypeIdent());
		}

		if (fromDate != null && toDate != null) {
			query.setTimestamp("fromTime", fromDate);
			query.setTimestamp("toTime", toDate);
		}

		return query.list();
	}
}