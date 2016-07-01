package rocks.inspectit.server.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import rocks.inspectit.server.dao.StorageDataDao;
import rocks.inspectit.server.util.JpaUtil;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.PlatformSensorData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.enumeration.StorageErrorCodeEnum;
import rocks.inspectit.shared.all.indexing.IIndexQuery;
import rocks.inspectit.shared.cs.indexing.buffer.IBufferTreeComponent;
import rocks.inspectit.shared.cs.indexing.impl.IndexingException;
import rocks.inspectit.shared.cs.indexing.query.provider.impl.IndexQueryProvider;
import rocks.inspectit.shared.cs.indexing.restriction.impl.IndexQueryRestrictionFactory;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.label.StringStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.AbstractStorageLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.AssigneeLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.RatingLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.StatusLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.UseCaseLabelType;

/**
 * DAO support for the storage purposes.
 *
 * @author Ivan Senic
 *
 */
@Repository
public class StorageDataDaoImpl implements StorageDataDao {

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
	 * Entity manager.
	 */
	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Transaction template to use to do init work.
	 */
	private final TransactionTemplate tt;

	/**
	 * Default constructor.
	 * <p>
	 * Needs {@link PlatformTransactionManager} for instantiating the {@link TransactionTemplate} to
	 * execute the initialization.
	 *
	 * @param transactionManager
	 *            {@link PlatformTransactionManager}. Autowired by Spring.
	 */
	@Autowired
	public StorageDataDaoImpl(PlatformTransactionManager transactionManager) {
		this.tt = new TransactionTemplate(transactionManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveLabel(AbstractStorageLabel<?> label) {
		if (label.getStorageLabelType().isValueReusable()) {
			List<?> exampleFind = loadAll(label.getClass());
			if (!exampleFind.contains(label)) {
				AbstractStorageLabelType<?> labelType = label.getStorageLabelType();
				if (null == labelType) {
					return false;
				}
				if ((labelType.getId() == 0) && !labelType.isMultiType()) {
					return false;
				}
				entityManager.persist(label);
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLabel(AbstractStorageLabel<?> label) {
		if (label.getStorageLabelType().isValueReusable()) {
			JpaUtil.delete(entityManager, label);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLabels(Collection<AbstractStorageLabel<?>> labels) {
		for (AbstractStorageLabel<?> label : labels) {
			this.removeLabel(label);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<AbstractStorageLabel<?>> getAllLabels() {
		Query query = entityManager.createNamedQuery(AbstractStorageLabel.FIND_ALL);
		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <E> List<AbstractStorageLabel<E>> getAllLabelsForType(AbstractStorageLabelType<E> labelType) {
		Query query = entityManager.createNamedQuery(AbstractStorageLabel.FIND_BY_LABEL_TYPE);
		query.setParameter("storageLabelType", labelType);
		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveLabelType(AbstractStorageLabelType<?> labelType) {
		if (labelType.isMultiType()) {
			entityManager.persist(labelType);
		} else {

			List<?> findByClass = loadAll(labelType.getClass());
			if (findByClass.isEmpty()) {
				entityManager.persist(labelType);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeLabelType(AbstractStorageLabelType<?> labelType) throws BusinessException {
		if (getAllLabelsForType(labelType).isEmpty()) {
			JpaUtil.delete(entityManager, labelType);
		} else {
			throw new BusinessException("Delete label type " + labelType.getClass().getSimpleName() + ".", StorageErrorCodeEnum.LABEL_TYPE_CAN_NOT_BE_DELETED);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends AbstractStorageLabelType<?>> List<E> getLabelTypes(Class<E> labelTypeClass) {
		return loadAll(labelTypeClass);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<AbstractStorageLabelType<?>> getAllLabelTypes() {
		Query query = entityManager.createNamedQuery(AbstractStorageLabelType.FIND_ALL);
		return query.getResultList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

		// then load all Platform sensor data from DB
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<PlatformSensorData> criteria = builder.createQuery(PlatformSensorData.class);
		Root<? extends PlatformSensorData> root = criteria.from(PlatformSensorData.class);

		Predicate platformIdPredicate = builder.equal(root.get("platformIdent"), platformId);
		Predicate timestampPredicate = null;
		if ((null != fromDate) && (null != toDate)) {
			timestampPredicate = builder.between(root.<Timestamp> get("timeStamp"), new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()));
		}

		if (null != timestampPredicate) {
			criteria.where(platformIdPredicate, timestampPredicate);
		} else {
			criteria.where(platformIdPredicate);
		}
		List<PlatformSensorData> sensorDatas = entityManager.createQuery(criteria).getResultList();

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
	@Override
	@SuppressWarnings("unchecked")
	public List<SystemInformationData> getSystemInformationData(Collection<Long> agentIds) {
		Query query = entityManager.createNamedQuery(SystemInformationData.FIND_LATEST_FOR_PLATFORM_IDS);
		query.setParameter("platformIdents", agentIds);
		return query.getResultList();
	}

	/**
	 * Initializes the default label list.
	 * <p>
	 * Due to to the fact that when PostConstruct method is fired Spring context might not be yet
	 * initialized, there is no guarantee that {@link #createDefaultLabelList()} will be executed in
	 * transactional context even if we annotate it with Transactional. Thus we need to execute
	 * creation with programmatic transaction management.
	 */
	@PostConstruct
	public void init() {
		tt.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				createDefaultLabelList();
			}
		});
	}

	/**
	 * Create set of default labels.
	 */
	private void createDefaultLabelList() {
		this.saveLabelType(new AssigneeLabelType());
		this.saveLabelType(new UseCaseLabelType());

		List<RatingLabelType> ratingLabelTypeList = this.getLabelTypes(RatingLabelType.class);
		RatingLabelType ratingLabelType;
		if (CollectionUtils.isNotEmpty(ratingLabelTypeList)) {
			ratingLabelType = ratingLabelTypeList.get(0);
		} else {
			ratingLabelType = new RatingLabelType();
			this.saveLabelType(ratingLabelType);
		}

		List<AbstractStorageLabel<String>> ratingLabelList = this.getAllLabelsForType(ratingLabelType);
		if (ratingLabelList.isEmpty()) {
			// add default rating labels
			this.saveLabel(new StringStorageLabel("Very Bad", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Bad", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Medium", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Good", ratingLabelType));
			this.saveLabel(new StringStorageLabel("Very Good", ratingLabelType));
		}

		List<StatusLabelType> statusLabelTypeList = this.getLabelTypes(StatusLabelType.class);
		StatusLabelType statusLabelType;
		if (CollectionUtils.isNotEmpty(statusLabelTypeList)) {
			statusLabelType = statusLabelTypeList.get(0);
		} else {
			statusLabelType = new StatusLabelType();
			this.saveLabelType(statusLabelType);
		}

		List<AbstractStorageLabel<String>> statusLabelList = this.getAllLabelsForType(statusLabelType);
		if (statusLabelList.isEmpty()) {
			// add default status labels
			this.saveLabel(new StringStorageLabel("Awaiting Review", statusLabelType));
			this.saveLabel(new StringStorageLabel("In-Progress", statusLabelType));
			this.saveLabel(new StringStorageLabel("Closed", statusLabelType));
		}
	}

	/**
	 * Loads all entities of one class.
	 *
	 * @param <E>
	 *            Type of entity.
	 * @param clazz
	 *            Class
	 * @return List of entities.
	 */
	private <E> List<E> loadAll(Class<E> clazz) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<E> criteria = builder.createQuery(clazz);
		criteria.from(clazz);
		return entityManager.createQuery(criteria).getResultList();
	}

}
