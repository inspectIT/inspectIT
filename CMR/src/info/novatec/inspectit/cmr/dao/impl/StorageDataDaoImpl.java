package info.novatec.inspectit.cmr.dao.impl;

import info.novatec.inspectit.cmr.dao.StorageDataDao;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.SystemSensorData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.enumeration.StorageErrorCodeEnum;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.buffer.IBufferTreeComponent;
import info.novatec.inspectit.indexing.impl.IndexingException;
import info.novatec.inspectit.indexing.query.provider.impl.IndexQueryProvider;
import info.novatec.inspectit.indexing.restriction.impl.IndexQueryRestrictionFactory;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;
import info.novatec.inspectit.storage.label.type.impl.AssigneeLabelType;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.label.type.impl.StatusLabelType;
import info.novatec.inspectit.storage.label.type.impl.UseCaseLabelType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Dao support for the storage purposes..
 * 
 * @author Ivan Senic
 * 
 */
@Repository
public class StorageDataDaoImpl extends HibernateDaoSupport implements StorageDataDao {

	/**
	 * {@link IndexQueryProvider}.
	 */
	@Autowired
	private IndexQueryProvider indexQueryProvider;

	/**
	 * {@link IndexingException} tree.
	 */
	@Autowired
	private IBufferTreeComponent<DefaultData> indexingTree;

