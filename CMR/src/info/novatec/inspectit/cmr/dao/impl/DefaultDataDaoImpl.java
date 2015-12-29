package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.DefaultDataDao;
import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.HttpInfo;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.spring.logger.Log;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The default implementation of the {@link DefaultDataDao} interface by using the Entity manager.
 * 
 * @author Patrice Bouillet
 * @author Eduard Tudenhoefner
 * @author Ivan Senic
 * @author Stefan Siegl
 */
@Repository
public class DefaultDataDaoImpl implements DefaultDataDao {

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
	 * Entity manager.
	 */
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * {@inheritDoc}
	 * <p>
	 * We must mark this as transactional cause it's running outside our services.
	 */
	@Transactional
	public void saveAll(List<? extends DefaultData> defaultDataCollection) {
		try {
			for (AbstractCmrDataProcessor processor : cmrDataProcessors) {
				processor.process(defaultDataCollection, entityManager);
			}
		} catch (Exception e) {
			log.error("Error occurred trying to process the CMR data processors on the incoming data.", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> findByExampleWithLastInterval(DefaultData template, long timeInterval) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DefaultData> criteria = builder.createQuery(DefaultData.class);
		Root<? extends DefaultData> root = criteria.from(template.getClass());
		criteria.select(root);

		Predicate platformId = builder.equal(root.get("platformIdent"), template.getPlatformIdent());
		Predicate sensorTypeId = builder.equal(root.get("sensorTypeIdent"), template.getSensorTypeIdent());
		Predicate timestamp = builder.greaterThan(root.<Timestamp> get("timeStamp"), new Timestamp(System.currentTimeMillis() - timeInterval));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			Predicate methodId = builder.equal(root.get("methodIdent"), methodSensorData.getMethodIdent());
			criteria.where(platformId, sensorTypeId, timestamp, methodId);
		} else if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			Predicate jmxSensorDefinitionDataId = builder.equal(root.get("jmxSensorDefinitionDataIdentId"), jmxSensorValueData.getJmxSensorDefinitionDataIdentId());
			criteria.where(platformId, sensorTypeId, timestamp, jmxSensorDefinitionDataId);
		} else {
			criteria.where(platformId, sensorTypeId, timestamp);
		}

		return entityManager.createQuery(criteria).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> findByExampleSinceId(DefaultData template) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DefaultData> criteria = builder.createQuery(DefaultData.class);
		Root<? extends DefaultData> root = criteria.from(template.getClass());
		criteria.select(root);

		Predicate id = builder.greaterThan(root.<Long> get("id"), template.getId());
		Predicate platformId = builder.equal(root.get("platformIdent"), template.getPlatformIdent());
		Predicate sensorTypeId = builder.equal(root.get("sensorTypeIdent"), template.getSensorTypeIdent());

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			Predicate methodId = builder.equal(root.get("methodIdent"), methodSensorData.getMethodIdent());
			criteria.where(id, platformId, sensorTypeId, methodId);
		} else if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			Predicate jmxSensorDefinitionDataId = builder.equal(root.get("jmxSensorDefinitionDataIdentId"), jmxSensorValueData.getJmxSensorDefinitionDataIdentId());
			criteria.where(id, platformId, sensorTypeId, jmxSensorDefinitionDataId);
		} else {
			criteria.where(id, platformId, sensorTypeId);
		}

		return entityManager.createQuery(criteria).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> findByExampleSinceIdIgnoreMethodId(DefaultData template) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DefaultData> criteria = builder.createQuery(DefaultData.class);
		Root<? extends DefaultData> root = criteria.from(template.getClass());
		criteria.select(root);

		Predicate id = builder.greaterThan(root.<Long> get("id"), template.getId());
		Predicate platformId = builder.equal(root.get("platformIdent"), template.getPlatformIdent());

		criteria.where(id, platformId);

		return entityManager.createQuery(criteria).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<DefaultData> findByExampleFromToDate(DefaultData template, Date fromDate, Date toDate) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DefaultData> criteria = builder.createQuery(DefaultData.class);
		Root<? extends DefaultData> root = criteria.from(template.getClass());
		criteria.select(root);

		Predicate platformId = builder.equal(root.get("platformIdent"), template.getPlatformIdent());
		Predicate sensorTypeId = builder.equal(root.get("sensorTypeIdent"), template.getSensorTypeIdent());
		Predicate timestamp = builder.between(root.<Timestamp> get("timeStamp"), new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()));

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			Predicate methodId = builder.equal(root.get("methodIdent"), methodSensorData.getMethodIdent());
			criteria.where(platformId, sensorTypeId, timestamp, methodId);
		} else if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			Predicate jmxSensorDefinitionDataId = builder.equal(root.get("jmxSensorDefinitionDataIdentId"), jmxSensorValueData.getJmxSensorDefinitionDataIdentId());
			criteria.where(platformId, sensorTypeId, timestamp, jmxSensorDefinitionDataId);
		} else {
			criteria.where(platformId, sensorTypeId, timestamp);
		}

		return entityManager.createQuery(criteria).getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	public DefaultData findByExampleLastData(DefaultData template) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<DefaultData> criteria = builder.createQuery(DefaultData.class);
		Root<? extends DefaultData> root = criteria.from(template.getClass());
		criteria.select(root);

		Predicate platformId = builder.equal(root.get("platformIdent"), template.getPlatformIdent());

		if (template instanceof MethodSensorData) {
			MethodSensorData methodSensorData = (MethodSensorData) template;
			Predicate methodId = builder.equal(root.get("methodIdent"), methodSensorData.getMethodIdent());
			criteria.where(platformId, methodId);
		} else if (template instanceof JmxSensorValueData) {
			JmxSensorValueData jmxSensorValueData = (JmxSensorValueData) template;
			Predicate jmxSensorDefinitionDataId = builder.equal(root.get("jmxSensorDefinitionDataIdentId"), jmxSensorValueData.getJmxSensorDefinitionDataIdentId());
			criteria.where(platformId, jmxSensorDefinitionDataId);
		} else {
			criteria.where(platformId);
		}

		criteria.orderBy(builder.desc(root.get("id")));

		List<DefaultData> results = entityManager.createQuery(criteria).setMaxResults(1).getResultList();
		if (CollectionUtils.isNotEmpty(results)) {
			return results.get(0);
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<HttpTimerData> getChartingHttpTimerDataFromDateToDate(Collection<HttpTimerData> templates, Date fromDate, Date toDate, boolean retrieveByTag) {
		if (CollectionUtils.isNotEmpty(templates)) {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<HttpTimerData> criteria = builder.createQuery(HttpTimerData.class);
			Root<? extends HttpTimerData> root = criteria.from(HttpTimerData.class);

			Predicate platformId = builder.equal(root.get("platformIdent"), templates.iterator().next().getPlatformIdent());
			Predicate timestamp = builder.between(root.<Timestamp> get("timeStamp"), new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()));
			Predicate condition = null;

			if (!retrieveByTag) {
				Set<String> uris = new HashSet<String>();
				for (HttpTimerData httpTimerData : templates) {
					if (!HttpInfo.UNDEFINED.equals(httpTimerData.getHttpInfo().getUri())) {
						uris.add(httpTimerData.getHttpInfo().getUri());
					}
				}
				condition = root.join("httpInfo").get("uri").in(uris);
			} else {
				Set<String> tags = new HashSet<String>();

				for (HttpTimerData httpTimerData : templates) {
					if (httpTimerData.getHttpInfo().hasInspectItTaggingHeader()) {
						tags.add(httpTimerData.getHttpInfo().getInspectItTaggingHeaderValue());
					}
				}
				condition = root.join("httpInfo").get("inspectItTaggingHeaderValue").in(tags);
			}

			criteria.where(platformId, timestamp, condition);

			return entityManager.createQuery(criteria).getResultList();
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void deleteAll(Long platformId) {
		// we need to delete each instance of the SystemInformationData, so that all VmArgumentData
		// orphans are also deleted, cause it can not be done with H2
		Query query = entityManager.createNamedQuery(SystemInformationData.FIND_ALL_FOR_PLATFORM_ID);
		query.setParameter("platformIdent", platformId);
		for (Object s : query.getResultList()) {
			entityManager.remove(s);
		}

		query = entityManager.createNamedQuery(DefaultData.DELETE_FOR_PLATFORM_ID);
		query.setParameter("platformIdent", platformId);
		query.executeUpdate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<JmxSensorValueData> getJmxDataOverview(JmxSensorValueData template, Date fromDate, Date toDate) {
		if (template == null) {
			return null;
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<JmxSensorValueData> c = cb.createQuery(JmxSensorValueData.class);
		Root<JmxSensorValueData> root = c.from(JmxSensorValueData.class);

		Subquery<Long> sq = c.subquery(Long.class);
		Root<JmxSensorValueData> sqRoot = sq.from(JmxSensorValueData.class);

		Predicate platformIdentPredicate = cb.equal(sqRoot.get("platformIdent"), template.getPlatformIdent());
		Predicate sensorTypeIdentPredicate = cb.equal(sqRoot.get("sensorTypeIdent"), template.getSensorTypeIdent());
		Predicate predicate = cb.and(platformIdentPredicate, sensorTypeIdentPredicate);

		if (template.getJmxSensorDefinitionDataIdentId() > 0) {
			predicate = cb.and(predicate, cb.equal(sqRoot.get("jmxSensorDefinitionDataIdentId"), template.getJmxSensorDefinitionDataIdentId()));
		}

		if (fromDate != null && toDate != null) {
			predicate = cb.and(predicate, cb.between(sqRoot.get("timeStamp").as(Date.class), fromDate, toDate));
		}

		sq.select(cb.max(sqRoot.get("id").as(Long.class))).where(predicate).groupBy(sqRoot.get("jmxSensorDefinitionDataIdentId"));

		c.select(root).where(cb.in(root.get("id")).value(sq));
		
		return entityManager.createQuery(c).getResultList();
	}
}