	/**
	 * This constructor is used to set the {@link SessionFactory} that is needed by
	 * {@link HibernateDaoSupport}. In a future version it may be useful to go away from the
	 * {@link HibernateDaoSupport} and directly use the {@link SessionFactory}. This is described
	 * here:
	 * http://blog.springsource.com/2007/06/26/so-should-you-still-use-springs-hibernatetemplate
	 * -andor-jpatemplate
	 * 
	 * @param sessionFactory
	 *            {@link SessionFactory}
	 */
	@Autowired
	public StorageDataDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean saveLabel(AbstractStorageLabel<?> label) {
		if (label.getStorageLabelType().isValueReusable()) {
			List<?> exampleFind = getHibernateTemplate().findByExample(label);
			if (!exampleFind.contains(label)) {
				AbstractStorageLabelType<?> labelType = label.getStorageLabelType();
				if (null == labelType) {
					return false;
				}
				if (labelType.getId() == 0 && !labelType.isMultiType()) {
					return false;
				}
				getHibernateTemplate().saveOrUpdate(label);
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeLabel(AbstractStorageLabel<?> label) {
		if (label.getStorageLabelType().isValueReusable()) {
			getHibernateTemplate().delete(label);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeLabels(Collection<AbstractStorageLabel<?>> labels) {
		for (AbstractStorageLabel<?> label : labels) {
			this.removeLabel(label);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractStorageLabel<?>> getAllLabels() {
		List<?> allLabels = getHibernateTemplate().loadAll(AbstractStorageLabel.class);
		return (List<AbstractStorageLabel<?>>) allLabels;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <E> List<AbstractStorageLabel<E>> getAllLabelsForType(AbstractStorageLabelType<E> labelType) {
		DetachedCriteria criteria = DetachedCriteria.forClass(AbstractStorageLabel.class);
		criteria.add(Restrictions.eq("storageLabelType", labelType));
		criteria.setFetchMode("storageLabelType", FetchMode.JOIN);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(criteria);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveLabelType(AbstractStorageLabelType<?> labelType) {
		if (labelType.isMultiType()) {
			getHibernateTemplate().saveOrUpdate(labelType);
		} else {
			List<?> findByClass = getHibernateTemplate().loadAll(labelType.getClass());
			if (findByClass.isEmpty()) {
				getHibernateTemplate().saveOrUpdate(labelType);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeLabelType(AbstractStorageLabelType<?> labelType) throws BusinessException {
		if (getAllLabelsForType(labelType).isEmpty()) {
			getHibernateTemplate().delete(labelType);
		} else {
			throw new BusinessException("Delete label type " + labelType.getClass().getSimpleName() + ".", StorageErrorCodeEnum.LABEL_TYPE_CAN_NOT_BE_DELETED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <E extends AbstractStorageLabelType<?>> List<E> getLabelTypes(Class<E> labelTypeClass) {
		return getHibernateTemplate().loadAll(labelTypeClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<AbstractStorageLabelType<?>> getAllLabelTypes() {
		List<?> returnList = getHibernateTemplate().loadAll(AbstractStorageLabelType.class);
		return (List<AbstractStorageLabelType<?>>) returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<DefaultData> getAllDefaultDataForAgent(long platformId, Date fromDate, Date toDate) {
		List<DefaultData> results = new ArrayList<>();

		// first load all from buffer
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.setPlatformIdent(platformId);
		if (null != fromDate) {
			query.setFromDate(new Timestamp(fromDate.getTime()));
		}
		if (null != toDate) {
			query.setToDate(new Timestamp(toDate.getTime()));
		}
		List<DefaultData> bufferData = indexingTree.query(query);
		if (CollectionUtils.isNotEmpty(bufferData)) {
			results.addAll(bufferData);
		}

		// then load all System sensor data from DB
		DetachedCriteria criteria = DetachedCriteria.forClass(SystemSensorData.class);
		criteria.add(Restrictions.eq("platformIdent", platformId));
		if (null != fromDate) {
			criteria.add(Restrictions.ge("timeStamp", new Timestamp(fromDate.getTime())));
		}
		if (null != toDate) {
			criteria.add(Restrictions.le("timeStamp", new Timestamp(toDate.getTime())));
		}
		criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		List<SystemSensorData> sensorDatas = getHibernateTemplate().findByCriteria(criteria);

		// combine results
		if (CollectionUtils.isNotEmpty(sensorDatas)) {
			results.addAll(sensorDatas);
		}

		// since we only have one system information data per agent connection
		// we need to manually add it as we can not be sure that the time stamp of the oldest
		// element in the buffer will include the system data send on the agent connection
		List<SystemInformationData> systemInformationData = getSystemInformationData(Collections.singletonList(platformId));
		if (CollectionUtils.isNotEmpty(systemInformationData)) {
			results.addAll(systemInformationData);
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DefaultData> getDataFromIdList(Collection<Long> elementIds, long platformIdent) {
		IIndexQuery query = indexQueryProvider.createNewIndexQuery();
		query.addIndexingRestriction(IndexQueryRestrictionFactory.isInCollection("id", elementIds));
		query.setPlatformIdent(platformIdent);
		return indexingTree.query(query);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<SystemInformationData> getSystemInformationData(Collection<Long> agentIds) {
		DetachedCriteria subQuery = DetachedCriteria.forClass(SystemInformationData.class);
		subQuery.add(Restrictions.in("platformIdent", agentIds));
		subQuery.setProjection(Projections.projectionList().add(Projections.max("id")));

		DetachedCriteria defaultDataCriteria = DetachedCriteria.forClass(SystemInformationData.class);
		defaultDataCriteria.add(Property.forName("id").in(subQuery));
		defaultDataCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
		return getHibernateTemplate().findByCriteria(defaultDataCriteria);
	}

	/**
	 * Create set of default labels.
	 */
	@PostConstruct
	protected void createDefaultLabelList() {
		this.saveLabelType(new AssigneeLabelType());
		this.saveLabelType(new UseCaseLabelType());
		this.saveLabelType(new RatingLabelType());
		this.saveLabelType(new StatusLabelType());

		AbstractStorageLabelType<String> ratingLabelType = this.getLabelTypes(RatingLabelType.class).get(0);
		List<AbstractStorageLabel<String>> ratingLabelList = this.getAllLabelsForType(ratingLabelType);
		if (ratingLabelList.isEmpty()) {
			// add default rating labels
			this.saveLabel(new StringStorageLabel("Very Bad", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Bad", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Medium", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Good", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Very Good", ratingLabelType));
		}

		AbstractStorageLabelType<String> statusLabelType = this.getLabelTypes(StatusLabelType.class).get(0);
		List<AbstractStorageLabel<String>> statusLabelList = this.getAllLabelsForType(statusLabelType);
		if (statusLabelList.isEmpty()) {
			// add default status labels
			this.saveLabel(new StringStorageLabel("Awaiting Review", statusLabelType));
			this.saveLabel(new StringStorageLabel("In-Progress", statusLabelType));
			this.saveLabel(new StringStorageLabel("Closed", statusLabelType));
		}
	}

}
